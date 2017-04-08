Title: Apache Velocity

# Apache Velocity

Welcome to Apache Velocity 2.0! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation on the Velocity web site:

  [http://velocity.apache.org/index.html](http://velocity.apache.org/index.html)

Here's a description of the top level directories:

    velocity-engine-core/       The Velocity Engine core module
    velocity-engine-examples/   Several simple examples
    velocity-engine-scripting/  JSR-223 implementation for Velocity scripting
    src/                        Source for parent modules, mainly changelog

## REQUIREMENTS

Apache Velocity will run with any Java runtime engine v1.7 or greater.

Building from source requires Java development kit v1.7 or greater and Maven 3 (3.0.5+).

At compile time, Maven should fetch all needed dependencies, which are:
* commons-lang v3.5
* slf4j-api v1.7.25
plus the following ones, needed for the integrated tests:
* slf4j-simple v1.7.25
* junit v4.12
* hsqldb v2.3.4
* commons-io 2.5

At runtime, Velocity only needs:
* commons-lang v3.5+
* slf4j-api and an slf4j binding, v1.7.x

## UPGRADING FROM EARLIER RELEASES

Release with the same major number are intended to be drop-in
replacements. However, in most cases the versions of dependency jars
must be adjusted because newer versions of Velocity might require
updates.

### Upgrading from Velocity 1.7.x to Velocity 2.0

#### Behavior / API changes:

* velocity is now using the SLF4J logging facade. Hence, all methods accepting
or returning a logger now use the org.slf4j.Logger object. Velocity uses a
logger name of org.apache.velocity (configurable with the runtime.log.name
configuration entry), and several other children loggers.
* the internal Context API now enforces String keys everywhere, this may break
custom Context implementations at compile-time.
* invalid reference events are now more sparsely sent; they're not sent if any
of the following conditions is met (the 1.x behavior did send invalid
reference events in all those cases):
  + the reference is a quiet reference
  + the reference could be successfully evaluated but resulted in a null value
  + the reference is tested for validity inside an #if / #elseif statement
* all events do now receive the current Velocity Context as a first argument.
The signatures of the MethodExceptionEventHandler,
ReferenceInsertionEventHandler and IncludeEventHandler events have changed,
and the ContextAware interface has been suppressed, as long as the
NullSetEventHandler event which is obsolete.
* The ResourceLoader class API has replaced InputStream getters by Reader
getters: InputStream ResourceLoader.getResourceStream(String name) has been
replaced by a Reader ResourceLoader.getResourceReader(String name, String
encoding).
* the default encoding ('ISO-8859-1' in 1.x) is now UTF-8.
* the MethodException event handler now receives an additional argument
providing template name and location infos.
* Initialization methods in Velocity and VelocityEngine taking an
ExtendedProperties have been removed (but setProperties(Properties) methods
are still here). All occurrences of the
org.apache.commons.collections.ExtendedProperties class in the runtime
internal initialization API have been replaced by
org.apache.velocity.util.ExtProperties.
* the macros are now using a 'call by sharing' convention (which means that
all arguments are evaluated once at start, and that the macro receives a
copy of the reference to each argument).
* the UberspectLoggable interface has been removed.

#### VTL Changes:

* the hypen ( - ) cannot be used in variable names anymore
* method arguments can be arithmetic expressions
* method arguments are now converted as needed between all main basic Java
standard types (booleans, numbers and strings). If you want to revert to
the 1.x behavior, set the property runtime.conversion.handler = none.
* space gobbling (to control the indentation of generated code) is now
configurable via the space.gobbing configuration key, which can take the
following values: none, bc (aka. backward compatible), lines and structured.
See the related documentation section for details. To maximize backward
compatibility with 1.x, set it to bc.

#### Dependencies changes:

* Velocity now requires a JDK version of 1.7 or higher.
* commons-collections and commons-logging aren't needed any more at runtime.
* there's a new compile-time and runtime dependency, slf4j-api 1.7.12.
* you'll need an SLF4J binding.
* commons-lang has to be upgraded to 3.5.

### Upgrading from Velocity 1.6.x to Velocity 1.7.x

There are no changes in the dependencies since Velocity 1.6

* Deprecated $velocityCount; please use $foreach.count or $foreach.index
* Deprecated $velocityHasNext; please use $foreach.hasNext, $foreach.first or $foreach.last
* Deprecated velocimacro.context.localscope setting; please get/set local #macro references
as members of the provided $macro scope control instead. (e.g. #set( $macro.foo = 'bar' )
and $macro.foo )
* Deprecated directive.evaluate.context.class setting; please get/set local #evaluate
references as members of the provided $evaluate scope control instead. (e.g. #set( $evaluate.foo = 'bar' ) and $evaluate.foo )
* Deprecated #literal directive; please use #[[this syntax]]# instead.
* Changed #stop to end template rendering rather than template parsing.
* Removed obsolete Veltag (use VelocityViewTag in VelocityTools project)
* Removed obsolete WebMacro conversion code.

### Upgrading from Velocity 1.5.x to Velocity 1.6.x

* Commons Collections has been upgraded to version 3.2.1.
* Commons Lang has been upgraded to version 2.4.
* Commons Logging 1.1 has been added.

Optional:

* Maven Ant 2.0.9 is required for the Maven Ant tasks.
* Java CC 4.1 is recommended to compile the parser files.

### Upgrading from Velocity 1.4.x or earlier to Velocity 1.5.x

* JDOM has been upgraded to version 1.0.
* Commons Collections has been upgraded to version 3.1.
* Commons Lang 2.1 has been added.

Optional:

* Apache Ant 1.6 or better is required for rebuilding.
* Java CC 3.2 is recommended to compile the parser files.
* HSQLDB 1.7.1 is required for running unit tests.


## BUILDING APACHE VELOCITY

In order to use the latest version of Apache Velocity, you may want to
build it.

Building is easy.  All components necessary to build are included or
get downloaded from the internet during the build, except for the Java
 SDK and the Maven build tool.  You can find details on how to build
Velocity online at:

[http://velocity.apache.org/engine/devel/build.html](http://velocity.apache.org/engine/devel/build.html)

*IMPORTANT* As the Apache Velocity build process wants to download a
number of jars from the internet, you must be online when you are
building for the first time.

To build Velocity's jar, just run maven using the command:

    mvn

This will create a target/ directory containing the Velocity .jar
file in each sub-module directory.

Be sure to update your classpath to include Velocity's .jar
file, or when using a modern servlet container, put it in the
WEB-INF/lib directory.

## TRYING THE EXAMPLES

After building Velocity, you can also build the examples that are
included with the Velocity distribution. These examples show how to
use Velocity in your Java applications.

For more information, please see the README.txt in the
velocity-engine-examples/src/etc/ directory.

- The Apache Velocity Team
