
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: TemplateTool.java,v 1.1.8.1 2004/03/04 00:18:28 geirm Exp $
 */ 
public class TemplateTool
{
    /**
     *  returns a list of references in a template in the
     *  order that they are encountered
     */
    public static List referenceList( Template template )
    {
        SimpleNode sn = (SimpleNode) template.getData();

        ReferenceListVisitor rlv = new ReferenceListVisitor();
        
        sn.jjtAccept( rlv, null );

        return rlv.getList();
    }

    /**
     *  returns a map of references as keys, and number of times
     *  they are used in the template.  There is no order
     *  information
     */
    public static Map referenceHistogram( Template template )
    {
        /*
         *  get the list
         */
        List list = referenceList( template );

        HashMap hm = new HashMap();
        int count = 0;

        for( int i = 0; i < list.size(); i++)
        {
            String ref = (String) list.get(i);
            
            Integer ival = (Integer) hm.get( ref );

            if ( ival == null)
                count = 0;
            else
                count = ival.intValue();
            
            count++;

            hm.put( ref, new Integer(count) );
        }

        return hm;
    }

    public static void main( String args[] )
        throws Exception
    {
        if (args.length < 1)
        {
            System.out.println("Need template arg...");
            return;
        }

        VelocityEngine ve = new VelocityEngine();

        ve.init();

        Template t = ve.getTemplate( args[0] );

        List l = TemplateTool.referenceList( t );

        System.out.println("References, in order :");
        System.out.println("======================");

        for( int i = 0; i < l.size(); i++)
        {
            System.out.println("   " + l.get(i));
        }

        Map m = TemplateTool.referenceHistogram( t );

        System.out.println("References, with Frequency Count :");
        System.out.println("==================================");

        for( Iterator iter = m.keySet().iterator()  ; iter.hasNext(); )
        {
            String key = (String) iter.next();

            System.out.println("   " + key + " : " + m.get( key ) );
        }
    }

    /**
     *  Visitor to accumulate references.
     */
    static class ReferenceListVisitor extends BaseVisitor
    {
        List list = null;

        ReferenceListVisitor()
        {
            list = new ArrayList();
        }

        public List getList()
        {
            return list;
        }

        public Object visit( ASTReference node, Object data)
        {
            String lit = node.literal();

            list.add(lit);
            
            /*
             *  feed the children...
             */
            data = node.childrenAccept(this, data);   
            
            return data;
        }
    }
}
