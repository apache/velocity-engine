
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

import org.apache.velocity.app.VelocityEngine;

/**
 *  simple class to demonstrate how to use the
 *  StderrLogSystem.  It doesn't do anything but
 *  setup and init(), but that should produce
 *  enough output...
 *
 *  @author <a href="geirm@apache.org">Geir Magnusson Jr.</a>
 *  @version $Id: Test.java,v 1.1.8.1 2004/03/04 00:18:28 geirm Exp $
 */
public class Test
{
    Test()
    {
        VelocityEngine ve = new VelocityEngine();

        ve.setProperty("runtime.log.logsystem.class", "StderrLogSystem");

        try
        {
            ve.init();
        }
        catch(Exception e )
        {
            System.out.println( e );
        }
    }

    public static void main( String args[] )
    {
        Test t = new Test();
    }
}
