package org.apache.velocity.texen.util;

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
 * @version $Id: PropertiesUtil.java,v 1.9.8.1 2004/03/03 23:23:07 geirm Exp $ 
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
