package org.apache.velocity.example;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeSingleton;

/**
 *  the ultimate in silliness...
 *
 *  tests the DBContext example by putting a string and a hashtable
 *  into the context and then rendering a simple template with it.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */

public class DBContextTest
{
    public DBContextTest(String templateFile)
    {
        try
        {
            RuntimeSingleton.init( new Properties() );

            Template template = RuntimeSingleton.getTemplate(templateFile);

            DBContext dbc = new DBContext();

            Hashtable h = new Hashtable();
            h.put("Bar", "this is from a hashtable!");

            dbc.put( "string", "Hello!");
            dbc.put( "hashtable", h );

            Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

            template.merge(dbc, writer);

            writer.flush();
            writer.close();
        }
        catch( Exception e )
        {
            RuntimeSingleton.getLog().error("Something funny happened", e);
        }
    }

    public static void main(String[] args)
    {
        DBContextTest t;
        t = new DBContextTest(args[0]);
    }
}
