package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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


/**
 * handles the range 'operator'  [ n .. m ]
 * 
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTIntegerRange.java,v 1.4.10.1 2004/03/03 23:22:58 geirm Exp $ 
*/

import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;

import org.apache.velocity.exception.MethodInvocationException;

public class ASTIntegerRange extends SimpleNode {
  
    public ASTIntegerRange(int id) 
    {
        super(id);
    }

    public ASTIntegerRange(Parser p, int id) 
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) 
    {
        return visitor.visit(this, data);
    }

    /**
     *  does the real work.  Creates an Vector of Integers with the
     *  right value range
     *
     *  @param context  app context used if Left or Right of .. is a ref
     *  @return Object array of Integers
     */
    public Object value( InternalContextAdapter context)
        throws MethodInvocationException
    { 
        /*
         *  get the two range ends
         */

        Object left = jjtGetChild(0).value( context );
        Object right = jjtGetChild(1).value( context );

        /*
         *  if either is null, lets log and bail
         */

        if (left == null || right == null)
        {
            rsvc.error( ( left == null ? "Left" : "Right" ) + " side of range operator [n..m] has null value."
                           + " Operation not possible. "
                           +  context.getCurrentTemplateName() + " [line " + getLine() 
                           + ", column " + getColumn() + "]");
            return null;
        }
        
        /*
         *  if not an Integer, not much we can do either
         */

        if ( !( left instanceof Integer )  || !( right instanceof Integer ))
        {
            rsvc.error( ( !( left instanceof Integer ) ? "Left" : "Right" ) 
                           + " side of range operator is not a valid type. "
                           + "Currently only integers (1,2,3...) and Integer type is supported. "
                           + context.getCurrentTemplateName() + " [line " + getLine() 
                           + ", column " + getColumn() + "]");
 
            return null;
        }


        /*
         *  get the two integer values of the ends of the range
         */

        int l = ( (Integer) left ).intValue() ;
        int r = (  (Integer) right ).intValue();

        /*
         *  find out how many there are
         */

        int num = Math.abs( l - r );
        num += 1;

        /*
         *  see if your increment is Pos or Neg
         */

        int delta = ( l >= r ) ? -1 : 1;

        /*
         *  make the vector and fill it
         */

        ArrayList foo = new ArrayList();
        int val = l;

        for(int i =0; i < num; i++)
        {
            foo.add( new Integer( val ));
            val += delta;
        }

        return foo;
    }
}

