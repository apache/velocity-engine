package org.apache.velocity.runtime.log;

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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * <p>
 * This class is responsible for instantiating the correct LoggingSystem
 * </p>
 *
 * <p>
 * The approach is :
 * </p>
 * <ul>
 * <li> 
 *      First try to see if the user is passing in a living object
 *      that is a LogSystem, allowing the app to give is living
 *      custom loggers.
 *  </li>
 *  <li> 
 *       Next, run through the (possible) list of classes specified
 *       specified as loggers, taking the first one that appears to 
 *       work.  This is how we support finding either log4j or
 *       logkit, whichever is in the classpath, as both are 
 *       listed as defaults.
 *  </li>
 *  <li>
 *      Finally, we turn to 'faith-based' logging, and hope that
 *      logkit is in the classpath, and try for an AvalonLogSystem
 *      as a final gasp.  After that, there is nothing we can do.
 *  </li>
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: LogManager.java,v 1.10.4.1 2004/03/03 23:22:56 geirm Exp $
 */
public class LogManager
{
    /**
     *  Creates a new logging system or returns an existing one
     *  specified by the application.
     */
    public static LogSystem createLogSystem( RuntimeServices rsvc )
        throws Exception
    {
        /*
         *  if a logSystem was set as a configuation value, use that. 
         *  This is any class the user specifies.
         */

        Object o = rsvc.getProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM );

        if (o != null && o instanceof LogSystem)
        {
            ((LogSystem) o).init( rsvc );

            return (LogSystem) o;
        }
  
        /*
         *  otherwise, see if a class was specified.  You
         *  can put multiple classes, and we use the first one we find.
         *
         *  Note that the default value of this property contains both the
         *  AvalonLogSystem and the SimpleLog4JLogSystem for convenience - 
         *  so we use whichever we find.
         */
        
        List classes = null;
        Object obj = rsvc.getProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS );

        /*
         *  we might have a list, or not - so check
         */

        if ( obj instanceof List)
        {
            classes = (List) obj;
        }
        else if ( obj instanceof String)
        { 
            classes = new ArrayList();
            classes.add( obj );
        }

        /*
         *  now run through the list, trying each.  It's ok to 
         *  fail with a class not found, as we do this to also
         *  search out a default simple file logger
         */

        for( Iterator ii = classes.iterator(); ii.hasNext(); )
        {
            String claz = (String) ii.next();

            if (claz != null && claz.length() > 0 )
            {
                rsvc.info("Trying to use logger class " + claz );
          
                try
                {
                    o = Class.forName( claz ).newInstance();

                    if ( o instanceof LogSystem )
                    {
                        ((LogSystem) o).init( rsvc );

                        rsvc.info("Using logger class " + claz );

                        return (LogSystem) o;
                    }
                    else
                    {
                        rsvc.error("The specifid logger class " + claz + 
                                   " isn't a valid LogSystem");
                    }
                }
                catch( NoClassDefFoundError ncdfe )
                {
                    rsvc.debug("Couldn't find class " + claz 
                               + " or necessary supporting classes in "
                               + "classpath. Exception : " + ncdfe);
                }
            }
        }
      
        /*
         *  if the above failed, then we are in deep doo-doo, as the 
         *  above means that either the user specified a logging class
         *  that we can't find, there weren't the necessary
         *  dependencies in the classpath for it, or there were no
         *  dependencies for the default loggers, log4j and logkit.
         *  Since we really don't know, 
         *  then take a wack at the AvalonLogSystem as a last resort.
         */

        LogSystem als = null;

        try
        {
            als = new AvalonLogSystem();

            als.init( rsvc );
        }
        catch( NoClassDefFoundError ncdfe )
        {
            String errstr = "PANIC : Velocity cannot find any of the"
                + " specified or default logging systems in the classpath,"
                + " or the classpath doesn't contain the necessary classes"
                + " to support them."
                + " Please consult the documentation regarding logging."
                + " Exception : " + ncdfe;

            System.out.println( errstr );
            System.err.println( errstr );

            throw ncdfe;
        }

        rsvc.info("Using AvalonLogSystem as logger of final resort.");
        
        return als;
    }
}

