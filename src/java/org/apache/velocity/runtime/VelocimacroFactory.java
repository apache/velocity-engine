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
 * @version $Id: VelocimacroFactory.java,v 1.6 2000/12/11 03:08:02 geirm Exp $ 
 *
 */

package org.apache.velocity.runtime;

import java.util.TreeMap;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.VelocimacroManager;
import org.apache.velocity.runtime.RuntimeConstants;

public class VelocimacroFactory
{
    private VelocimacroManager vmManager = new VelocimacroManager();

    private boolean replaceAllowed = false;
    private boolean addNewAllowed = true;
    private boolean templateLocal = false;
    private boolean blather = false;

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
            Runtime.info("Velocimacro : initialization starting.");
 
            /*
             *   allow replacements while we add the libraries, if exist
             */
            
            setReplacementPermission( true );
            setBlather( true );

            /*
             *  add all library macros to the global namespace
             */

           vmManager.setNamespaceUsage( false );
        
            /*
             *  now, if there is a global or local libraries specified, use them.
             *  All we have to do is get the template. The template will be parsed;
             *  VM's  are added during the parse phase
             */
            
            String strLib = Runtime.getString( RuntimeConstants.VM_GLOBAL_LIBRARY, "");
            
            if (  !strLib.equals("") ) 
            {
                try {
                    Runtime.info("Velocimacro : adding VMs from global VM library template : " + strLib );
                    Template template = Runtime.getTemplate( strLib );   
                    Runtime.info("Velocimacro : global VM library template macro registration complete." );
                 } catch (Exception e) {
                    Runtime.info("Velocimacro : error using global VM library template "+ strLib + " : " + e );
                }
            }
            else
                Runtime.info("Velocimacro : no global VM library template used.");

            strLib = Runtime.getString(  RuntimeConstants.VM_LOCAL_LIBRARY, "");
            
            if ( !strLib.equals("") ) 
            {
                try {
                    Runtime.info("Velocimacro : adding VMs from local VM library template : " + strLib );
                    Template template = Runtime.getTemplate(strLib);
                    Runtime.info("Velocimacro : local VM library template macro registration complete.");
                } catch ( Exception e ) {
                    Runtime.info("Velocimacro : error using local VM library template "+ strLib + " : " + e );
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
                        
            if ( !Runtime.getBoolean(  RuntimeConstants.VM_PERM_ALLOW_INLINE, true) )
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
            
            if ( Runtime.getBoolean(  RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, false) )
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
           
            vmManager.setNamespaceUsage( true );

            /*
             *  template-local inline VM mode : default is off
             */

            setTemplateLocalInline( Runtime.getBoolean(RuntimeConstants.VM_PERM_INLINE_LOCAL, false) );
        
            if ( getTemplateLocalInline() )
                Runtime.info("Velocimacro : allowInlineLocal = true : VMs defined inline will be local to their defining template only.");
            else
                Runtime.info("Velocimacro : allowInlineLocal = false : VMs defined inline will be  global in scope if allowed.");
 
            vmManager.setTemplateLocalInlineVM( getTemplateLocalInline() );

            /*
             *  general message switch.  default is on
             */

            setBlather( Runtime.getBoolean( RuntimeConstants.VM_MESSAGES_ON, true ));
        
            if (getBlather())
                Runtime.info("Velocimacro : messages on  : VM system will output information messages");
            else
                Runtime.info("Velocimacro : messages off : VM system will be quiet");

            Runtime.info("Velocimacro : initialization complete.");
        }
    
        return;
    }

