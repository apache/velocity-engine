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
 * @version $Id: Generator.java,v 1.1 2000/11/03 14:42:00 jvanzyl Exp $ 
 */
public class Generator
{
    public static final String PATH_INPUT = "path.input";
    public static final String PATH_OUTPUT = "path.output";
    public static final String CONTEXT_STRINGS = "context.objects.strings";
    public static final String CONTEXT_FILES = "context.objects.files";
    
    Properties props = new Properties();

    //Generator instance = new Generator();

    /**
     * Create a new generator object with default properties
     */
    public Generator ()
    {
        setDefaultProps();
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
        props.setProperty (PATH_INPUT,".");
        props.setProperty (PATH_OUTPUT,"output/");
        props.setProperty (CONTEXT_STRINGS,"org.apache.velocity.texen.util.StringUtil");
        props.setProperty (CONTEXT_FILES,"org.apache.velocity.texen.util.FileUtil");
    }
        
    /**
     * Get a property from the propery set of this generator.
     */        
    public String getProperty (String name)
    {
        return props.getProperty (name,"");
    }

    /**
     * Get a property from the propery set of this generator.
     */        
    public void setProperty (String key, String value)
    {
        props.put(key,value);
    }

    /**
     * Parse an input file and return the result as a String
     * object.
     */ 
    
    /*
    public String parse (String input) throws Exception
    {
        return this.parse (input,null);
    }
    */
     
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
     * You can add one object (obj) to the context with the name objName.
     */ 
    public String parse (String input, String output, String objName, Object obj) 
        throws Exception
    {
        Hashtable h = new Hashtable();
        if (objName != null && obj != null)
        {
            h.put (objName,obj);
        }
        
        return this.parse (input,output,h);
    }
    
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     */ 
    public String parse (String input, String output, Hashtable objs) 
        throws Exception
    {
        try
        {
        Context context = getContext (objs);
        
        Template template = Runtime.getTemplate(input);
        
        if (output == null || output.equals(""))
        {
            StringWriter sw = new StringWriter();
            template.merge (context,sw);
            return sw.toString();
        }
        else
        {
            FileWriter fw = new FileWriter (props.getProperty (PATH_OUTPUT)+
                                            File.separator +
                                            output);
            template.merge (context,fw);
            fw.close();
            
            return "";
        }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        
    }

    public String parse (String controlTemplate, Context controlContext)
        throws Exception
    {
        fillContextDefaults(controlContext);
        fillContextProperties(controlContext);
        
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
        Context context = new Context();
        
        fillContextHash (context,objs);
        fillContextDefaults (context);
        fillContextProperties (context);
        
        return context;
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
        Generator gen = new Generator (props);
        context.put ("generator",gen);
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
    
    /**
     * Just 4 Testing
     */
    
    /*
    public static void main (String[] args) throws Exception
    {
        Runtime.init("velocity.properties");
        //Runtime.init();
        
        Generator gen = new Generator();
        System.out.println (gen.parse (args[0]));
    }
    */
         
}
