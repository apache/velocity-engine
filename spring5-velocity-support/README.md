Title: Apache Velocity Spring 5 Support

# Apache Velocity Spring 5 Support

This module is the legacy Spring 5.x variant (Java 8) of the engine support initially hosted by the Spring project in its 4.x versions.

For current Spring (6.x/7.x on Java 17), use the sibling `spring-velocity-support` module instead.

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

