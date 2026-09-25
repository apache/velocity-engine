Title: Apache Velocity Engine

# Apache Velocity

Welcome to Apache Velocity Engine! Apache Velocity is a general purpose
template engine written in Java. For more information about Velocity,
please look at the HTML documentation on the [Velocity web site](https://velocity.apache.org/index.html).

Here's a description of the top level directories:

    velocity-engine-core/       The Velocity Engine core module
    velocity-engine-examples/   Several simple examples
    velocity-engine-scripting/  JSR-223 implementation for Velocity scripting
    spring-velocity-support     Velocity Engine factory bean for current Spring (6.x/7.x, Java 17)
    spring5-velocity-support    Velocity Engine factory bean for legacy Spring 5.x (Java 8)
    src/                        Source for parent modules, mainly changelog

Apache Velocity requires at least Java 8 to run.

## CUSTOMIZING THE PARSER

It is possible to [build a custom parser](https://velocity.apache.org/engine/2.5/developer-guide.html#customizing-the-vtl-parser), to change some of the characters used by the VTL syntax: `*`, `@` and `#`.

Let's say you want to generate Markdown, where `#` starts a heading: you can for instance build your own parser which will use the `%` character as directives prefix instead of `#`.

The `$` character can also be changed, but this is deprecated since 2.5: the next major version does not allow it.

## TRYING THE EXAMPLES

After building Velocity, you can also build the examples that are
included with the Velocity distribution. These examples show how to
use Velocity in your Java applications.

For more information, please see the [examples README](velocity-engine-examples) in the `velocity-engine-examples` directory.

___

- The Apache Velocity Team

