
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
 * @version $Id: TemplateTool.java,v 1.1 2001/11/27 13:25:45 geirm Exp $
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
