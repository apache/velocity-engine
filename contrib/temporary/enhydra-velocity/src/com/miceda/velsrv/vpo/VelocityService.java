package com.miceda.velsrv.vpo;

// Java
import java.io.*;
import java.util.Properties;

// Enhydra imports
import com.lutris.util.Config;
import com.lutris.logging.Logger;
import com.lutris.appserver.server.Enhydra;

// Velocity
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.util.SimplePool;
import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;


/**
 * Singleton Velocity service.
 * Processes the template and  makes the information you put in the context 
 * available to the template.
 * 
 * You can init() this in the startUp() method of your Application.
 *
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version 1.0
 * @since 1.0
 */
public class VelocityService
{
    // My single instance
    private static VelocityService instance = 
        new VelocityService();
    
    private static boolean isInited = false;

    /**
     * Encoding determined from the velocity.properties file.
     */
    private String encoding;
    private String defaultContentType;
    
    /**
     * The HTTP content type context key.
     */
    public static final String CONTENT_TYPE = "default.contentType";
    /**
     *  The default content type for the response
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";
      
    /**
     *  Encoding for the output stream
     */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * Private constructor
     */
    private VelocityService()
    {}
        
    /**
     * Start up the service and load the velocity properties file.
     *
     * @param cf a <code>Config</code> value
     * @exception Exception if an error occurs
     */
    public void init( Config cf ) throws Exception
    {
        String vel_props = cf.getString("velocity");
        
        try
        {
            Properties p = new Properties();
            p.load( new FileInputStream(vel_props) );
            
            Velocity.init(p);
            
            defaultContentType = Runtime.getString( CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            encoding = Runtime.getString( Runtime.OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING);
        
        setInitValue(true);
        }
        catch( Exception e )
        {
            
            Enhydra.getLogChannel().write(Logger.ERROR, "Error init() velocity service" + e );
            throw new Exception("Error on init()");  
        }
    }
    
    private void setInitValue( boolean v )
    {
        this.isInited = v;
    }
    
    /**
     * Create a Context object that also contains the globalContext.
     *
     * @return A Context object.
     */
    public VelocityContext getContext()
    {
        return new VelocityContext();
    }
       
    /**
     * Return a template from Velocity.
     *
     * @param filename A String with the name of the template.
     * @return A Template.
     * @exception NotFoundException.
     */
    private Template getTemplate( String name )
     throws Exception
    {
        return Runtime.getTemplate(name);
    } 
        
       
    /**
     * Process the template.
     *
     * @param context a <code>Context</code> value
     * @param filename a <code>String</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    public String handleRequest(Context context, String filename)
        throws Exception
    {
        String results = null;
        ByteArrayOutputStream bytes = null;
        
        try
        {
            bytes = new ByteArrayOutputStream();
            handleRequest(context, filename, bytes);
            results = bytes.toString(encoding);
        } 
        catch(Exception e) 
        {
            renderingError(filename, e);
        }
        finally
        {
            try 
            {
                if (bytes != null) bytes.close();
            }
            catch(IOException ignored)
            {
            }
        }
        return results;
    }

    /**
     * Process the request and fill in the template with the values
     * you set in the Context. 
     *
     * @param context A Context.
     * @param filename A String with the filename of the template.
     * @param out A OutputStream where we will write the process template as
     * a String.
     *
     * @throws TurbineException Any exception trown while processing will be
     *         wrapped into a TurbineException and rethrown.
     */
    private void handleRequest(Context context,
                              String filename,
                              OutputStream output)
        throws Exception
    {
        
        OutputStreamWriter writer = null;
        
        try
        {
            writer = new OutputStreamWriter(output, encoding);
            Template template = getTemplate(filename);
            template.merge( context, writer );
        }
        catch(Exception e) 
        {
            renderingError(filename, e);
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.flush();
                }                
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
    }
    
    /**
     * Macro to handle rendering errors.
     *
     * @param filename The file name of the unrenderable template.
     * @param e        The error.
     *
     * @exception TurbineException Thrown every time.  Adds additional
     *                             information to <code>e</code>.
     */
    private static final void renderingError(String filename, Exception e)
        throws Exception
    {
        String err = "Error rendering Velocity template: " + filename;
        Enhydra.getLogChannel().write(Logger.ERROR, err + ": " + e.getMessage());
        
        throw new Exception(e.getMessage());
    }
        
    /**
     * @return a <code>VelocityService</code> my only instance
     */
    public static VelocityService getInstance()
    {
        return instance;
    }
}