    /**
     *   adds a macro to the factory. 
     */
    public boolean addVelocimacro( String name, String macroBody, String argArray[], String macroArray[], 
                                   TreeMap argIndexMap, String sourceTemplate )
    {
        /*
         *  maybe we should throw an exception, maybe just tell the caller like this...
         * 
         *  I hate this : maybe exceptions are in order here...
         */
        
        if ( name == null || macroBody == null | argArray == null || macroArray == null || argIndexMap == null )
        {
            logVMMessage("Velocimacro : VM addition rejected : programmer error : arg null"  );
            return false;
        }
        
        /*
         *  maybe the rules should be in manager?  I dunno. It's to manage the namespace issues
         *
         *  first, are we allowed to add VMs at all?  This trumps all.
         */

        if ( !addNewAllowed )
        {
            logVMMessage("Velocimacro : VM addition rejected : " + name + " : inline VMs not allowed."  );
            return false;
        }

        /*
         *  are they local in scope?  Then it is ok to add.
         */

        if (!templateLocal  )
        {
            /* 
             * otherwise, if we have it already in global namespace, and they can't replace
             * since local templates are not allowed, the global namespace is implied.
             *  remember, we don't know anything about namespace managment here, so lets
             *  note do anything fancy like trying to give it the global namespace here
             *
             *  so if we have it, and we aren't allowed to replace, bail
             */
            
            if ( isVelocimacro( name, sourceTemplate ) && !replaceAllowed )
            {
                logVMMessage("Velocimacro : VM addition rejected : " + name + " : inline not allowed to replace existing VM"  );
                return false;
            }
        }

        /*
         *  seems like all is good.  Lets do it.
         */

        synchronized( this ) 
        {
            vmManager.addVM( name, macroBody, argArray, macroArray, argIndexMap, sourceTemplate );
        }

        /*
         *  if we are to blather, blather...
         */

        if ( blather)
        {
            String s = "#" +  argArray[0];
            s += "(";
        
            for( int i=1; i < argArray.length; i++)
                {
                    s += " ";
                    s += argArray[i];
                }
            s += " ) : source = ";
            s += sourceTemplate;
            
           logVMMessage( "Velocimacro : added new VM : " + s );
        }

        return true;
    }

    /**
     *  localization of the logging logic
     */
    private void logVMMessage( String s )
    {
        if (blather)
            Runtime.info( s );
    }

    /**
     *   tells the world if a given directive string is a Velocimacro
     */
    public boolean isVelocimacro( String vm , String sourceTemplate )
    {
        synchronized( this ) 
        {
            /*
             *  first we check the locals to see if we have a local definition for this template
             */
            
            if (vmManager.get( vm, sourceTemplate ) != null)
                return true;
        }

        return false;
    }

    /**
     *  actual factory : creates a Directive that will
     *  behave correctly wrt getting the framework to 
     *  dig out the correct # of args
     */
    public Directive getVelocimacro( String vmName, String sourceTemplate )
    {
        synchronized( this ) 
        {
            if ( isVelocimacro( vmName, sourceTemplate ) ) 
            {    
                return  vmManager.get( vmName, sourceTemplate );
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
    public boolean dumpVMNamespace( String namespace )
    {
        return vmManager.dumpNamespace( namespace );
    }

    /**
     *  sets permission to have VMs local in scope to their declaring template
     *  note that this is really taken care of in the VMManager class, but
     *  we need it here for gating purposes in addVM
     *  eventually, I will slide this all into the manager, maybe.
     */   
    private void setTemplateLocalInline( boolean b )
    {
        templateLocal = b;
    }

    private boolean getTemplateLocalInline()
    {
        return templateLocal;
    }

    /**
     *   sets the permission to add new macros
     */
    private boolean setAddMacroPermission( boolean arg )
    {
        boolean b = addNewAllowed;
        
        addNewAllowed = arg;
        return b;
    }

    /**
     *    sets the permission for allowing addMacro() calls to 
     *    replace existing VM's
     */
    private boolean setReplacementPermission( boolean arg )
    {
        boolean b = replaceAllowed;
        
        replaceAllowed = arg;
        return b;
    }

    /**
     *  set output message mode 
     */
    private void setBlather( boolean b )
    {
        blather = b;
    }

    private boolean getBlather()
    {
        return blather;
    }
}







