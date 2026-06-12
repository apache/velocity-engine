Title: Apache Velocity Spring Support

# Apache Velocity Spring Support

This module is the current, Jakarta-based variant of the engine support initially hosted by the Spring project in its 4.x versions. It targets Java 17+ and works with both Spring 6.x and Spring 7.x (it is compiled against the Spring 6 baseline and relies only on stable `spring-core`/`spring-beans`/`spring-context` APIs that are unchanged across both).

For legacy Spring 5.x integration on Java 8, use the sibling `spring5-velocity-support` module instead.

Example configuration:

```xml
<bean id="velocityEngine"
    class="org.apache.velocity.spring.VelocityEngineFactoryBean">
    <property name="velocityProperties">
        <props>
            <prop key="resource.loaders">classpath</prop>
            <prop key="resource.loader.classpath.class">
                org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
            </prop>
        </props>
    </property>
</bean>
```

