--------
Velocity
--------

Welcome to Velocity. Velocity is a general purpose template engine
written in Java. For more information about Velocity, please look at
the HTML documentation in the docs/ directory, as well as the Velocity
web site

  http://velocity.apache.org/index.html

Here is a description of what each of the top level directories
contains. Please consult the documentation in each of the lower level
directories for information that is specific to their contents.

bin/        This is a temporary build directory.
build/      This is where the build scripts live.
contrib/    The home for source code which is not officially maintained.
convert/    The WebMacro to Velocity conversion program.
docs/       This is where the documentation lives.
examples/   This is where the examples live.
src/        This is where all of the source code to Velocity lives.
test/       This is where all of the tests live.
whiteboard/ The place for developer experimentation (unmaintained).
xdocs/      This is the .xml files for building the .html files
            related to the website and documentation.

            
REQUIREMENTS
------------

Velocity will run with any version of Java greater than 1.3.  Building
Velocity requires Java version 1.4 (or greater) and ant 1.6 or
greater.


INCLUDED PRE-BUILT JARS
-----------------------

If you are using an offical Velocity release distribution, you will
find two pre-built Velocity jars in the top level directory.

1) velocity-<version>.jar: 
    The Velocity jar that does not include any external dependencies
    needed by Velocity, such as the jakarta-commons-collection
    classes, Jakarta Avalon Logkit, or Jakarta ORO. We do this to
    allow you to use whatever version of collections, logkit, etc that
    you wish w/o fear of collision.  These jars are included in the
    distribution, in the build/lib directory, or at the respective
    project sites.

2) velocity-dep-<version>.jar:
    This is a Velocity jar that includes all dependencies that were
    included with previous distribution jars.  It is intended as a
    convenience to allow you to drop this distribution in place of
    existing 1.1 or 1.2-dep distributions.

Please see the WHY_TWO_JARS.txt file for more information.


BUILDING VELOCITY
-----------------

In order to use the latest version of Velocity, you may want to build
it.

Building Velocity is easy.  All components necessary to build Velocity
are included or get downloaded from the internet during the build,
except for the Java 2 SDK and the fabulous Ant build tool.  You can
find details on how to build Velocity online at:

http://velocity.apache.org/engine/devel/build.html

Note that you must use Ant version 1.6 or later.

To build Velocity's jar, change directory into the build/ directory
and enter:

ant jar

This will create a bin/ directory containing the Velocity .jar
file. Be sure to update your classpath to include Velocity's .jar
file, or when using a modern servlet container, put it in the
WEB-INF/lib directory.

If you wish to build a Velocity jar that contains all dependencies, we
have provided an optional build target for our convenience :

ant jar-dep

This will build a complete Velocity jar with dependencies included,
and it can be found in the /bin directory as

velocity-dep-<version>.jar


TRYING THE EXAMPLES
-------------------

After building Velocity, you can also buld the examples that are
included with the Velocity distribution.  These examples show how to
use Velocity in your Java applications.  There also are examples of
how to use Anakia, a XML transformation engine.

For more information, please see the README.txt in the examples/
directory.

-The Velocity Team
