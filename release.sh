#!/usr/bin/env bash
# release.sh — Manual "prepare + perform" for Apache Velocity Engine
#
# Usage:
#   ./release.sh <RELEASE_VERSION> <RC_NUMBER> [--dry-run] [--gpg-keyname <KEYID>] [--profile <MVN_PROFILE>] [--help]
#
# Example:
#   ./release.sh 2.4 RC1
#
# Behavior:
# - Sets versions to RELEASE (drops -SNAPSHOT), sets <scm><tag> to RELEASE TAG (e.g., 2.5),
#   commits, creates and pushes RC tag (e.g., 2.5-RC1), then runs `mvn release:perform`
#   from that tag with goals=deploy (tests skipped), using SCM URL read from the POM.
# - After perform, assembles a "dist/dev" staging folder under:
#     target/velocity-staging/engine/${RELEASE}/
#   computes *.sha256 for each file and signature, and prints copy instructions.
# - Bumps to NEXT dev version (defaults to next MINOR: X.(Y+1)-SNAPSHOT),
#   resets <scm><tag> to HEAD, commits.
#
# Requirements:
# - Credentials & server ids in ~/.m2/settings.xml
#    <server>
#      <id>apache.releases.https</id>
#      <username>your-apache-id</username>
#      <password>your-apache-password</password>
#    </server>
#
# 
# The GPG key can be provided:
# - by defining an "apache-release" profile in ~/.m2/settings.xml :
#    <profile>
#        <id>apache-release-key</id>
#        <properties>
#            <gpg.keyname>...</gpg.keyname>
#        </properties>
#    </profile>
# - by using another profile name with the --profile option
# - by explicitly giving a GPG key with the --gpg-keyname option

set -euo pipefail

# -------------------------
# Defaults / globals
# -------------------------
MODULE=engine
DRY_RUN=0
MVN_PROFILE="${MVN_PROFILE:-apache-release}"
GPG_KEYNAME="${GPG_KEYNAME:-}"

# -------------------------
# Helpers
# -------------------------
die() { echo "ERROR: $*" >&2; exit 1; }
pause() { read -r -p "$* Press Enter to continue..."; }
is_dry() { [[ "$DRY_RUN" -eq 1 ]]; }
run() { if is_dry; then echo "[DRY-RUN] $*"; else echo "[RUN] $*"; eval "$@"; fi }
git_run() { if is_dry; then echo "[DRY-RUN] git $*"; else echo "[RUN] git $*"; git "$@"; fi }
mvn_q() { if is_dry; then echo "[DRY-RUN] mvn $*"; else echo "[RUN] mvn $*"; mvn "$@"; fi }

usage() {
  cat <<EOF
Usage:
  $0 <RELEASE_VERSION> <RC_NUMBER> [--dry-run] [--gpg-keyname <KEYID>] [--profile <MVN_PROFILE>] [--help]

Examples:
  $0 2.4 RC1
  $0 2.4.1 RC2 --gpg-keyname BEFEEF227A98B809

Options:
  --dry-run            Show what would happen; do not mutate git or run deploys.
  --gpg-keyname K      Explicit GPG key id/name (overrides settings.xml).
  --profile P          Maven profile to activate for release (default: apache-release).
  --help               Print this help.
EOF
}

# -------------------------
# Parse args
# -------------------------
if [[ "${1:-}" == "--help" || $# -lt 2 ]]; then usage; exit 0; fi

RELEASE="$1"; shift
RC_NUM_RAW="$1"; shift
# Normalize RC: enforce UPPERCASE "RCN"
RC_NUM="$(echo "$RC_NUM_RAW" | tr '[:lower:]' '[:upper:]')"
[[ "$RC_NUM" =~ ^RC[0-9]+$ ]] || die "RC number must look like RC1, RC2... (got '$RC_NUM_RAW')"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1 ;;
    --gpg-keyname) shift; GPG_KEYNAME="${1:-}"; [[ -n "$GPG_KEYNAME" ]] || die "--gpg-keyname requires a value" ;;
    --profile) shift; MVN_PROFILE="${1:-}"; [[ -n "$MVN_PROFILE" ]] || die "--profile requires a value" ;;
    --help) usage; exit 0 ;;
    *) die "Unknown option: $1" ;;
  esac
  shift
done

# Derived names
RELEASE_TAG="$RELEASE"            # final tag (e.g., 2.4)
RC_TAG="${RELEASE}-${RC_NUM}"     # e.g., 2.4-RC1

