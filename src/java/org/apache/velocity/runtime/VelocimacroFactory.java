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

/**
 *  VelocimacroFactory.java
 *
 *   manages the set of VMs in a running Velocity engine.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocimacroFactory.java,v 1.3 2000/12/10 04:52:51 geirm Exp $ 
 *
 */

package org.apache.velocity.runtime;

import java.util.TreeMap;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.VelocimacroManager;

public class VelocimacroFactory
{
    private VelocimacroManager vmManager_ = new VelocimacroManager();

    private boolean bReplaceAllowed_ = false;
    private boolean bAddNewAllowed_ = true;
    private boolean bTemplateLocal_ = false;

    /** name of global Velocimacro library template */
    private static String GLOBAL_LIBRARY = "velocimacro.library.global";

    /** name of local Velocimacro library template */
    private static String LOCAL_LIBRARY  = "velocimacro.library.local";

    /** boolean (true/false) default true : allow inline (in-template) macro definitions */
    private static String VM_PERM_ALLOW_INLINE  = "velocimacro.permissions.allowInline";

    /** boolean (true/false) default false : allow inline (in-template) macro definitions to replace existing */
    public final static String VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL  = "velocimacro.permissions.allowInlineToReplaceGlobal";
    
    /** switch for forcing inline macros to be local */
    public final static String VM_PERM_INLINE_LOCAL = "velocimacro.permissions.allowInlineLocalScope";
    
    /**
     *    setup
     */
    public void initVelocimacro()
    {
        /*
         *  maybe I'm just paranoid...
         */

        synchronized( this )
        {
            /*
             *   allow replacements while we add the libraries, if exist
             */
            
            setReplacementPermission( true );

            /*
             *  add all library macros to the global namespace
             */

           vmManager_. setNamespaceUsage( false );
        
            /*
             *  now, if there is a global or local libraries specified, use them.
             *  All we have to do is get the template. The template will be parsed;
             *  VM's  are added during the parse phase
             */
            
            String strLib = Runtime.getString( GLOBAL_LIBRARY, "");
            
            if (  !strLib.equals("") ) 
            {
                try {
                    Runtime.info("Adding VMs from global VM library template : " + strLib );
                    Template template = Runtime.getTemplate( strLib );   
                    Runtime.info("Global VM library template macro registration complete." );
                 } catch (Exception e) {
                    Runtime.info("Error using global VM library template "+ strLib + " : " + e );
                }
            }
            else
                Runtime.info("Velocimacro : no global VM library template used.");

            strLib = Runtime.getString( LOCAL_LIBRARY, "");
            
            if ( !strLib.equals("") ) 
            {
                try {
                    Runtime.info("Adding VMs from local VM library template : " + strLib );
                    Template template = Runtime.getTemplate(strLib);
                    Runtime.info("Local VM library template macro registration complete.");
                } catch ( Exception e ) {
                    Runtime.info("Error using local VM library template "+ strLib + " : " + e );
                }
            }
            else
                Runtime.info("Velocimacro : no local VM library template used.");
   

            /*
             *   now, the permissions
             */
            
            /*
             *  allowinline : anything after this will be an inline macro, I think
             *  there is the question if a #include is an inline, and I think so
             *
             *  default = true
             */
            
            setAddMacroPermission( true );
            
            
            if ( !Runtime.getBoolean( VM_PERM_ALLOW_INLINE, true) )
            {
                setAddMacroPermission( false );
                Runtime.info("Velocimacro : allowInline = false : VMs can not be defined inline in templates");
            }
            else
                Runtime.info("Velocimacro : allowInline = true : VMs can be defined inline in templates");

            /*
             *  allowInlineToReplaceGlobal : allows an inline VM , if allowed at all,
             *  to replace an existing global VM
             *
             *  default = false
             */
            
            setReplacementPermission( false );
            
            if ( Runtime.getBoolean( VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, false) )
            {
                setReplacementPermission( true );
                Runtime.info("Velocimacro : allowInlineToOverride = true : VMs defined inline may replace previous VM definitions");
            }
            else
               Runtime.info("Velocimacro : allowInlineToOverride = false : VMs defined inline may NOT replace previous VM definitions");

            /*
             *  now turn on namespace handling as far as permissions allow in the manager, and also set it
             *  here for gating purposes
             */
           
            vmManager_.setNamespaceUsage( true );

            if (Runtime.getBoolean(  VM_PERM_INLINE_LOCAL, false ))
            {
                setTemplateLocal( true );
                Runtime.info("Velocimacro : allowInlineLocal = true : VMs defined inline will be local to their defining template only.");
            }
            else
                Runtime.info("Velocimacro : allowInlineLocal = false : VMs defined inline will be  global in scope if allowed.");
 
        }

        Runtime.info("Velocimacro initialized.");
        return;
    }

