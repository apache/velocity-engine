package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.introspection.IntrospectionCacheData;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelPropertyGet;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.app.event.EventCartridge;

import java.lang.reflect.InvocationTargetException;

/**
 *  ASTIdentifier.java
 *
 *  Method support for identifiers :  $foo
 *
 *  mainly used by ASTRefrence
 *
 *  Introspection is now moved to 'just in time' or at render / execution
 *  time. There are many reasons why this has to be done, but the
 *  primary two are   thread safety, to remove any context-derived
 *  information from class member  variables.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTIdentifier.java,v 1.19.4.1 2004/03/03 23:22:58 geirm Exp $
 */
public class ASTIdentifier extends SimpleNode
{
    private String identifier = "";

    /**
     *  This is really immutable after the init, so keep one for this node
     */
    protected Info uberInfo;

    public ASTIdentifier(int id)
    {
        super(id);
    }

    public ASTIdentifier(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  simple init - don't do anything that is context specific.
     *  just get what we need from the AST, which is static.
     */
    public  Object init(InternalContextAdapter context, Object data)
        throws Exception
    {
        super.init(context, data);

        identifier = getFirstToken().image;

        uberInfo = new Info(context.getCurrentTemplateName(),
                getLine(), getColumn());

        return data;
    }

    /**
     *  invokes the method on the object passed in
     */
    public Object execute(Object o, InternalContextAdapter context)
        throws MethodInvocationException
    {

        VelPropertyGet vg = null;

        try
        {
            Class c = o.getClass();

            /*
             *  first, see if we have this information cached.
             */

            IntrospectionCacheData icd = context.icacheGet(this);

            /*
             * if we have the cache data and the class of the object we are
             * invoked with is the same as that in the cache, then we must
             * be allright.  The last 'variable' is the method name, and
             * that is fixed in the template :)
             */

            if (icd != null && icd.contextData == c)
            {
                vg = (VelPropertyGet) icd.thingy;
            }
            else
            {
                /*
                 *  otherwise, do the introspection, and cache it.  Use the
                 *  uberspector
                 */

                vg = rsvc.getUberspect().getPropertyGet(o,identifier, uberInfo);

                if (vg != null && vg.isCacheable())
                {
                    icd = new IntrospectionCacheData();
                    icd.contextData = c;
                    icd.thingy = vg;
                    context.icachePut(this,icd);
                }
            }
        }
        catch(Exception e)
        {
            rsvc.error("ASTIdentifier.execute() : identifier = "
                               + identifier + " : " + e);
        }

        /*
         *  we have no getter... punt...
         */

        if (vg == null)
        {
            return null;
        }

        /*
         *  now try and execute.  If we get a MIE, throw that
         *  as the app wants to get these.  If not, log and punt.
         */
        try
        {
            return vg.invoke(o);
        }
        catch(InvocationTargetException ite)
        {
            EventCartridge ec = context.getEventCartridge();

            /*
             *  if we have an event cartridge, see if it wants to veto
             *  also, let non-Exception Throwables go...
             */

            if (ec != null
                    && ite.getTargetException() instanceof java.lang.Exception)
            {
                try
                {
                    return ec.methodException(o.getClass(), vg.getMethodName(),
                            (Exception)ite.getTargetException());
                }
                catch(Exception e)
                {
                    throw new MethodInvocationException(
                      "Invocation of method '" + vg.getMethodName() + "'"
                      + " in  " + o.getClass()
                      + " threw exception "
                      + ite.getTargetException().getClass() + " : "
                      + ite.getTargetException().getMessage(),
                      ite.getTargetException(), vg.getMethodName());
                }
            }
            else
            {
                /*
                 * no event cartridge to override. Just throw
                 */

                throw  new MethodInvocationException(
                "Invocation of method '" + vg.getMethodName() + "'"
                + " in  " + o.getClass()
                + " threw exception "
                + ite.getTargetException().getClass() + " : "
                + ite.getTargetException().getMessage(),
                ite.getTargetException(), vg.getMethodName());


            }
        }
        catch(IllegalArgumentException iae)
        {
            return null;
        }
        catch(Exception e)
        {
            rsvc.error("ASTIdentifier() : exception invoking method "
                        + "for identifier '" + identifier + "' in "
                        + o.getClass() + " : " + e);
        }

        return null;
    }
}
