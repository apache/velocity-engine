package org.apache.velocity.context;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import java.util.HashMap;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.directive.VMProxyArg;
import org.apache.velocity.util.introspection.IntrospectionCacheData;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.app.event.EventCartridge;

/**
 *  This is a special, internal-use-only context implementation to be
 *  used for the new Velocimacro implementation.
 *
 *  The main distinguishing feature is the management of the VMProxyArg objects
 *  in the put() and get() methods.
 *
 *  Further, this context also supports the 'VM local context' mode, where
 *  any get() or put() of references that aren't args to the VM are considered
 *  local to the vm, protecting the global context.
 *  
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @version $Id: VMContext.java,v 1.9 2001/08/31 09:38:59 geirm Exp $ 
 */
public class VMContext implements InternalContextAdapter
{
    /** container for our VMProxy Objects */
    HashMap vmproxyhash = new HashMap();

    /** container for any local or constant VMProxy items */
    HashMap localcontext = new HashMap();

    /** the base context store.  This is the 'global' context */
    InternalContextAdapter innerContext = null;

    /** context that we are wrapping */
    InternalContextAdapter wrappedContext = null;

    /** support for local context scope feature, where all references are local */
    private  boolean localcontextscope = false;

     /**
     *  CTOR, wraps an ICA
     */
    public VMContext( InternalContextAdapter  inner, RuntimeServices rsvc )
    {
        localcontextscope = rsvc.getBoolean( RuntimeConstants.VM_CONTEXT_LOCALSCOPE, false );

        wrappedContext = inner;
        innerContext = inner.getBaseContext();
    }

    /**
     *  return the inner / user context
     */
    public Context getInternalUserContext()
    {
        return innerContext.getInternalUserContext();
    }

    public InternalContextAdapter getBaseContext()
    {
        return innerContext.getBaseContext();
    }

    /**
     *  Used to put VMProxyArgs into this context.  It separates
     *  the VMProxyArgs into constant and non-constant types
     *  pulling out the value of the constant types so they can
     *  be modified w/o damaging the VMProxyArg, and leaving the
     *  dynamic ones, as they modify context rather than their own
     *  state
     *  @param  vmpa VMProxyArg to add 
     */
    public void addVMProxyArg(  VMProxyArg vmpa )
    {
        /*
         *  ask if it's a constant : if so, get the value and put into the
         *  local context, otherwise, put the vmpa in our vmproxyhash
         */

        String key = vmpa.getContextReference();

        if ( vmpa.isConstant() )
        {
            localcontext.put( key, vmpa.getObject( wrappedContext ) );
        }
        else
        {
            vmproxyhash.put( key, vmpa );
        }
    }

    /**
     *  Impl of the Context.put() method. 
     *
     *  @param key name of item to set
     *  @param value object to set to key
     *  @return old stored object
     */
    public Object put(String key, Object value)
    {
        /*
         *  first see if this is a vmpa
         */

        VMProxyArg vmpa = (VMProxyArg) vmproxyhash.get( key );

        if( vmpa != null)
        {
            return vmpa.setObject( wrappedContext, value );
        }
        else
        {
            if(localcontextscope)
            {
                /*
                 *  if we have localcontextscope mode, then just 
                 *  put in the local context
                 */

                return localcontext.put( key, value );
            }
            else
            {
                /*
                 *  ok, how about the local context?
                 */
  
                if (localcontext.containsKey( key ))
                {
                    return localcontext.put( key, value);
                }
                else
                {
                    /*
                     * otherwise, let them push it into the 'global' context
                     */

                    return innerContext.put( key, value );   
                }
            }
        }
    }

    /**
     *  Impl of the Context.gut() method. 
     *
     *  @param key name of item to get
     *  @return  stored object or null
     */
    public Object get( String key )
    {
        /*
         * first, see if it's a VMPA
         */
        
        Object o = null;
        
        VMProxyArg vmpa = (VMProxyArg) vmproxyhash.get( key );
        
        if( vmpa != null )
        {
            o = vmpa.getObject( wrappedContext );
        }
        else
        {
            if(localcontextscope)
            {
                /*
                 * if we have localcontextscope mode, then just 
                 * put in the local context
                 */

                o =  localcontext.get( key );
            }
            else
            {
                /*
                 *  try the local context
                 */
            
                o = localcontext.get( key );
                
                if ( o == null)
                {
                    /*
                     * last chance
                     */

                    o = innerContext.get( key );
                }
            }
        }
       
        return o;
    }
 
    /**
     *  not yet impl
     */
    public boolean containsKey(Object key)
    {
        return false;
    }
  
    /**
     *  impl badly
     */
    public Object[] getKeys()
    {
        return vmproxyhash.keySet().toArray();
    }

    /**
     *  impl badly
     */
    public Object remove(Object key)
    {
        return vmproxyhash.remove( key );
    }

    public void pushCurrentTemplateName( String s )
    {
        innerContext.pushCurrentTemplateName( s );
    }

    public void popCurrentTemplateName()
    {
        innerContext.popCurrentTemplateName();
    }
   
    public String getCurrentTemplateName()
    {
        return innerContext.getCurrentTemplateName();
    }

    public Object[] getTemplateNameStack()
    {
        return innerContext.getTemplateNameStack();
    }

    public IntrospectionCacheData icacheGet( Object key )
    {
        return innerContext.icacheGet( key );
    }
   
    public void icachePut( Object key, IntrospectionCacheData o )
    {
        innerContext.icachePut( key, o );
    }

    public EventCartridge attachEventCartridge( EventCartridge ec )
    {
        return innerContext.attachEventCartridge( ec );
    }

    public EventCartridge getEventCartridge()
    {
        return innerContext.getEventCartridge();
    }


    public void setCurrentResource( Resource r )
    {
        innerContext.setCurrentResource( r );
    }

    public Resource getCurrentResource()
    {
        return innerContext.getCurrentResource();
    }
}



