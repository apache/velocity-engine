package org.apache.velocity.context;

/*
 * Copyright 2000,2004 The Apache Software Foundation.
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
 *  @version $Id: VMContext.java,v 1.9.10.1 2004/03/03 23:22:54 geirm Exp $ 
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



