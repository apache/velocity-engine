/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
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

import java.sql.*;
import java.io.Serializable;
import java.io.*;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

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
 * @version $Id: DBContext.java,v 1.2 2001/03/28 03:17:27 geirm Exp $
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
    public Object internalGet( String key )
    {
        try 
        {
            String data = null;

            String sql = "SELECT k, val FROM contextstore WHERE k ='"+key+"'";
            
            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery( sql );

            if(rs.next())
               data = rs.getString("val");
            
            rs.close();
            s.close();
            
            ObjectInputStream in = new ObjectInputStream(  new ByteArrayInputStream( data.getBytes() ));

            Object o =  in.readObject();

            in.close();

            return o;
        }        
        catch(Exception e)
        {
            System.out.println("internalGet() : " + e );
        }

        return null;
    }

    /**
     *  Serializes and stores an object in the database.
     *  This is really a hokey way to do it, and will
     *  cause problems.  The right way is to use a 
     *  prepared statement...
     */
    public Object internalPut( String key, Object value )
    {
        try 
        {    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( baos );
            
            out.writeObject( value );
            String data = baos.toString();

            out.close();
            baos.close();
          
            Statement s = conn.createStatement();

            s.executeUpdate( "DELETE FROM contextstore WHERE k = '" + key + "'" );
            s.executeUpdate( "INSERT INTO contextstore (k,val) values ('"+key+"','" + data + "')" );

            s.close();            
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
    public  boolean internalContainsKey(Object key)
    {
        return false;
    }
    
    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    public  Object[] internalGetKeys()
    {
        return null;
    }

    /**
     *  Not implementing. Not required for Velocity core
     *  operation, so not bothering.  As we say above :
     *  "very fragile, crappy code..."
     */
    public  Object internalRemove(Object key)
    {
        return null;
    }


    private void setup()
    {
        try
        {
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test?user=root");
        }
        catch (Exception e) 
        {
            System. out.println(e);
        }
      
        return;
    }
}

