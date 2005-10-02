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

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.RuntimeServices;


/**
 *  This is a toy demonstration of how Velocity
 *  can use an externally configured logger.  In 
 *  this example, the class using Velocity
 *  implements Velocity's logger interface, and
 *  all Velocity log messages are funneled back
 *  through it.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class LoggerExample implements LogChute
{
    public LoggerExample()
    {
        try
        {
            /*
             *  this class implements the LogSystem interface, so we
             *  can use it as a logger for Velocity
             */
            
            Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this );
            Velocity.init();

            /*
             *  that will be enough.  The Velocity initialization will be 
             *  output to stdout because of our
             *  logVelocityMessage() method in this class
             */
        }
        catch( Exception e )
        {
            System.out.println("Exception : " + e);
        }
    }

	/**
	 *  Required init() method for LogSystem
	 *  to get access to RuntimeServices
	 */ 
	 public void init( RuntimeServices rs )
	 {
	 	return;
	 }
	 
    /**
     * This just prints the message and level to System.out.
     */ 
    public void log(int level, String message)
    {
        System.out.println("level : " + level + " msg : " + message);
    }

     
    /**
     * This prints the level, message, and the Throwable's message to 
     * System.out.
     */ 
    public void log(int level, String message, Throwable t)
    {
        System.out.println("level : " + level + " msg : " + message + " t : "
                           + t.getMessage());
    }

    /**
     * This always returns true because logging levels can't be disabled in
     * this impl.
     */
    public void isLevelEnabled(int level)
    {
        return true;
    }

    public static void main(String[] args)
    {
        LoggerExample t = new LoggerExample();
    }
}
