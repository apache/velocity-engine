--------
Velocity
--------

Welcome to Velocity. Velocity is a general purpose template engine
written in Java. For more information about Velocity, please look at the
HTML documentation in the docs/ directory.

Here is a description of what each of the top level directories
contains. Please consult the documentation in each of the lower level
directories for information that is specific to their contents.

bin/        This is a temporary build directory.
build/      This is where the build scripts live.
docs/       This is where the documentation lives.
examples/   This is where the examples live.
src/        This is where all of the source code to Velocity lives.
test/       This is where all of the tests live.

REQUIREMENTS
------------

The Java 2 SDK is required to build Velocity.

BUILDING VELOCITY
-----------------

In order to get started with Velocity, you must first build it. To do
so, cd into the build/ directory and execute the platform-specific build
script.

Unix:
./build-velocity.sh

Win32:
build-velocity.bat

This will create a bin/ directory containing the Velocity .jar file. Be
sure to update your classpath to include Velocity's .jar file.

TRYING THE EXAMPLE
------------------

After building Velocity cd into the examples/ directory and run test.sh.
This will generate a file named output which is the result of the
test.wm input.

-The Velocity Team
