
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

// Velocity Service
import com.miceda.velsrv.vpo.*;

import com.lutris.appserver.server.*;
import com.lutris.appserver.server.httpPresentation.*;
import com.lutris.appserver.server.session.*;
import com.lutris.util.*;

/**
 * An example of the Application class.
 * You can init() Velocity in the statup()
 *
 * Application-wide data would go here.
 */
public class VelocityApplication extends StandardApplication {

    /*
     *  A few methods you might want to add to.
     *  See StandardApplication for more details.
     */
    public void startup(Config appConfig) throws ApplicationException {
        super.startup(appConfig);
        try
        {
            // Start-up Velocity
            VelocityService.getInstance().init( appConfig );
        }
        catch( Exception e )
        {
            System.out.println("Cannot start service " + e );
        }

        System.out.println("Started service...");
        
    }
    public boolean requestPreprocessor(HttpPresentationComms comms)
                   throws Exception {
        return super.requestPreprocessor(comms);
    }

    /**
     * This is an optional function, used only by the Multiserver's graphical
     * administration. This bit of HTML appears in the status page for this
     * application. You could add extra status info, for example
     * a list of currently logged in users.
     *
     * @return HTML that is displayed in the status page of the Multiserver.
     */
    public String toHtml() {
        return "This is the<I>Velocity Example</I>";
    }
}

