
// Enhydra
import com.lutris.appserver.server.httpPresentation.*;

// Velocity Service
import com.miceda.velsrv.vpo.*;
import org.apache.velocity.VelocityContext;

// Standard imports
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;


/**
 * Simple example of how to use for presentation.
 *
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version 1.0
 * @since 1.0
 * @see VelocityPO
 */
public class WelcomePresentation extends VelocityPO
{
    protected String processRequest( HttpPresentationComms comms, VelocityContext context )
     throws Exception
    {
        // Populate the context
        context.put("mymessage","Hello from Velocity");
        context.put("today", new Date() );
        
        // pass the name of the template
        return getOutput( context, "welcome.vm");
    }
}
