Title: Apache Velocity Engine

# Apache Velocity

Welcome to Apache Velocity Engine! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation on the [Velocity web site](https://velocity.apache.org/index.html).

Here's a description of the top level directories:

    velocity-engine-core/       The Velocity Engine core module
    velocity-engine-examples/   Several simple examples
    velocity-engine-scripting/  JSR-223 implementation for Velocity scripting
    spring-velocity-support     Velocity Engine factory bean for Spring framework
    src/                        Source for parent modules, mainly changelog

Apache Velocity requires at least Java 8 to run.

## CUSTOMIZING THE PARSER

It is possible to [build a custom parser](https://velocity.apache.org/engine/2.3/developer-guide.html#customizing-the-vtl-parser), to change some of the characters used by the VTL syntax: `*`, `@`, `$` and `#`.

Let's say you want to merge some templatized jQuery code full of `$` characters, you can for instance build you own parser which will use the `§` character as references prefix instead of `$`.

## TRYING THE EXAMPLES

After building Velocity, you can also build the examples that are
included with the Velocity distribution. These examples show how to
use Velocity in your Java applications.

For more information, please see the [examples README](velocity-engine-examples) in the `velocity-engine-examples` directory.

___

- The Apache Velocity Team

