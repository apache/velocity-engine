package org.apache.velocity.texen.util;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.velocity.texen.Generator;

/**
 * A property utility class for the texen text/code generator
 * Usually this class is only used from a Velocity context.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:sbailliez@apache.org">Stephane Bailliez</a>
 * @version $Id: PropertiesUtil.java,v 1.8.2.1 2001/11/08 04:05:57 geirm Exp $ 
 */
public class PropertiesUtil
{
    /**
     * Load properties from either a file in the templatePath if there
     * is one or the classPath.
     *
     * @param propertiesFile the properties file to load through
     * either the templatePath or the classpath.
     * @return a properties instance filled with the properties found
     * in the file or an empty instance if no file was found.
     */
    public Properties load(String propertiesFile)
    {
        Properties properties = new Properties();
        String templatePath = Generator.getInstance().getTemplatePath();
        if (templatePath != null)
        {
            properties = loadFromTemplatePath(propertiesFile);
        }
        else
        {
            properties = loadFromClassPath(propertiesFile);
        }
    
        return properties;
        
    }
    
    /**
     * Load a properties file from the templatePath defined in the
     * generator. As the templatePath can contains multiple paths,
     * it will cycle through them to find the file. The first file
     * that can be successfully loaded is considered. (kind of
     * like the java classpath), it is done to clone the Velocity
     * process of loading templates.
     *
     * @param propertiesFile the properties file to load. It must be
     * a relative pathname.
     * @return a properties instance loaded with the properties from
     * the file. If no file can be found it returns an empty instance.
     */
    protected Properties loadFromTemplatePath(String propertiesFile)
    {
        Properties properties = new Properties();
        String templatePath = Generator.getInstance().getTemplatePath();
        
        // We might have something like the following:
        //
        // #set ($dbprops = $properties.load("$generator.templatePath/path/props")
        //
        // as we have in Torque but we want people to start using
        //
        // #set ($dbprops = $properties.load("path/props")
        //
        // so that everything works from the filesystem or from
        // a JAR. So the actual Generator.getTemplatePath()
        // is not deprecated but it's use in templates
        // should be.
        StringTokenizer st = new StringTokenizer(templatePath, ",");
        while (st.hasMoreTokens())
        {
            String templateDir = st.nextToken();
            try
            {
                // If the properties file is being pulled from the
                // file system and someone is using the method whereby
                // the properties file is assumed to be in the template
                // path and they are simply using:
                //
                // #set ($dbprops = $properties.load("props") (1)
                // 
                // than we have to tack on the templatePath in order
                // for the properties file to be found. We want (1)
                // to work whether the generation is being run from
                // the file system or from a JAR file.
                String fullPath = propertiesFile;
                
                // FIXME probably not that clever since there could be
                // a mix of file separators and the test will fail :-(
                if (!fullPath.startsWith(templateDir))
                {
                    fullPath = templateDir + "/" + propertiesFile;
                }

                properties.load(new FileInputStream(fullPath));
                // first pick wins, we don't need to go further since
                // we found a valid file.
                break;
            }
            catch (Exception e)
            {
                // do nothing
            }
        } 
        return properties;
    }

    /**
     * Load a properties file from the classpath
     *
     * @param propertiesFile the properties file to load.
     * @return a properties instance loaded with the properties from
     * the file. If no file can be found it returns an empty instance.
     */ 
    protected Properties loadFromClassPath(String propertiesFile)
    {
        Properties properties = new Properties();
        ClassLoader classLoader = this.getClass().getClassLoader();
        
        try
        {
            // This is a hack for now to make sure that properties
            // files referenced in the filesystem work in
            // a JAR file. We have to deprecate the use
            // of $generator.templatePath in templates first
            // and this hack will allow those same templates
            // that use $generator.templatePath to work in
            // JAR files.
            if (propertiesFile.startsWith("$generator"))
            {
                propertiesFile = propertiesFile.substring(
                    "$generator.templatePath/".length());
            }
            
            InputStream inputStream = classLoader.getResourceAsStream(propertiesFile);
            properties.load(inputStream);
        }
        catch (IOException ioe)
        {
            // do nothing
        }
        return properties;
    }
}
