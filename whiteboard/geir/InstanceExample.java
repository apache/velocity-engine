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

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

/**
 * This class is a simple demonstration of how to use multiple instances
 * of the Velocity engine.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: InstanceExample.java,v 1.1.10.1 2004/03/04 00:18:30 geirm Exp $
 */

public class InstanceExample
{
    public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */
        
        VelocityEngine ve1 = new VelocityEngine();
        VelocityEngine ve2 = new VelocityEngine();

        try
        {
            ve1.setProperty("runtime.log", "velengine1.log");
            ve1.setProperty("file.resource.loader.path", "./template1");
            ve1.init();
            
            ve2.setProperty("runtime.log", "velengine2.log");
            ve2.setProperty("file.resource.loader.path", "./template2");
            ve2.init();
        }
        catch(Exception e)
        {
            System.out.println("Problem initializing Velocity : " + e );
            return;
        }

        /* lets make a Context and put data into it */

        VelocityContext context = new VelocityContext();

        context.put("name", "Velocity");
        context.put("project", "Jakarta");
        
        /* lets render a template */

        StringWriter w1 = new StringWriter();
        StringWriter w2 = new StringWriter();

        try
        {
            ve1.mergeTemplate("instanceexample.vm", context, w1);
            ve2.mergeTemplate("instanceexample.vm", context, w2);
        }
        catch (Exception e )
        {
            System.out.println("Problem merging template : " + e );
        }

        System.out.println(" template 1 : " + w1 );
        System.out.println(" template 2 : " + w2 );
    }
}