    /**
     *   adds a macro to the factory. 
     */
    public boolean addVelocimacro( String strName, String strMacro, String strArgArray[], String strMacroArray[], 
                                   TreeMap tmArgIndexMap, String strSourceTemplate )
    {
        /*
         *  maybe we should throw an exception, maybe just tell the caller like this...
         * 
         *  I hate this : maybe exceptions are in order here...
         */
        
        if ( strName == null || strMacro == null | strArgArray == null || strMacroArray == null || tmArgIndexMap == null )
            return false;
        
        /*
         *  maybe the rules should be in manager?  I dunno. It's to manage the namespace issues
         *
         *  first, are we allowed to add VMs at all?  This trumps all.
         */

        if ( !bAddNewAllowed_ )
            return false;

        /*
         *  are they local in scope?  Then it is ok to add.
         */

        if (!bTemplateLocal_  )
        {
            /* 
             * otherwise, if we have it already in global namespace, and they can't replace
             * since local templates are not allowed, the global namespace is implied.
             *  remember, we don't know anything about namespace managment here, so lets
             *  note do anything fancy like trying to give it the global namespace here
             *
             *  so if we have it, and we aren't allowed to replace, bail
             */
            
            if ( isVelocimacro( strName, strSourceTemplate ) && !bReplaceAllowed_ )
                return false;
        }

        /*
         *  seems like all is good.  Lets do it.
         */

        synchronized( this ) 
        {
            vmManager_.addVM( strName, strMacro, strArgArray, strMacroArray, tmArgIndexMap, strSourceTemplate );
        }

        return true;
    }

    /**
     *   tells the world if a given directive string is a Velocimacro
     */
    public boolean isVelocimacro( String vm , String strSourceTemplate )
    {
        synchronized( this ) 
        {
            /*
             *  first we check the locals to see if we have a local definition for this template
             */
            
            if (vmManager_.get( vm, strSourceTemplate ) != null)
                return true;
        }

        return false;
    }

    /**
     *  actual factory : creates a Directive that will
     *  behave correctly wrt getting the framework to 
     *  dig out the correct # of args
     */
    public Directive getVelocimacro( String strVMName, String strSourceTemplate )
    {
        synchronized( this ) 
        {
            if ( isVelocimacro( strVMName, strSourceTemplate ) ) 
            {    
                return  vmManager_.get( strVMName, strSourceTemplate );
            }
        }

        /*
         *  wasn't a VM.  Sorry...
         */
        
        return null;
    }

    /**
     *  tells the vmManager to dump the specified namespace
     */
    public boolean dumpVMNamespace( String strNamespace )
    {
        return vmManager_.dumpNamespace( strNamespace );
    }

    /**
     *  sets permission to have VMs local in scope to their declaring template
     *  note that this is really taken care of in the VMManager class, but
     *  we need it here for gating purposes in addVM
     *  eventually, I will slide this all into the manager, maybe.
     */   
    private void setTemplateLocal( boolean b )
    {
        bTemplateLocal_ = b;
    }

    /**
     *   sets the permission to add new macros
     */
    private boolean setAddMacroPermission( boolean bAddNewAllowed )
    {
        boolean b = bAddNewAllowed_;
        
        bAddNewAllowed_ = bAddNewAllowed;
        return b;
    }

    /**
     *    sets the permission for allowing addMacro() calls to 
     *    replace existing VM's
     */
    private boolean setReplacementPermission( boolean bReplacementAllowed )
    {
        boolean b = bReplaceAllowed_;
        
        bReplaceAllowed_ = bReplacementAllowed;
        return b;
    }

}







