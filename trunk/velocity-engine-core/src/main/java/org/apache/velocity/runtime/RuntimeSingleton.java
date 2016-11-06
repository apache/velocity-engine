package org.apache.velocity.runtime;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.Macro;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.ContentResource;
import org.apache.velocity.util.ExtProperties;
import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.Uberspect;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.List;
import java.util.Properties;

/**
 * This is the Runtime system for Velocity. It is the
 * single access point for all functionality in Velocity.
 * It adheres to the mediator pattern and is the only
 * structure that developers need to be familiar with
 * in order to get Velocity to perform.
 *
 * The Runtime will also cooperate with external
 * systems, which can make all needed setProperty() calls
 * before calling init().
 *
 * -----------------------------------------------------------------------
 * N O T E S  O N  R U N T I M E  I N I T I A L I Z A T I O N
 * -----------------------------------------------------------------------
 * RuntimeSingleton.init()
 *
 * If Runtime.init() is called by itself the Runtime will
 * initialize with a set of default values.
 * -----------------------------------------------------------------------
 * RuntimeSingleton.init(String/Properties)
 *
 * In this case the default velocity properties are layed down
 * first to provide a solid base, then any properties provided
 * in the given properties object will override the corresponding
 * default property.
 * -----------------------------------------------------------------------
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jlb@houseofdistraction.com">Jeff Bowden</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 *
 * @see org.apache.velocity.runtime.RuntimeInstance
 *
 * @version $Id$
 */
public class RuntimeSingleton implements RuntimeConstants
{
    private static RuntimeInstance ri = new RuntimeInstance();

    /**
     * This is the primary initialization method in the Velocity
     * Runtime. The systems that are setup/initialized here are
     * as follows:
     *
     * <ul>
     *   <li>Logging System</li>
     *   <li>ResourceManager</li>
     *   <li>Event Handlers</li>
     *   <li>Parser Pool</li>
     *   <li>Global Cache</li>
     *   <li>Static Content Include System</li>
     *   <li>Velocimacro System</li>
     * </ul>
     * @see RuntimeInstance#init()
     */
    public synchronized static void init()
    {
        ri.init();
    }

    /**
     * Resets the instance, so Velocity can be re-initialized again.
     *
     * @since 2.0.0
     */
    public synchronized static void reset()
    {
        ri.reset();
    }

    /**
     * Returns true if the RuntimeInstance has been successfully initialized.
     * @return True if the RuntimeInstance has been successfully initialized.
     * @see RuntimeInstance#isInitialized()
     * @since 1.5
     */
    public static boolean isInitialized()
    {
        return ri.isInitialized();
    }

    /**
     * Returns the RuntimeServices Instance used by this wrapper.
     *
     * @return The RuntimeServices Instance used by this wrapper.
     */
    public static RuntimeServices getRuntimeServices()
    {
        return ri;
    }


    /**
     * Allows an external system to set a property in
     * the Velocity Runtime.
     *
     * @param key property key
     * @param  value property value
     * @see RuntimeInstance#setProperty(String, Object)
     */
    public static void setProperty(String key, Object value)
    {
        ri.setProperty(key, value);
    }

    /**
     * Allow an external system to set an ExtendedProperties
     * object to use. This is useful where the external
     * system also uses the ExtendedProperties class and
     * the velocity configuration is a subset of
     * parent application's configuration. This is
     * the case with Turbine.
     *
     * @param configuration
     * @deprecated use {@link #setConfiguration(ExtProperties)}
     * @see RuntimeInstance#setConfiguration(ExtendedProperties)
     */
    public @Deprecated static void setConfiguration( ExtendedProperties configuration)
    {
        ri.setConfiguration(configuration);
    }

    /**
     * Allow an external system to set an ExtProperties
     * object to use. This is useful where the external
     * system also uses the ExtProperties class and
     * the velocity configuration is a subset of
     * parent application's configuration. This is
     * the case with Turbine.
     *
     * @param configuration
     * @see RuntimeInstance#setConfiguration(ExtProperties)
     */
    public static void setConfiguration( ExtProperties configuration)
    {
        ri.setConfiguration(configuration);
    }

    /**
     * Add a property to the configuration. If it already
     * exists then the value stated here will be added
     * to the configuration entry. For example, if
     *
     * resource.loader = file
     *
     * is already present in the configuration and you
     *
     * addProperty("resource.loader", "classpath")
     *
     * Then you will end up with a Vector like the
     * following:
     *
     * ["file", "classpath"]
     *
     * @param key
     * @param value
     * @see RuntimeInstance#addProperty(String, Object)
     */
    public static void addProperty(String key, Object value)
    {
        ri.addProperty(key, value);
    }

