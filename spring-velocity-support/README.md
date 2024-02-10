Title: Apache Velocity Spring Support

# Apache Velocity Spring Support

This module is an adaptation of the engine support initially hosted by the Spring project in its 4.x versions.

Example configuration:

```xml
<bean id="velocityEngine"
    class="org.apache.velocity.spring.VelocityEngineFactoryBean">
    <property name="velocityProperties">
        <props>
            <prop key="resource.loaders">class</prop>
            <prop key="class.resource.loader.class">
                org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
            </prop>
        </props>
    </property>
</bean>
```

