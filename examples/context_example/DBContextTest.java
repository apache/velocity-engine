
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.Runtime;

/**
 *  the ultimate in silliness...
 *
 *  tests the DBContext example by putting a string and a hashtable
 *  into the context and then rendering a simple template with it.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: DBContextTest.java,v 1.1 2001/02/12 03:11:25 geirm Exp $
 */

public class DBContextTest
{
    public DBContextTest(String templateFile)
    {
        try
        {
            Runtime.init( new Properties() );
 
            Template template = Runtime.getTemplate(templateFile);

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
            Runtime.error(e);
        }
    }

    public static void main(String[] args)
    {
        DBContextTest t;
        t = new DBContextTest(args[0]);
    }
}
