#!/usr/bin/env bash
# release.sh — prepare + perform an Apache Velocity Engine release candidate
#
#   ./release.sh <RELEASE_VERSION> <RC_NUMBER> [--dry-run] [--gpg-keyname <KEYID>] [--profile <MVN_PROFILE>]
#
# Sets the release version, commits, pushes the RC tag, deploys from it (release:perform, tests
# skipped), assembles the dist/dev folder with *.sha256, writes a vote email skeleton, then bumps
# to the next snapshot version.
#
# Requirements:
# - ~/.m2/settings.xml: credentials for server apache.releases.https, and a profile (default
#   apache-release-key) defining gpg.keyname, unless --gpg-keyname is given.
# - MAVEN_GPG_PASSPHRASE exported, or typed when asked: maven-gpg-plugin 3.x ignores
#   gpg.passphrase, and switches gpg to loopback pinentry itself when it has a passphrase.

set -euo pipefail

MODULE=engine
DRY_RUN=0
MVN_PROFILE="${MVN_PROFILE:-apache-release-key}"
GPG_KEYNAME="${GPG_KEYNAME:-}"

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
  $0 2.5 RC3
  $0 2.5 RC3 --gpg-keyname BEFEEF227A98B809

Options:
  --dry-run            Show what would happen; do not mutate git or run deploys.
  --gpg-keyname K      GPG key id (default: gpg.keyname of the settings.xml profile below).
  --profile P          settings.xml profile defining gpg.keyname (default: apache-release-key).
  --help               Print this help.
EOF
}

