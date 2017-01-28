package org.apache.velocity.runtime;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.Template;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.directive.Macro;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages VMs in namespaces.  Currently, two namespace modes are
 * supported:
 *
 * <ul>
 * <li>flat - all allowable VMs are in the global namespace</li>
 * <li>local - inline VMs are added to it's own template namespace</li>
 * </ul>
 *
 * Thanks to <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 * for some ideas incorporated here.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 * @version $Id$
 */
public class VelocimacroManager
{
    private boolean registerFromLib = false;

    /** reference to global namespace hash */
    private final Map globalNamespace;

    /** set of names of library templates/namespaces */
    private final Map libraries = new ConcurrentHashMap(17, 0.5f, 20);

    private RuntimeServices rsvc = null;

    /*
     * big switch for namespaces.  If true, then properties control
     * usage. If false, no.
     */
    private boolean namespacesOn = true;
    private boolean inlineLocalMode = false;
    private boolean inlineReplacesGlobal = false;

    /**
     * Adds the global namespace to the hash.
     */
    VelocimacroManager(RuntimeServices rsvc)
    {
        /*
         *  add the global namespace to the namespace hash. We always have that.
         */

        globalNamespace = new ConcurrentHashMap(101, 0.5f, 20);
        this.rsvc = rsvc;
    }

    /**
     * Adds a VM definition to the cache.
     *
     * Called by VelocimacroFactory.addVelociMacro (after parsing and discovery in Macro directive)
     *
     * @param vmName Name of the new VelociMacro.
     * @param macroBody String representation of the macro body.
     * @param macroArgs  Array of macro arguments, containing the
     *        #macro() arguments and default values.  the 0th is the name.
     * @param definingTemplate The template from which this macro has been loaded.
     * @param canReplaceGlobalMacro whether this macro can replace a global macro
     * @return Whether everything went okay.
     */
    public boolean addVM(final String vmName, final Node macroBody, List<Macro.MacroArg> macroArgs,
                         final Template definingTemplate, boolean canReplaceGlobalMacro)
    {
        if (macroBody == null)
        {
            // happens only if someone uses this class without the Macro directive
            // and provides a null value as an argument
            throw new VelocityException("Null AST for "+vmName+" in " + definingTemplate.getName());
        }

        MacroEntry me = new MacroEntry(vmName, macroBody, macroArgs, definingTemplate.getName(), rsvc);

        me.setFromLibrary(registerFromLib);

        /*
         *  the client (VMFactory) will signal to us via
         *  registerFromLib that we are in startup mode registering
         *  new VMs from libraries.  Therefore, we want to
         *  addto the library map for subsequent auto reloads
         */

        boolean isLib = true;

        MacroEntry exist = (MacroEntry) globalNamespace.get(vmName);

        if (registerFromLib)
        {
           libraries.put(definingTemplate.getName(), definingTemplate);
        }
        else
        {
            /*
             *  now, we first want to check to see if this namespace (template)
             *  is actually a library - if so, we need to use the global namespace
             *  we don't have to do this when registering, as namespaces should
             *  be shut off. If not, the default value is true, so we still go
             *  global
             */

            isLib = libraries.containsKey(definingTemplate.getName());
        }

        if ( !isLib && usingNamespaces() )
        {
            definingTemplate.getMacros().put(vmName, me);
            return true;
        }
        else
        {
            /*
             *  otherwise, add to global template.  First, check if we
             *  already have it to preserve some of the autoload information
             */


            if (exist != null)
            {
                me.setFromLibrary(exist.getFromLibrary());
            }

            /*
             *  now add it
             */

            globalNamespace.put(vmName, me);

            return true;
        }
    }

