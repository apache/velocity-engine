package org.apache.velocity.runtime.configuration;

/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine", "Turbine",
 *    "Apache Turbine", "Turbine Project", "Apache Turbine Project" and
 *    "Java Apache Project" must not be used to endorse or promote products
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

import java.io.*;
import java.util.*;

/**
 * Configuration utility from Velocity.
 * Based upon TurbineResources
 * 
 * @author Dave Bryson
 * @version $Revision: 1.2 $
 */
public class VelocityResources
{
    /** The name of the file to load properties from. */
    private static String fileName = null;

    /** The Configurations class */
    private static Configurations conf = null;

    private static boolean isInitialized = false;

    /**
     * Load Configuration from a properties file. 
     *
     * @param propertiesFileName The file name.
     * @exception IOException, if there was an I/O problem.
     */
    public static void setPropertiesFileName(String propertiesFileName)
        throws IOException
    {
        fileName = propertiesFileName;
        if ( fileName == null )
            throw new IOException ( "VelocityResources: fileName must not be null!" );

        init( new ExtendedProperties(fileName) );
    }

    /**
     * Load configuration from the an InputStream.  
     *
     * @param properties A Properties object.
     */
    public static void setPropertiesInputStream( InputStream is )
     throws IOException
    {
        ExtendedProperties exp = new ExtendedProperties();
        exp.load( is );
        init( exp );
    }

    /**
     * Private initializer method that sets up the generic
     * resources.
     *
     * @exception IOException, if there was an I/O problem.
     */
    private static void init( ExtendedProperties exp )
        throws IOException
    {
        conf =  new Configurations( exp );
        isInitialized = true;
    }
    
    /**
     * Indicate to client code whether property
     * resources have been initialized or not.
     */
    public static boolean isInitialized()
    {
        return isInitialized;
    }        
    
    /**
     * Get the Configurations that was used to define this object.
     *
     * @return A Configurations object.
     */
    public static Configurations getConfig()
    {
        return conf;
    }

    /**
     * Add properties from the external system.
     * 
     * @param the name of the value
     * @param the value
     */
    public static void setProperty( String name, String value )
    {
        conf.getRepository().put( name, value );
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a boolean value.
     *
     * @param name The resource name.
     * @return The value of the named resource as a boolean.
     */
    public static boolean getBoolean(String name)
    {
        return conf.getBoolean (name);
    }

    /**
     * The purppose of this method is to get the configuration
     * resource with the given name as a boolean value, or a default
     * value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the named resource as a boolean.
     */
    public static boolean getBoolean(String name,
                                     boolean def)
    {
        return conf.getBoolean(name, def);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a double.
     *
     * @param name The resoource name.
     * @return The value of the named resource as double.
     */
    public static double getDouble(String name)
    {
        return conf.getDouble(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a double, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the named resource as a double.
     */
    public static double getDouble(String name,
                                   double def)
    {
        return conf.getDouble(name, def);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a float.
     *
     * @param name The resource name.
     * @return The value of the resource as a float.
     */
    public static float getFloat(String name)
    {
        return conf.getFloat(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a float, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as a float.
     */
    public static float getFloat(String name,
                                 float def)
    {
        return conf.getFloat(name, def);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an Integer.
     *
     * @param name The resource name.
     * @return The value of the resource as an Integer.
     */
    public static Integer getInteger(String name)
    {
        return new Integer( conf.getInteger(name) );
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an Integer, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as an Integer.
     */
    public static Integer getInteger(String name,
                                     int def)
    {
        return new Integer( conf.getInteger(name, def) );
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an integer.
     *
     * @param name The resource name.
     * @return The value of the resource as an integer.
     */
    public static int getInt(String name)
    {
        return conf.getInteger(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an integer, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as an integer.
     */
    public static int getInt(String name,
                             int def)
    {
        return conf.getInteger(name, def);
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Enumeration with all the keys.
     */
    public static Enumeration getKeys()
    {
        return conf.getKeys();
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository that match the specified prefix.
     *
     * @param prefix A String prefix to test against.
     * @return An Enumeration of keys that match the prefix.
     */
    public static Enumeration getKeys(String prefix)
    {
        Enumeration keys = conf.getKeys();
        Vector matchingKeys = new Vector();
        while( keys.hasMoreElements() )
        {
            Object key = keys.nextElement();
            if( key instanceof String && ((String) key).startsWith(prefix) )
            {
                matchingKeys.addElement(key);
            }
        }
        return matchingKeys.elements();
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a long.
     *
     * @param name The resource name.
     * @return The value of the resource as a long.
     */
    public static long getLong(String name)
    {
        return conf.getLong(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a long, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as a long.
     */
    public static long getLong(String name,
                               long def)
    {
        return conf.getLong(name, def);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a string.
     *
     * @param name The resource name.
     * @return The value of the resource as a string.
     */
    public static String getString(String name)
    {
        return conf.getString(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a string, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as a string.
     */
    public static String getString(String name,
                                   String def)
    {
        return conf.getString(name, def);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a string array.
     *
     * @param name The resource name.
     * @return The value of the resource as a string array.
     */
    public static String[] getStringArray(String name)
    {
        return conf.getStringArray(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a vector.
     *
     * @param name The resource name.
     * @return The value of the resource as a vector.
     */
    public static Vector getVector(String name)
    {
        return conf.getVector(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as a vector, or a default value.
     *
     * @param name The resource name.
     * @param def The default value of the resource.
     * @return The value of the resource as a vector.
     */
    public static Vector getVector(String name,
                                   Vector def)
    {
        Vector vec = conf.getVector(name);
        if ( vec == null)
            return def;
        return vec;
    }
}