    /**
     * Clear the values pertaining to a particular
     * property.
     *
     * @param key of property to clear
     * @see RuntimeInstance#clearProperty(String)
     */
    public static void clearProperty(String key)
    {
        ri.clearProperty( key );
    }

    /**
     *  Allows an external caller to get a property.  The calling
     *  routine is required to know the type, as this routine
     *  will return an Object, as that is what properties can be.
     *
     *  @param key property to return
     *  @return Value of the property or null if it does not exist.
     * @see RuntimeInstance#getProperty(String)
     */
    public static Object getProperty( String key )
    {
        return ri.getProperty( key );
    }

    /**
     * Initialize the Velocity Runtime with a Properties
     * object.
     *
     * @param p
     * @see RuntimeInstance#init(Properties)
     */
    public static void init(Properties p)
    {
        ri.init(p);
    }

    /**
     * Initialize the Velocity Runtime with the name of
     * ExtProperties object.
     *
     * @param configurationFile
     * @see RuntimeInstance#init(String)
     */
    public static void init(String configurationFile)
    {
        ri.init( configurationFile );
    }

    /**
     * Parse the input and return the root of
     * AST node structure.
     * <br><br>
     *  In the event that it runs out of parsers in the
     *  pool, it will create and let them be GC'd
     *  dynamically, logging that it has to do that.  This
     *  is considered an exceptional condition.  It is
     *  expected that the user will set the
     *  PARSER_POOL_SIZE property appropriately for their
     *  application.  We will revisit this.
     *
     * @param reader Reader retrieved by a resource loader
     * @param template Template being parsed
     * @return A root node representing the template as an AST tree.
     * @throws ParseException When the template could not be parsed.
     * @see RuntimeInstance#parse(Reader, Template)
     */
    public static SimpleNode parse( Reader reader, Template template )
        throws ParseException
    {
        return ri.parse(reader, template);
    }

    /**
     * Returns a <code>Template</code> from the resource manager.
     * This method assumes that the character encoding of the
     * template is set by the <code>input.encoding</code>
     * property. The default is platform dependant.
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @see RuntimeInstance#getTemplate(String)
     */
    public static Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException
    {
        return ri.getTemplate(name);
    }

    /**
     * Returns a <code>Template</code> from the resource manager
     *
     * @param name The  name of the desired template.
     * @param encoding Character encoding of the template
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @see RuntimeInstance#getTemplate(String, String)
     */
    public static Template getTemplate(String name, String  encoding)
        throws ResourceNotFoundException, ParseErrorException
    {
        return ri.getTemplate(name, encoding);
    }

    /**
     * Returns a static content resource from the
     * resource manager.  Uses the current value
     * if INPUT_ENCODING as the character encoding.
     *
     * @param name Name of content resource to get
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException When the template could not be parsed.
     * @see RuntimeInstance#getContent(String)
     */
    public static ContentResource getContent(String name)
        throws ResourceNotFoundException, ParseErrorException
    {
        return ri.getContent(name);
    }

    /**
     * Returns a static content resource from the
     * resource manager.
     *
     * @param name Name of content resource to get
     * @param encoding Character encoding to use
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException When the template could not be parsed.
     * @see RuntimeInstance#getContent(String, String)
     */
    public static ContentResource getContent( String name, String encoding )
        throws ResourceNotFoundException, ParseErrorException
    {
        return ri.getContent(name, encoding);
    }


    /**
     *  Determines is a template exists, and returns name of the loader that
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.templateExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     * @see RuntimeInstance#getLoaderNameForResource(String)
     */
    public static String getLoaderNameForResource( String resourceName )
    {
        return ri.getLoaderNameForResource(resourceName);
    }


    /**
     * Returns a convenient Log instance that wraps the current LogChute.
     *
     * @return A convenience Log instance that wraps the current LogChute.
     * @see RuntimeInstance#getLog()
     * @since 1.5
     */
    public static Logger getLog()
    {
        return ri.getLog();
    }

    /**
     * String property accessor method with default to hide the
     * configuration implementation.
     *
     * @param key property key
     * @param defaultValue  default value to return if key not
     *               found in resource manager.
     * @return value of key or default
     * @see RuntimeInstance#getString(String, String)
     */
    public static String getString( String key, String defaultValue)
    {
        return ri.getString(key, defaultValue);
    }

    /**
     * Returns the appropriate VelocimacroProxy object if strVMname
     * is a valid current Velocimacro.
     *
     * @param vmName Name of velocimacro requested
     * @param templateName Name of the template that contains the velocimacro.
     * @return The requested VelocimacroProxy.
     * @see RuntimeInstance#getVelocimacro(String, String)
     */

