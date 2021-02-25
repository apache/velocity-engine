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

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *   Example context impl that uses a database to store stuff :)
 *
 *   yes, this is silly
 *
 *   expects a mysql db test with table
 *
 *  CREATE TABLE contextstore (
 *    k varchar(100),
 *    val blob
 *  );
 *
 *  very fragile, crappy code.... just a demo!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */

public class DBContext extends AbstractContext
{
    Connection conn = null;

    public DBContext()
    {
        super();
        setup();
    }

    public DBContext( Context inner )
    {
        super( inner );
        setup();
    }

    /**
     *  retrieves a serialized object from the db
     *  and returns the living instance to the
     *  caller.
     */
    @Override
    public Object internalGet(String key )
    {
        try
        {
            String sql = "SELECT val FROM contextstore WHERE k ='"+key+"'";
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery( sql );

            Object o = null;
            ObjectInputStream in = null;

            if(rs.next())
            {
                in = new ObjectInputStream(  rs.getBinaryStream(1) );
                o =  in.readObject();
                in.close();
            }

            rs.close();
            s.close();

            return o;
        }
        catch(Exception e)
        {
            System.out.println("internalGet() : " + e );
            e.printStackTrace();
        }

        return null;
    }

    /**
     *  Serializes and stores an object in the database.
     *  This is really a hokey way to do it, and will
     *  cause problems.  The right way is to use a
     *  prepared statement...
     */
    @Override
    public Object internalPut(String key, Object value )
    {
        try
        {
            Statement s = conn.createStatement();
            s.executeUpdate( "DELETE FROM contextstore WHERE k = '" + key + "'" );
            s.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( baos );
            out.writeObject( value );
            byte buf[] = baos.toByteArray();
            out.close();
            baos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO contextstore (k,val) values ('"+key+"', ?)");
            ps.setBinaryStream(1, bais, buf.length);
            ps.executeUpdate();
            ps.close();

        }
        catch(Exception e)
        {
            System.out.println("internalGet() : " + e );
        }

        return null;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    @Override
    public  boolean internalContainsKey(String key)
    {
        return false;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    @Override
    public  String[] internalGetKeys()
    {
        return null;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    @Override
    public  Object internalRemove(String key)
    {
        return null;
    }


    private void setup()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=YOUR_DB_USER&password=YOUR_DB_PASSWORD");
        }
        catch (Exception e)
        {
            System. out.println(e);
        }
    }
}

