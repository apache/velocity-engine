package org.apache.velocity.runtime;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;

import java.util.Properties;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.resource.ContentResource;

import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.Uberspect;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import org.apache.commons.collections.ExtendedProperties;


/**
 * Interface for internal runtime services that are needed by the 
 * various components w/in Velocity.  This was taken from the old
 * Runtime singleton, and anything not necessary was removed.
 *
 *  Currently implemented by RuntimeInstance.
 * 
 * @author <a href="mailto:geirm@optonline.net">Geir Magusson Jr.</a>
 * @version $Id: RuntimeServices.java,v 1.7.4.1 2004/03/03 23:22:55 geirm Exp $
 */
public interface RuntimeServices extends RuntimeLogger
{

   /*
     * This is the primary initialization method in the Velocity
     * Runtime. The systems that are setup/initialized here are
     * as follows:
     * 
     * <ul>
     *   <li>Logging System</li>
     *   <li>ResourceManager</li>
     *   <li>Parser Pool</li>
     *   <li>Global Cache</li>
     *   <li>Static Content Include System</li>
     *   <li>Velocimacro System</li>
     * </ul>
     */
    public void init() throws Exception;

    /**
     * Allows an external system to set a property in
     * the Velocity Runtime.
     *
     * @param String property key
     * @param String property value
     */
    public  void setProperty(String key, Object value);

    /**
     * Allow an external system to set an ExtendedProperties
     * object to use. This is useful where the external
     * system also uses the ExtendedProperties class and
     * the velocity configuration is a subset of
     * parent application's configuration. This is
     * the case with Turbine.
     *
     * @param ExtendedProperties configuration
     */
    public void setConfiguration( ExtendedProperties configuration);

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
     * @param String key
     * @param String value
     */
    public void addProperty(String key, Object value);
    
    /**
     * Clear the values pertaining to a particular
     * property.
     *
     * @param String key of property to clear
     */
    public void clearProperty(String key);
    
    /**
     *  Allows an external caller to get a property.  The calling
     *  routine is required to know the type, as this routine
     *  will return an Object, as that is what properties can be.
     *
     *  @param key property to return
     */
    public  Object getProperty( String key );

    /**
     * Initialize the Velocity Runtime with a Properties
     * object.
     *
     * @param Properties
     */
    public void init(Properties p) throws Exception;
    
    /**
     * Initialize the Velocity Runtime with the name of
     * ExtendedProperties object.
     *
     * @param Properties
     */
    public void init(String configurationFile) throws Exception;

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
     * @param InputStream inputstream retrieved by a resource loader
     * @param String name of the template being parsed
     */
    public  SimpleNode parse( Reader reader, String templateName )
        throws ParseException;

    /**
     *  Parse the input and return the root of the AST node structure.
     *
     * @param InputStream inputstream retrieved by a resource loader
     * @param String name of the template being parsed
     * @param dumpNamespace flag to dump the Velocimacro namespace for this template
     */
    public SimpleNode parse( Reader reader, String templateName, boolean dumpNamespace )
        throws ParseException;
    
    /**
     * Returns a <code>Template</code> from the resource manager.
     * This method assumes that the character encoding of the 
     * template is set by the <code>input.encoding</code>
     * property.  The default is "ISO-8859-1"
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception;

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
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name, String  encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception;

    /**
     * Returns a static content resource from the
     * resource manager.  Uses the current value
     * if INPUT_ENCODING as the character encoding.
     *
     * @param name Name of content resource to get
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     */
    public ContentResource getContent(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception;

    /**
     * Returns a static content resource from the
     * resource manager.
     *
     * @param name Name of content resource to get
     * @param encoding Character encoding to use
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     */
    public ContentResource getContent( String name, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception;

    /**
     *  Determines is a template exists, and returns name of the loader that 
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.templateExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     */
    public String getLoaderNameForResource( String resourceName );

    /**
     * String property accessor method with default to hide the
     * configuration implementation.
     * 
     * @param String key property key
     * @param String defaultValue  default value to return if key not 
     *               found in resource manager.
     * @return String  value of key or default 
     */
    public String getString( String key, String defaultValue);

    /**
     * Returns the appropriate VelocimacroProxy object if strVMname
     * is a valid current Velocimacro.
     *
     * @param String vmName  Name of velocimacro requested
     * @return String VelocimacroProxy 
     */
    public Directive getVelocimacro( String vmName, String templateName  );

   /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     *
     * @param String name  Name of velocimacro 
     * @param String macro  String form of macro body
     * @param String argArray  Array of strings, containing the 
     *                         #macro() arguments.  the 0th is the name.
     * @return boolean  True if added, false if rejected for some 
     *                  reason (either parameters or permission settings) 
     */
    public boolean addVelocimacro( String name, 
                                          String macro, 
                                          String argArray[], 
                                          String sourceTemplate );
 
    /**
     *  Checks to see if a VM exists
     *
     * @param name  Name of velocimacro
     * @return boolean  True if VM by that name exists, false if not
     */
    public boolean isVelocimacro( String vmName, String templateName );

    /**
     *  tells the vmFactory to dump the specified namespace.  This is to support
     *  clearing the VM list when in inline-VM-local-scope mode
     */
    public boolean dumpVMNamespace( String namespace );

    /**
     * String property accessor method to hide the configuration implementation
     * @param key  property key
     * @return   value of key or null
     */
    public String getString(String key);

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param String key property key
     * @return int value
     */
    public int getInt( String key );

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key  property key
     * @param int default value
     * @return int  value
     */
    public int getInt( String key, int defaultValue );

    /**
     * Boolean property accessor method to hide the configuration implementation.
     * 
     * @param String key  property key
     * @param boolean default default value if property not found
     * @return boolean  value of key or default value
     */
    public boolean getBoolean( String key, boolean def );

    /**
     * Return the velocity runtime configuration object.
     *
     * @return ExtendedProperties configuration object which houses
     *                       the velocity runtime properties.
     */
    public ExtendedProperties getConfiguration();

    /**
     * Return the specified applcation attribute.
     *
     * @param key The name of the attribute to retrieve.
     */
    public Object getApplicationAttribute( Object key );

    /**
     * Returns the configured class introspection/reflection
     * implemenation.
     */
    public Uberspect getUberspect();

    /**
     * Returns the configured method introspection/reflection
     * implemenation.
     */
    public Introspector getIntrospector();
}
