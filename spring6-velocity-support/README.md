Title: Apache Velocity Spring 6 Support

# Apache Velocity Spring 6 Support

This module is the Spring 6.x variant of the engine support initially hosted by the Spring project in its 4.x versions. It targets Java 17+.

For Spring 5.x integration on Java 8, use the sibling `spring-velocity-support` module instead.

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

