
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