# Compute NEXT version = next minor snapshot by default (2.4 -> 2.5-SNAPSHOT)
compute_next_version() {
  local v="$1"
  if [[ "$v" =~ ^([0-9]+)\.([0-9]+)(\.([0-9]+))?$ ]]; then
    local MAJ="${BASH_REMATCH[1]}"; local MIN="${BASH_REMATCH[2]}"
    echo "${MAJ}.$((MIN+1))-SNAPSHOT"
  else
    # Fallback: append -SNAPSHOT if not three-part semantic
    echo "${v}-SNAPSHOT"
  fi
}
NEXT_DEFAULT="$(compute_next_version "$RELEASE")"

echo "Planned versions/tags:"
echo "  RELEASE     : $RELEASE"
echo "  RC TAG      : $RC_TAG"
echo "  FINAL TAG   : $RELEASE_TAG"
echo "  NEXT (default): $NEXT_DEFAULT"
pause "Confirm the above."

# -------------------------
# Pre-flight checks
# -------------------------
git_run fetch --all --tags
# We don't enforce being on 'main'; use current branch.
CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
[[ -n "$CURRENT_BRANCH" ]] || die "Cannot determine current branch."
[[ -z "$(git status --porcelain)" ]] || die "Working tree is not clean."

# Quick build to ensure green state
mvn_q -q -DskipTests -U verify

# Verify no SNAPSHOT deps (Enforcer rule)
# (You can also wire requireReleaseDeps in a profile; here we invoke ad-hoc)
mvn_q -Denforcer.skip=false -Drules=requireReleaseDeps enforcer:enforce

# Determine SCM URLs from POM
SCM_DEV_URL="$(mvn -q help:evaluate -Dexpression=project.scm.developerConnection -DforceStdout || true)"
if [[ -z "$SCM_DEV_URL" || "$SCM_DEV_URL" == "null" ]]; then
  SCM_DEV_URL="$(mvn -q help:evaluate -Dexpression=project.scm.connection -DforceStdout || true)"
fi
[[ -n "$SCM_DEV_URL" && "$SCM_DEV_URL" != "null" ]] || die "Could not read project.scm.[developer]Connection from POM."

# -------------------------
# Step 1: Set release version and <scm><tag> to final tag; commit
# -------------------------
pause "About to set project version to $RELEASE and <scm><tag> to $RELEASE_TAG."
mvn_q -q versions:set -DnewVersion="$RELEASE"
mvn_q -q versions:commit

# Stamp <scm><tag> with the final release tag for the release commit
mvn_q -q versions:set-scm-tag -DnewTag="$RELEASE_TAG"

git_run add -A
git_run commit -m "Release $RELEASE: set versions to $RELEASE and <scm><tag>=$RELEASE_TAG"

# -------------------------
# Step 2: Create & push RC tag (must be pushed before perform)
# -------------------------
pause "About to create and push RC tag $RC_TAG at current commit."
git_run tag -a "$RC_TAG" -m "Velocity $MODULE $RELEASE (RC: $RC_NUM)"
git_run push origin "refs/tags/$RC_TAG:refs/tags/$RC_TAG"

# -------------------------
# Step 3: Perform (deploy) from the RC tag
# -------------------------
# Build arguments for perform
MVN_ARGS="-Dgpg.sign=true -DskipTests"
[[ -n "$GPG_KEYNAME" ]] && MVN_ARGS="$MVN_ARGS -Dgpg.keyname=$GPG_KEYNAME"
[[ -n "$MVN_PROFILE" ]] && MVN_ARGS="$MVN_ARGS -P$MVN_PROFILE"

pause "About to run mvn release:perform from tag $RC_TAG (goals=deploy)."
mvn_q -B -e \
  -DconnectionUrl="$SCM_DEV_URL" \
  -Dtag="$RC_TAG" \
  -Dgoals="deploy" \
  -Darguments="$MVN_ARGS" \
  release:perform

# -------------------------
# Step 4: Assemble dist/dev staging folder + sha256  (SMART COPY)
# -------------------------
STAGE_DIR="target/velocity-staging/${MODULE}/${RELEASE}"
run mkdir -p "$STAGE_DIR"

