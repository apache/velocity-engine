package org.apache.velocity;

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
 * @version $Id: DBContext.java,v 1.2 2000/12/30 23:46:18 geirm Exp $
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

    public  boolean internalContainsKey(Object key)
    {
        return false;
    }

    public  Object[] internalGetKeys()
    {
        return null;
    }

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

