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

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;

/**
 * This class is responsible for handling EscapedDirectives
 *  in VTL.
 * 
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTEscapedDirective.java,v 1.3.14.1 2004/03/03 23:22:58 geirm Exp $ 
 */
public class ASTEscapedDirective extends SimpleNode 
{  
    public ASTEscapedDirective(int id) 
    {
        super(id);
    }

    public ASTEscapedDirective(Parser p, int id) 
    {
        super(p, id);
    }


    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) 
    {
        return visitor.visit(this, data);
    }

    public boolean render(InternalContextAdapter context, Writer writer)
        throws IOException
    {
        writer.write(getFirstToken().image);
        return true;
    }    

}