# We'll scan:
#  1) the current project (root) target
#  2) submodules' targets (after release:perform, they exist under target/checkout/**/target)
#  3) include release-notes.html if present at repo root
#
# We flatten everything into $STAGE_DIR (like your dist/dev listing).
# We keep only relevant files for the release version and common packaging.
#
copy_matches() {
  local search_root="$1"
  [[ -d "$search_root" ]] || return 0
  # Find candidate files containing the release version in the name,
  # and matching common release extensions (pom, jar, sources, javadoc, zips, asc).
  # Skip Maven metadata, checksum files we will re-create, and transient files.
  while IFS= read -r -d '' f; do
    run cp -v "$f" "$STAGE_DIR/"
  done < <(find "$search_root" -type f -print0 \
      | grep -zE "/target/|/dist/|/assembly/|/package/" \
      | grep -zE "${RELEASE//./\\.}" \
      | grep -zE "\.(pom|jar|zip|tar\.gz|asc)$" \
      | grep -zEv "\.sha(1|256)$" )
}

# 1) root target
copy_matches "target"

# 2) submodules under the perform checkout (present after release:perform)
copy_matches "target/checkout"
# also scan submodules of the current workspace (useful when running without perform)
copy_matches "."

# 3) include release-notes.html if present
if [[ -f "release-notes.html" ]]; then
  run cp -v "release-notes.html" "$STAGE_DIR/"
fi

# Compute SHA-256 for every file in STAGE_DIR except existing .sha256 files.
# (This yields checksums for artifacts AND for their .asc signatures.)
if [[ -d "$STAGE_DIR" ]]; then
  (
    cd "$STAGE_DIR"
    for fileitem in *; do
      [[ -f "$fileitem" ]] || continue
      [[ "$fileitem" =~ \.sha256$ ]] && continue
      run "sha256sum \"$fileitem\" > \"$fileitem.sha256\""
    done
  )
fi

echo
echo "===================================================================="
echo "Dist/dev staging assembled in:"
echo "  $STAGE_DIR"
echo
echo "Now copy the contents of that directory to your local checkout of:"
echo "  https://dist.apache.org/repos/dist/dev/velocity/${MODULE}/${RELEASE}/"
echo "Then: svn add/commit there."
echo "===================================================================="
echo

# -------------------------
# Step 5: Generate vote email skeleton
# -------------------------
VOTE_FILE="target/${MODULE}-${RELEASE}-rc-vote.txt"
STAGING_TODO_URL="(paste Nexus staging repo URL here after deploy)"
cat > "$VOTE_FILE" <<EOF
Subject: [VOTE] Release Apache Velocity ${MODULE^} $RELEASE ($RC_TAG)

Hi all,

This is a vote to release Apache Velocity ${MODULE^} $RELEASE, RC: $RC_TAG.

Artifacts have been staged in Nexus:
  $STAGING_TODO_URL

The dist/dev artifacts to verify (source & binary) are here (after you copy them):
  https://dist.apache.org/repos/dist/dev/velocity/${MODULE}/${RELEASE}/

Please vote on the release:
  [ ] +1 Release the artifacts as Apache Velocity ${MODULE^} $RELEASE
  [ ]  0 No opinion
  [ ] -1 Do not release (please state why)

Vote will be open for at least 72 hours.

Checklist:
- [ ] Check signatures (*.asc) and hashes (*.sha256)
- [ ] Build from source
- [ ] Verify LICENSE/NOTICE
- [ ] Sanity check artifacts (pom, javadoc, sources, binaries)
- [ ] Compare tag $RC_TAG against sources

Tag to verify:
  $RC_TAG

Thanks,
(Your Name)
EOF

echo "Vote email skeleton generated at: $VOTE_FILE"

# -------------------------
# Step 6: Bump to NEXT dev version and reset <scm><tag>=HEAD; commit
# -------------------------
read -r -p "Enter NEXT development version [default: $NEXT_DEFAULT]: " NEXT_IN
NEXT="${NEXT_IN:-$NEXT_DEFAULT}"

pause "About to bump to $NEXT and reset <scm><tag>=HEAD."
mvn_q -q versions:set -DnewVersion="$NEXT"
mvn_q -q versions:commit
mvn_q -q versions:set-scm-tag -DnewTag=HEAD

git_run add -A
git_run commit -m "Start $NEXT (reset <scm><tag>=HEAD)"

# Push branch changes (optional; uncomment to push automatically)
git_run push origin "$CURRENT_BRANCH" || true

echo "Done."