    /**
     * Gets a VelocimacroProxy object by the name / source template duple.
     *
     * @param vmName Name of the VelocityMacro to look up.
     * @param renderingTemplate Template we are currently rendering.
     * @param template Source Template.
     * @return A proxy representing the Macro.
     */
    public VelocimacroProxy get(final String vmName, final Template renderingTemplate, final Template template)
    {
        if( inlineReplacesGlobal && renderingTemplate != null )
        {
            /*
             * if VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL is true (local macros can
             * override global macros) and we know which template we are rendering at the
             * moment, check if local namespace contains a macro we are looking for
             * if so, return it instead of the global one
             */

            MacroEntry me = (MacroEntry)renderingTemplate.getMacros().get(vmName);
            if( me != null )
            {
                return me.getProxy();
            }
        }

        if( usingNamespaces() && template != null )
        {
            MacroEntry me = (MacroEntry)template.getMacros().get(vmName);
            if( template.getMacros().size() > 0 && me != null )
            {
                return me.getProxy();
            }
        }

        MacroEntry me = (MacroEntry) globalNamespace.get(vmName);

        if (me != null)
        {
            return me.getProxy();
        }

        return null;
    }

    /**
     *  public switch to let external user of manager to control namespace
     *  usage indep of properties.  That way, for example, at startup the
     *  library files are loaded into global namespace
     *
     * @param namespaceOn True if namespaces should be used.
     */
    public void setNamespaceUsage(final boolean namespaceOn)
    {
        this.namespacesOn = namespaceOn;
    }

    /**
     * Should macros registered from Libraries be marked special?
     * @param registerFromLib True if macros from Libs should be marked.
     */
    public void setRegisterFromLib(final boolean registerFromLib)
    {
        this.registerFromLib = registerFromLib;
    }

    /**
     * Should macros from the same template be inlined?
     *
     * @param inlineLocalMode True if macros should be inlined on the same template.
     */
    public void setTemplateLocalInlineVM(final boolean inlineLocalMode)
    {
        this.inlineLocalMode = inlineLocalMode;
    }

    /**
     *  determines if currently using namespaces.
     *
     *  @return true if using namespaces, false if not
     */
    private boolean usingNamespaces()
    {
        /*
         *  if the big switch turns of namespaces, then ignore the rules
         */

        if (!namespacesOn)
        {
            return false;
        }

        /*
         *  currently, we only support the local template namespace idea
         */

        return inlineLocalMode;

    }

    /**
     * Return the library name for a given macro.
     * @param vmName Name of the Macro to look up.
     * @param template Template
     * @return The name of the library which registered this macro in a namespace.
     */
    public String getLibraryName(final String vmName, Template template)
    {
        if (usingNamespaces())
        {
            /*
             *  if we have this macro defined in this namespace, then
             *  it is masking the global, library-based one, so
             *  just return null
             */
            MacroEntry me = (MacroEntry)template.getMacros().get(vmName);
            if( me != null )
                return null;
        }

        MacroEntry me = (MacroEntry) globalNamespace.get(vmName);

        if (me != null)
        {
            return me.getSourceTemplate();
        }

        return null;
    }

    /**
     * @since 1.6
     */
    public void setInlineReplacesGlobal(boolean is)
    {
        inlineReplacesGlobal = is;
    }


    /**
     *  wrapper class for holding VM information
     */
    private static class MacroEntry
    {
        private final String vmName;
        private final List<Macro.MacroArg> macroArgs;
        private final String sourceTemplate;
        private SimpleNode nodeTree = null;
        private boolean fromLibrary = false;
        private VelocimacroProxy vp;

        private MacroEntry(final String vmName, final Node macro,
                   List<Macro.MacroArg> macroArgs, final String sourceTemplate,
                   RuntimeServices rsvc)
        {
            this.vmName = vmName;
            this.macroArgs = macroArgs;
            this.nodeTree = (SimpleNode)macro;
            this.sourceTemplate = sourceTemplate;

            vp = new VelocimacroProxy();
            vp.setName(this.vmName);
            vp.setMacroArgs(this.macroArgs);
            vp.setNodeTree(this.nodeTree);
            vp.setLocation(macro.getLine(), macro.getColumn(), macro.getTemplate());
            vp.init(rsvc);
        }

        /**
         * Has the macro been registered from a library.
         * @param fromLibrary True if the macro was registered from a Library.
         */
        public void setFromLibrary(final boolean fromLibrary)
        {
            this.fromLibrary = fromLibrary;
        }

        /**
         * Returns true if the macro was registered from a library.
         * @return True if the macro was registered from a library.
         */
        public boolean getFromLibrary()
        {
            return fromLibrary;
        }

        public String getSourceTemplate()
        {
            return sourceTemplate;
        }

        VelocimacroProxy getProxy()
        {
            return vp;
        }
    }
}
