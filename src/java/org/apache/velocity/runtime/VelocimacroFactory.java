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
 * @version $Id: VelocimacroFactory.java,v 1.2 2000/12/06 05:58:57 geirm Exp $ 
 *
 */

package org.apache.velocity.runtime;

import java.util.Hashtable;
import java.util.TreeMap;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.Template;

public class VelocimacroFactory
{
    private Hashtable hMacros_ = new Hashtable();
    private boolean bReplaceAllowed_ = false;
    private boolean bAddNewAllowed_ = true;
    private Object  obLock = new Object();

    /** name of global Velocimacro library template */
    private static String GLOBAL_LIBRARY = "velocimacro.library.global";

    /** name of local Velocimacro library template */
    private static String LOCAL_LIBRARY  = "velocimacro.library.local";

    /** boolean (true/false) default true : allow inline (in-template) macro definitions */
    private static String PERM_ALLOW_INLINE  = "velocimacro.permissions.allowInline";

    /** boolean (true/false) default false : allow inline (in-template) macro definitions to replace existing */
    private static String PERM_ALLOW_INLINE_OVERRIDE  = "velocimacro.permissions.allowInlineToOverride";

    /**
     *    setup
     */
    public void initVelocimacro()
    {
        /*
         *  maybe I'm just paranoid...
         */

        synchronized( obLock )
        {
            /*
             *   allow replacements while we add the libraries, if exist
             */
            
            setReplacementPermission( true );
        
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
            
            strLib = Runtime.getString( PERM_ALLOW_INLINE, "");
            
            if ( strLib.equals("false"))
            {
                setAddMacroPermission( false );
                Runtime.info("Velocimacro : allowInline = false : VMs can not be defined inline in templates");
            }
            else
                Runtime.info("Velocimacro : allowInline = true : VMs can be defined inline in templates");

            /*
             *  allowInlineToOverride : allows an inline, if allowed at all
             *  to replace an existing VM
             *
             *  default = false
             */
            
            setReplacementPermission( false );
            
            strLib = Runtime.getString( PERM_ALLOW_INLINE_OVERRIDE, "");
            
            if (strLib.equals("true"))
            {
                setReplacementPermission( true );
                Runtime.info("Velocimacro : allowInlineToOverride = true : VMs defined inline may replace previous VM definitions");
            }
            else
               Runtime.info("Velocimacro : allowInlineToOverride = false : VMs defined inline may NOT replace previous VM definitions");
        }

        Runtime.info("Velocimacro initialized.");
        return;
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

    /**
     *   adds a macro to the factory.  Lots of room for improvement here...
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
         *   if exists, need to see if allowed to replace
         */
        
        if ( isVelocimacro( strName ) && !bReplaceAllowed_ )
            return false;
        
        if ( !bAddNewAllowed_ )
            return false;
        /*
         *  ok. Just make one
         */
        
        Hashtable h = new Hashtable();
        h.put("macroname", strName );
        h.put("argarray",  strArgArray );
        h.put("macroarray",  strMacroArray );
        h.put("indexmap", tmArgIndexMap );
        h.put("macrobody", strMacro);
        h.put("sourcetemplate", strSourceTemplate );

        synchronized( obLock) {
            if (!isVelocimacro( strName ))
                hMacros_.put( strName, h );
        }

        return true;
    }

    /**
     *   tells the world if a given directive string is a Velocimacro
     */
    public boolean isVelocimacro( String vm )
    {
        synchronized( obLock ) {
            if (hMacros_.get( vm ) != null)
                return true;
        }

        return false;
    }

    /**
     *  actual factory : creates a Directive that will
     *  behave correctly wrt getting the framework to 
     *  dig out the correct # of args
     */
    public Directive getVelocimacro( String strVMName )
    {
        synchronized( obLock ) 
        {
            if ( isVelocimacro( strVMName ) ) 
                {    
                    Hashtable h = (Hashtable) hMacros_.get( strVMName );
                    
                    VelocimacroProxy vp = new VelocimacroProxy();
                     
                    vp.setName( (String) h.get("macroname"));
                    vp.setArgArray(  (String []) h.get("argarray") ); 
                    vp.setMacroArray( (String [] ) h.get("macroarray"));
                    vp.setArgIndexMap( (TreeMap) h.get("indexmap"));
                    vp.setMacrobody( (String) h.get("macrobody"));

                    return vp;
                }
        }

        /*
         *  wasn't a VM.  Sorry...
         */
        
        return null;
    }
}