    /**
     * Returns the appropriate VelocimacroProxy object if strVMname
     * is a valid current Velocimacro.
     *
     * @param vmName  Name of velocimacro requested
     * @param renderingTemplate Template we are currently rendering. This
     *    information is needed when VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL setting is true
     *    and template contains a macro with the same name as the global macro library.
     * @param template current template
     *
     * @return VelocimacroProxy
     */
    public static Directive getVelocimacro(String vmName, Template renderingTemplate, Template template)
    {
        return ri.getVelocimacro(vmName, renderingTemplate, template);
    }

    /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     *
     * @param name  Name of a new velocimacro.
     * @param macro  root AST node of the parsed macro
     * @param macroArgs  Array of macro arguments, containing the
     *        #macro() arguments and default values.  the 0th is the name.
     * @param definingTemplate Templaite containing the definition of the macro.
     */
    public static boolean addVelocimacro(String name, Node macro,
                                         List<Macro.MacroArg> macroArgs, Template definingTemplate)
    {
        return ri.addVelocimacro(name, macro, macroArgs, definingTemplate);
    }

    /**
     *  Checks to see if a VM exists
     *
     * @param vmName Name of the Velocimacro.
     * @param template Template on which to look for the Macro.
     * @return True if VM by that name exists, false if not
     */
    public static boolean isVelocimacro(String vmName, Template template)
    {
        return ri.isVelocimacro(vmName, template);
    }

    /* --------------------------------------------------------------------
     * R U N T I M E  A C C E S S O R  M E T H O D S
     * --------------------------------------------------------------------
     * These are the getXXX() methods that are a simple wrapper
     * around the configuration object. This is an attempt
     * to make a the Velocity Runtime the single access point
     * for all things Velocity, and allow the Runtime to
     * adhere as closely as possible the the Mediator pattern
     * which is the ultimate goal.
     * --------------------------------------------------------------------
     */

    /**
     * String property accessor method to hide the configuration implementation
     * @param key  property key
     * @return   value of key or null
     * @see RuntimeInstance#getString(String)
     */
    public static String getString(String key)
    {
        return ri.getString( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key Property key
     * @return value
     * @see RuntimeInstance#getInt(String)
     */
    public static int getInt( String key )
    {
        return ri.getInt( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key  property key
     * @param defaultValue The default value.
     * @return value
     * @see RuntimeInstance#getInt(String, int)
     */
    public static int getInt( String key, int defaultValue )
    {
        return ri.getInt( key, defaultValue );
    }

    /**
     * Boolean property accessor method to hide the configuration implementation.
     *
     * @param key property key
     * @param def The default value if property not found.
     * @return value of key or default value
     * @see RuntimeInstance#getBoolean(String, boolean)
     */
    public static boolean getBoolean( String key, boolean def )
    {
        return ri.getBoolean( key, def );
    }

    /**
     * Return the velocity runtime configuration object.
     *
     * @return ExtProperties configuration object which houses
     *                       the velocity runtime properties.
     * @see RuntimeInstance#getConfiguration()
     */
    public static ExtProperties getConfiguration()
    {
        return ri.getConfiguration();
    }

    /**
     * Returns the event handlers for the application.
     * @return The event handlers for the application.
     * @see RuntimeInstance#getApplicationEventCartridge()
     * @since 1.5
     */
     public EventCartridge getEventCartridge()
     {
         return ri.getApplicationEventCartridge();
     }

    /**
     *  Gets the application attribute for the given key
     *
     * @see org.apache.velocity.runtime.RuntimeServices#getApplicationAttribute(Object)
     * @param key
     * @return The application attribute for the given key.
     * @see RuntimeInstance#getApplicationAttribute(Object)
     */
    public static Object getApplicationAttribute(Object key)
    {
        return ri.getApplicationAttribute(key);
    }

    /**
     * Returns the Uberspect object for this Instance.
     *
     * @return The Uberspect object for this Instance.
     * @see org.apache.velocity.runtime.RuntimeServices#getUberspect()
     * @see RuntimeInstance#getUberspect()
     */
    public static Uberspect getUberspect()
    {
        return ri.getUberspect();
    }


    /**
     * Remove a directive.
     *
     * @param name name of the directive.
     */
    public static void removeDirective(String name)
    {
        ri.removeDirective(name);
    }

    /**
     * Instantiates and loads the directive with some basic checks.
     *
     * @param directiveClass classname of directive to load
     */
    public static void loadDirective(String directiveClass)
    {
        ri.loadDirective(directiveClass);
    }
}
