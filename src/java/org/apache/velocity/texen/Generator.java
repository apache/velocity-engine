package org.apache.velocity.texen;

// JDK Classes
import java.io.*;
import java.util.*;

// Velocity Classes
import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;

// Local Classes
import org.apache.velocity.texen.util.BaseUtil;

/**
 * A text/code generator class
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Generator.java,v 1.6 2000/11/18 00:56:04 jvanzyl Exp $ 
 */
public class Generator
{
    public static final String OUTPUT_PATH = "output.path";
    public static final String TEMPLATE_PATH = "template.path";
    
    private static final String DEFAULT_TEXEN_PROPERTIES =
        "org/apache/velocity/texen/defaults/texen.properties";

    private Properties props = new Properties();
    private Context controlContext;
    
    private static Generator instance = new Generator();

    private Generator()
    {
        setDefaultProps();
    }

    /**
     * Create a new generator object with default properties
     */
    public static Generator getInstance()
    {
        return instance;
    }
    
    /**
     * Create a new generator object with properties loaded from
     * a file.  If the file does not exist or any other exception
     * occurs during the reading operation the default properties
     * are used.
     */
    public Generator (String propFile)
    {
        try
        {
            FileInputStream fi = new FileInputStream (propFile);
            BufferedInputStream bi = new BufferedInputStream (fi);
            try
            {
                props.load (bi);
            }
            finally
            {
                bi.close();
            }
        }
        catch (Exception e)
        {
            // If something goes wrong we use default properties
            setDefaultProps();
        }
    }
    
    /**
     * Create a new Generator object with a given property
     * set.  The property set will be duplicated.
     */
    public Generator (Properties props)
    {
        this.props = (Properties)props.clone();
    }
    
    
    /**
     * Set default properties
     */
    protected void setDefaultProps()
    {
        ClassLoader classLoader = Runtime.class.getClassLoader();
        try
        {
            InputStream inputStream = classLoader.getResourceAsStream(
                DEFAULT_TEXEN_PROPERTIES);
            
            props.load( inputStream );
        }
        catch (Exception ioe)
        {
            System.err.println("Cannot get default properties!");
        }
    }
    
    /**
     * Set the template path, where Texen will look
     * for Velocity templates.
     */
    public void setTemplatePath(String templatePath)
    {
        props.put(TEMPLATE_PATH, templatePath);
    }

    /**
     * Get the template path.
     */
    public String getTemplatePath()
    {
        return props.getProperty(TEMPLATE_PATH);
    }

    /**
     * Set the output path for the generated
     * output.
     */
    public void setOutputPath(String outputPath)
    {
        props.put(OUTPUT_PATH, outputPath);
    }

    /**
     * Get the output path for the generated
     * output.
     */
    public String getOutputPath()
    {
        return props.getProperty(OUTPUT_PATH);
    }

    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     */ 
    public String parse (String input, String output) throws Exception
    {
        return this.parse (input,output,null,null);
    }
    
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     */ 
    public String parse (String input, String output, String objName, Object obj) 
        throws Exception
    {
        
        if (objName != null && obj != null)
            controlContext.put(objName, obj);
        
        Template template = Runtime.getTemplate(input);
        
        if (output == null || output.equals(""))
        {
            StringWriter sw = new StringWriter();
            template.merge (controlContext,sw);
            return sw.toString();
        }
        else
        {
            FileWriter fw = new FileWriter (getOutputPath() +
                File.separator + output);
            template.merge (controlContext,fw);
            fw.close();
            
            return "";
        }
    }

    public String parse (String controlTemplate, Context controlContext)
        throws Exception
    {
        this.controlContext = controlContext;
        fillContextDefaults(this.controlContext);
        fillContextProperties(this.controlContext);
        
        Template template = Runtime.getTemplate(controlTemplate);
        StringWriter sw = new StringWriter();
        template.merge (controlContext,sw);
        
        return sw.toString();
    }


    /**
     * Create a new context and fill it with the elements of the
     * objs Hashtable.  Default objects and objects that comes from
     * the properties of this Generator object is also added.
     */ 
    protected Context getContext (Hashtable objs)
    {
        fillContextHash (controlContext,objs);
        return controlContext;
    }

    /** 
     * Add all the contents of a Hashtable to the context
     */
    protected void fillContextHash (Context context, Hashtable objs)
    {
        Enumeration enum = objs.keys();
        while (enum.hasMoreElements())
        {
            String key = enum.nextElement().toString();
            context.put (key, objs.get(key));
        }
    }
    

    /**
     * Add properties that will aways be in the context by default
     */
    protected void fillContextDefaults (Context context)
    {
        context.put ("generator", instance);
        context.put ("outputDirectory", getOutputPath());
    }
    
    /**
     * Add objects to the context from the current properties
     */
    protected void fillContextProperties (Context context)
    {
        Enumeration enum = props.propertyNames();
        
        while (enum.hasMoreElements())
        {
            String nm = (String)enum.nextElement();
            if (nm.startsWith ("context.objects."))
            {
                
                String contextObj = props.getProperty (nm);
                int colon = nm.lastIndexOf ('.');
                String contextName = nm.substring (colon+1);
                
                try
                {
                    Class cls = Class.forName (contextObj);
                    BaseUtil b = (BaseUtil)cls.newInstance();
                    b.init();
                    context.put (contextName,b);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //TO DO: Log Something Here
                }
            }
        }
    }
}