# -------------------------
# Arguments
# -------------------------
if [[ "${1:-}" == "--help" || $# -lt 2 ]]; then usage; exit 0; fi

RELEASE="$1"; shift
RC_NUM_RAW="$1"; shift
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

RELEASE_TAG="$RELEASE"            # e.g. 2.5
RC_TAG="${RELEASE}-${RC_NUM}"     # e.g. 2.5-RC1
DIST_DIR="velocity-${MODULE}/${RELEASE}"
DIST_DEV_URL="https://dist.apache.org/repos/dist/dev/velocity/${DIST_DIR}/"

# next minor snapshot: 2.4 -> 2.5-SNAPSHOT
compute_next_version() {
  local v="$1"
  if [[ "$v" =~ ^([0-9]+)\.([0-9]+)(\.([0-9]+))?$ ]]; then
    echo "${BASH_REMATCH[1]}.$((BASH_REMATCH[2]+1))-SNAPSHOT"
  else
    echo "${v}-SNAPSHOT"
  fi
}
NEXT_DEFAULT="$(compute_next_version "$RELEASE")"

# The deploy build runs in a fresh checkout where file-activated settings profiles stay off, and
# gpg's fallback is the first key of the keyring: the key is always resolved and passed explicitly.
if [[ -z "$GPG_KEYNAME" ]]; then
  GPG_KEYNAME="$(mvn -q help:evaluate -Dexpression=gpg.keyname -DforceStdout -P"$MVN_PROFILE" 2>/dev/null || true)"
  [[ -n "$GPG_KEYNAME" && "$GPG_KEYNAME" != null* ]] \
    || die "No gpg.keyname in settings.xml profile '$MVN_PROFILE'; use --profile or --gpg-keyname."
fi

echo "Planned versions/tags:"
echo "  RELEASE       : $RELEASE"
echo "  RC TAG        : $RC_TAG"
echo "  FINAL TAG     : $RELEASE_TAG"
echo "  NEXT (default): $NEXT_DEFAULT"
echo "  GPG KEY       : $GPG_KEYNAME"
pause "Confirm the above."

# -------------------------
# Pre-flight
# -------------------------
git_run fetch --all --tags
CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
[[ -n "$CURRENT_BRANCH" ]] || die "Cannot determine current branch."
[[ -z "$(git status --porcelain)" ]] || die "Working tree is not clean."

# Signing self-test with the very key and passphrase of the deploy, before any tag is pushed.
# Read-only, so it also runs in --dry-run.
if [[ -z "${MAVEN_GPG_PASSPHRASE:-}" ]]; then
  read -rsp "GPG passphrase (key $GPG_KEYNAME): " MAVEN_GPG_PASSPHRASE; echo
  [[ -n "$MAVEN_GPG_PASSPHRASE" ]] || die "A GPG passphrase is required to sign the release."
fi
export MAVEN_GPG_PASSPHRASE
echo "release.sh signing self-test" \
  | gpg --batch --yes --pinentry-mode loopback --passphrase "$MAVEN_GPG_PASSPHRASE" \
        --local-user "$GPG_KEYNAME" --detach-sign -o /dev/null \
  || die "GPG signing self-test failed for key $GPG_KEYNAME: check the passphrase."
echo "GPG signing self-test OK."

echo "Pre-flight build (tests skipped)..."
mvn -q -DskipTests -U verify

SCM_DEV_URL="$(mvn -q help:evaluate -Dexpression=project.scm.developerConnection -DforceStdout || true)"
if [[ -z "$SCM_DEV_URL" || "$SCM_DEV_URL" == "null" ]]; then
  SCM_DEV_URL="$(mvn -q help:evaluate -Dexpression=project.scm.connection -DforceStdout || true)"
fi
[[ -n "$SCM_DEV_URL" && "$SCM_DEV_URL" != "null" ]] || die "Could not read project.scm.[developer]Connection from POM."

# -------------------------
# Step 1: release version and <scm><tag>; commit
# -------------------------
pause "About to set project version to $RELEASE and <scm><tag> to $RELEASE_TAG."
mvn_q -q versions:set -DnewVersion="$RELEASE"
mvn_q -Denforcer.skip=false -Drules=requireReleaseDeps enforcer:enforce
mvn_q -q versions:commit
mvn_q -q versions:set-scm-tag -DnewTag="$RELEASE_TAG"

git_run add -A
git_run commit -m "Release $RELEASE: set versions to $RELEASE and <scm><tag>=$RELEASE_TAG"

# -------------------------
# Step 2: RC tag (release:perform checks it out from the remote)
# -------------------------
pause "About to create and push RC tag $RC_TAG at current commit."
git_run tag -a "$RC_TAG" -m "Velocity $MODULE $RELEASE (RC: $RC_NUM)"
git_run push origin "refs/tags/$RC_TAG:refs/tags/$RC_TAG"

# -------------------------
# Step 3: deploy from the RC tag
# -------------------------
if ! is_dry; then
  cat > release.properties <<EOF
scm.url=${SCM_DEV_URL}
scm.tag=${RC_TAG}
EOF
fi

# the ASF parent's release plugin configuration activates the apache-release profile itself
MVN_ARGS="-Dgpg.sign=true -DskipTests -Dgpg.keyname=$GPG_KEYNAME"

pause "About to run mvn release:perform from tag $RC_TAG (goals=deploy)."
mvn_q -B -e \
  -Dgoals="deploy" \
  -Darguments="$MVN_ARGS" \
  release:perform

run "rm -f release.properties"

# -------------------------
# Step 4: dist/dev folder + sha256
# -------------------------
STAGE_DIR="target/velocity-staging/${DIST_DIR}"
run mkdir -p "$STAGE_DIR"

# release artifacts and signatures of this version, flattened
copy_matches() {
  local search_root="$1"
  [[ -d "$search_root" ]] || return 0
  while IFS= read -r -d '' f; do
    run cp -v "$f" "$STAGE_DIR/"
  done < <(find "$search_root" -type f -not -path "*/velocity-staging/*" -print0 \
      | grep -zE "/target/|/dist/|/assembly/|/package/" \
      | grep -zE -- "-${RELEASE//./\\.}[.-]" \
      | grep -zEv "SNAPSHOT" \
      | grep -zE "\.(pom|jar|zip|tar\.gz|asc)$" \
      | grep -zEv "\.sha(1|256)$" )
}

copy_matches "target"
copy_matches "target/checkout"   # modules built by release:perform
copy_matches "."

if [[ -f "release-notes.html" ]]; then
  run cp -v "release-notes.html" "$STAGE_DIR/"
fi

# checksums for artifacts and signatures alike
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
echo "  $DIST_DEV_URL"
echo "Then: svn add/commit there."
echo "===================================================================="
echo

# -------------------------
# Step 5: vote email skeleton
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
  $DIST_DEV_URL

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
# Step 6: next snapshot version, <scm><tag>=HEAD; commit and push the branch
# -------------------------
read -r -p "Enter NEXT development version [default: $NEXT_DEFAULT]: " NEXT_IN
NEXT="${NEXT_IN:-$NEXT_DEFAULT}"

pause "About to bump to $NEXT and reset <scm><tag>=HEAD."
mvn_q -q versions:set -DnewVersion="$NEXT"
mvn_q -q versions:commit
mvn_q -q versions:set-scm-tag -DnewTag=HEAD

git_run add -A
git_run commit -m "Start $NEXT (reset <scm><tag>=HEAD)"
git_run push origin "$CURRENT_BRANCH" || true

echo "Done."
