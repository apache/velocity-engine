package com.miceda.velsrv.vpo;

// Java
import java.io.*;

// Enhydra
import com.lutris.appserver.server.Enhydra;
import com.lutris.logging.Logger;
import com.lutris.appserver.server.httpPresentation.HttpPresentation;
import com.lutris.appserver.server.httpPresentation.HttpPresentationComms;
import com.lutris.appserver.server.httpPresentation.HttpPresentationException;

// Velocity
import org.apache.velocity.VelocityContext;

/**
 * Extend this class for presentation instead of implementing
 * the HttpPresentation interface.
 * <br>
 * This is not optimized, but its a start.
 *
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version 1.0
 * @since 1.0
 * @see HttpPresentation
 */
public abstract class VelocityPO implements HttpPresentation
{
   
    /**
     * Override and populate the context.
     * see the WelcomePresentation example
     * 
     * @param comms a <code>HttpPresentationComms</code> value
     * @param context a <code>VelocityContext</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    protected abstract String processRequest( HttpPresentationComms comms, VelocityContext context )
     throws Exception;

    
    /**
     * Call this to return the processed template.
     *
     * @param context a <code>VelocityContext</code> value
     * @param filename a <code>String</code> value
     * @return a <code>String</code> value
     * @exception Exception if an error occurs
     */
    protected String getOutput( VelocityContext context, String filename )
        throws Exception
    {
        return VelocityService.getInstance().handleRequest(context,filename);
    }
    
    
    /**
     * Implements the run method required by the HttpPresentation.
     * You do not need to use this method.
     *
     * @param comms a <code>HttpPresentationComms</code> value
     * @exception Exception if an error occurs
     */
    public void run( HttpPresentationComms comms )
     throws Exception
    {
        String outputString = null;
        
        try
        {
            outputString = processRequest( comms,  doSetupContext(comms) );
        }
        catch( Exception e )
        {
            outputString = error(e.getMessage());
            Enhydra.getLogChannel().write( Logger.ERROR, "Error processing template " + e ); 
        }
        finally
        {
            if( outputString == null )
            {
                String errorMessage = 
                    "Output set to null. Did you set the template filename?";
                doOutput( comms, error(errorMessage) );
            }
            else
            {
                doOutput( comms, outputString );
            }
        }
    }
    
    /**
     * Setup the context. Note, this automatically makes 
     * the HttpPresentationComms available to your templates
     *
     * @param comms a <code>HttpPresentationComms</code> value
     * @return a <code>VelocityContext</code> value
     */
    private VelocityContext doSetupContext( HttpPresentationComms comms )
    {
        VelocityContext ctx = getContext();
        ctx.put("comms", comms );
        return ctx;
    }
      
    /**
     * Get the context from the Service
     *
     * @return a <code>VelocityContext</code> value
     */
    private VelocityContext getContext()
    {
        return VelocityService.getInstance().getContext();
    }
       
    /**
     * Write out the populated template to the  HttpPresentationComms
     *
     * @param comms a <code>HttpPresentationComms</code> value
     * @param outty a <code>String</code> value
     * @exception Exception if an error occurs
     */
    private void doOutput( HttpPresentationComms comms, String outty  )
        throws Exception
    {
        comms.response.writeHTML( outty );
    }

    /**
     * Returns a default error message if the template was not found.
     *
     * @param message a <code>String</code> value
     * @return a <code>String</code> value
     */
    private final String error( String message )
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>Error processing the template</h2>");
        html.append(message);
        html.append("</body>");
        html.append("</html>");
        
        return  html.toString();
    }
}




