package org.apache.velocity.runtime.log;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @version $Id: LogManager.java,v 1.9 2001/11/17 12:32:34 geirm Exp $
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
                    rsvc.info("Couldn't find class " + claz 
                              +" or necessary supporting classes in classpath. Exception : " 
                              + ncdfe );
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

