package org.apache.velocity.texen.util;


// JDK Classes
import java.io.*;
import java.util.*;

/**
 * A property utility class for the texen text/code generator
 * Usually this class is only used from a Velocity context.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 */
public class PropertiesUtil extends BaseUtil
{
    public Properties load(String propertiesFile)
    {
        Properties properties = new Properties();
        
        try
        {
            properties.load(new FileInputStream(propertiesFile));
            return properties;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
