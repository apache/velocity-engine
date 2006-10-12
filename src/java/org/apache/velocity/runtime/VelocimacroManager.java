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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Hashtable;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.runtime.parser.node.SimpleNode;

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
    private final RuntimeServices rsvc;
    private static String GLOBAL_NAMESPACE = "";

    private boolean registerFromLib = false;

    /** Hash of namespace hashes. */
    private final Hashtable namespaceHash = new Hashtable();

    /** map of names of library tempates/namespaces */
    private final Hashtable libraryMap = new Hashtable();

    /*
     * big switch for namespaces.  If true, then properties control
     * usage. If false, no.
     */
    private boolean namespacesOn = true;
    private boolean  inlineLocalMode = false;

    /**
     * Adds the global namespace to the hash.
     */
    VelocimacroManager(RuntimeServices rsvc)
    {
        this.rsvc = rsvc;

        /*
         *  add the global namespace to the namespace hash. We always have that.
         */

        addNamespace(GLOBAL_NAMESPACE);
    }

    /**
     * Adds a VM definition to the cache.
     * @param vmName Name of the new VelociMacro.
     * @param macroBody String representation of the macro body.
     * @param argArray Array of macro parameters, first parameter is the macro name.
     * @param namespace The namespace/template from which this macro has been loaded.
     * @return Whether everything went okay.
     */
    public boolean addVM(final String vmName, final String macroBody, final String argArray[],
                         final String namespace)
    {
        MacroEntry me = new MacroEntry(vmName, macroBody, argArray,
                                       namespace);

        me.setFromLibrary(registerFromLib);

        /*
         *  the client (VMFactory) will signal to us via
         *  registerFromLib that we are in startup mode registering
         *  new VMs from libraries.  Therefore, we want to
         *  addto the library map for subsequent auto reloads
         */

        boolean isLib = true;

        if (registerFromLib)
        {
           libraryMap.put(namespace, namespace);
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

            isLib = libraryMap.containsKey(namespace);
        }

        if (!isLib && usingNamespaces(namespace))
        {
            /*
             *  first, do we have a namespace hash already for this namespace?
             *  if not, add it to the namespaces, and add the VM
             */

            Hashtable local = getNamespace(namespace, true);
            local.put(vmName, me);

            return true;
        }
        else
        {
            /*
             *  otherwise, add to global template.  First, check if we
             *  already have it to preserve some of the autoload information
             */

            MacroEntry exist = (MacroEntry) getNamespace(GLOBAL_NAMESPACE).get(vmName);

            if (exist != null)
            {
                me.setFromLibrary(exist.getFromLibrary());
            }

            /*
             *  now add it
             */

            getNamespace(GLOBAL_NAMESPACE).put(vmName, me);

            return true;
        }
    }

    /**
     * gets a new living VelocimacroProxy object by the
     * name / source template duple
     * @param vmName Name of the VelocityMacro to look up.
     * @param namespace Namespace in which to look up the macro.
     * @return A proxy representing the Macro.
     */
    public VelocimacroProxy get(final String vmName, final String namespace)
    {

        if (usingNamespaces(namespace))
        {
            Hashtable local =  getNamespace(namespace, false);

            /*
             *  if we have macros defined for this template
             */

            if (local != null)
            {
                MacroEntry me = (MacroEntry) local.get(vmName);

                if (me != null)
                {
                    return me.createVelocimacro(namespace);
                }
            }
        }

        /*
         * if we didn't return from there, we need to simply see
         * if it's in the global namespace
         */

        MacroEntry me = (MacroEntry) getNamespace(GLOBAL_NAMESPACE).get( vmName );

        if (me != null)
        {
            return me.createVelocimacro(namespace);
        }

        return null;
    }

    /**
     * Removes the VMs and the namespace from the manager.
     * Used when a template is reloaded to avoid
     * losing memory.
     *
     * @param namespace namespace to dump
     * @return boolean representing success
     */
    public boolean dumpNamespace(final String namespace)
    {
        synchronized(this)
        {
            if (usingNamespaces(namespace))
            {
                Hashtable h = (Hashtable) namespaceHash.remove(namespace);

                if (h == null)
                {
                    return false;
                }

                h.clear();

                return true;
            }

            return false;
        }
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
     *  returns the hash for the specified namespace.  Will not create a new one
     *  if it doesn't exist
     *
     *  @param namespace  name of the namespace :)
     *  @return namespace Hashtable of VMs or null if doesn't exist
     */
    private Hashtable getNamespace(final String namespace)
    {
        return getNamespace(namespace, false);
    }

    /**
     *  returns the hash for the specified namespace, and if it doesn't exist
     *  will create a new one and add it to the namespaces
     *
     *  @param namespace  name of the namespace :)
     *  @param addIfNew  flag to add a new namespace if it doesn't exist
     *  @return namespace Hashtable of VMs or null if doesn't exist
     */
    private Hashtable getNamespace(final String namespace, final boolean addIfNew)
    {
        Hashtable h = (Hashtable) namespaceHash.get(namespace);

        if (h == null && addIfNew)
        {
            h = addNamespace(namespace);
        }

        return h;
    }

    /**
     *   adds a namespace to the namespaces
     *
     *  @param namespace name of namespace to add
     *  @return Hash added to namespaces, ready for use
     */
    private Hashtable addNamespace(final String namespace)
    {
        Hashtable h = new Hashtable();
        Object oh;

        if ((oh = namespaceHash.put(namespace, h)) != null)
        {
          /*
           * There was already an entry on the table, restore it!
           * This condition should never occur, given the code
           * and the fact that this method is private.
           * But just in case, this way of testing for it is much
           * more efficient than testing before hand using get().
           */
          namespaceHash.put(namespace, oh);
          /*
           * Should't we be returning the old entry (oh)?
           * The previous code was just returning null in this case.
           */
          return null;
        }

        return h;
    }

    /**
     *  determines if currently using namespaces.
     *
     *  @param namespace currently ignored
     *  @return true if using namespaces, false if not
     */
    private boolean usingNamespaces(final String namespace)
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

        if (inlineLocalMode)
        {
            return true;
        }

        return false;
    }

    /**
     * Return the library name for a given macro.
     * @param vmName Name of the Macro to look up.
     * @param namespace Namespace to look the macro up.
     * @return The name of the library which registered this macro in a namespace.
     */
    public String getLibraryName(final String vmName, final String namespace)
    {
        if (usingNamespaces(namespace))
        {
            Hashtable local =  getNamespace(namespace, false);

            /*
             *  if we have this macro defined in this namespace, then
             *  it is masking the global, library-based one, so
             *  just return null
             */

            if ( local != null)
            {
                MacroEntry me = (MacroEntry) local.get(vmName);

                if (me != null)
                {
                    return null;
                }
            }
        }

        /*
         * if we didn't return from there, we need to simply see
         * if it's in the global namespace
         */

        MacroEntry me = (MacroEntry) getNamespace(GLOBAL_NAMESPACE).get(vmName);

        if (me != null)
        {
            return me.getSourceTemplate();
        }

        return null;
    }


    /**
     *  wrapper class for holding VM information
     */
    private class MacroEntry
    {
        private final String vmName;
        private final String[] argArray;
        private final String macroBody;
        private final String sourceTemplate;

        private SimpleNode nodeTree = null;
        private boolean fromLibrary = false;

        private MacroEntry(final String vmName, final String macroBody,
                   final String argArray[], final String sourceTemplate)
        {
            this.vmName = vmName;
            this.argArray = argArray;
            this.macroBody = macroBody;
            this.sourceTemplate = sourceTemplate;
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

        /**
         * Returns the node tree for this macro.
         * @return The node tree for this macro.
         */
        public SimpleNode getNodeTree()
        {
            return nodeTree;
        }

        /**
         * Returns the source template name for this macro.
         * @return The source template name for this macro.
         */
        public String getSourceTemplate()
        {
            return sourceTemplate;
        }

        VelocimacroProxy createVelocimacro(final String namespace)
        {
            VelocimacroProxy vp = new VelocimacroProxy();
            vp.setName(this.vmName);
            vp.setArgArray(this.argArray);
            vp.setMacrobody(this.macroBody);
            vp.setNodeTree(this.nodeTree);
            vp.setNamespace(namespace);
            return vp;
        }

        void setup(final InternalContextAdapter ica)
        {
            /*
             *  if not parsed yet, parse!
             */

            if( nodeTree == null)
            {
                parseTree(ica);
            }
        }

        void parseTree(final InternalContextAdapter ica)
        {
            try
            {
                BufferedReader br = new BufferedReader(new StringReader(macroBody));

                nodeTree = rsvc.parse(br, "VM:" + vmName, true);
                nodeTree.init(ica, null);
            }
            catch (Exception e)
            {
                rsvc.getLog().error("VelocimacroManager.parseTree() failed on VM '"
                                    + vmName + "'", e);
            }
        }
    }
}
