package org.apache.velocity.runtime.visitor;

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

import java.util.Map;

import org.apache.velocity.runtime.parser.node.ASTReference;

/**
 *  This class is a visitor used by the VM proxy to change the 
 *  literal representation of a reference in a VM.  The reason is
 *  to preserve the 'render literal if null' behavior w/o making
 *  the VMProxy stuff more complicated than it is already.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VMReferenceMungeVisitor.java,v 1.2.4.1 2004/03/03 23:23:03 geirm Exp $
 */ 
public class VMReferenceMungeVisitor extends BaseVisitor
{
    /**
     *  Map containing VM arg to instance-use reference
     *  Passed in with CTOR
     */
    private Map argmap = null;

    /**
     *  CTOR - takes a map of args to reference
     */
    public VMReferenceMungeVisitor( Map map )
    {
        argmap = map;
    }

    /**
     *  Visitor method - if the literal is right, will
     *  set the literal in the ASTReference node
     *
     *  @param node ASTReference to work on
     *  @param data Object to pass down from caller
     */
    public Object visit( ASTReference node, Object data)
    {
        /*
         *  see if there is an override value for this
         *  reference
         */
        String override = (String) argmap.get( node.literal().substring(1) );

        /*
         *  if so, set in the node
         */
        if( override != null)
        {
            node.setLiteral( override );
        }

        /*
         *  feed the children...
         */
        data = node.childrenAccept(this, data);   

        return data;
    }
}

