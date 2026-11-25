package org.apache.velocity.test;

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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import java.io.StringWriter;

/**
 * Which inline macro a call reaches when several templates of one merge define macros, under
 * <code>velocimacro.inline.local_scope</code> and <code>velocimacro.inline.replace_global</code>.
 *
 * <p>With local scope, an inline macro belongs to its merge instead of the engine. A call written in a template
 * asks, in this order:</p>
 * <ol>
 * <li>that template's own macros;</li>
 * <li>if inline macros replace global ones, the other templates of the merge, the latest entered first;</li>
 * <li>the engine-wide macros (the libraries);</li>
 * <li>the other templates of the merge, the latest entered first.</li>
 * </ol>
 * <p>The templates of a merge are its root and what was <code>#parse</code>d so far.</p>
 */
public class InlineMacroScopeTestCase extends BaseTestCase
{
    private static final String LIBRARY = "lib.vm";

    /** whether the engines of this test reload their macro libraries */
    private boolean autoreload = false;

    public InlineMacroScopeTestCase(String name)
    {
        super(name);
    }

    /**
     * Merges <code>p.vm</code> on a new engine.
     * @param templates names and sources, alternating; <code>lib.vm</code>, when listed, is the macro library
     */
    private String merge(boolean localScope, boolean replaceGlobal, String... templates)
    {
        VelocityEngine ve = createEngine();
        ve.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, localScope);
        ve.setProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, replaceGlobal);
        ve.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, autoreload);
        // the library is read when the engine starts: its repository is filled before
        StringResourceRepository repository = new StringResourceRepositoryImpl();
        for (int i = 0; i < templates.length; i += 2)
        {
            repository.putStringResource(templates[i], templates[i + 1]);
            if (LIBRARY.equals(templates[i]))
            {
                ve.setProperty(RuntimeConstants.VM_LIBRARY, LIBRARY);
            }
        }
        ve.setApplicationAttribute(stringRepoName, repository);
        StringWriter writer = new StringWriter();
        ve.getTemplate("p.vm").merge(new VelocityContext(), writer);
        return writer.toString();
    }

    private String local(String... templates)
    {
        return merge(true, false, templates);
    }

    private String localReplacing(String... templates)
    {
        return merge(true, true, templates);
    }

    /**
     * A parsed template's macros reach what is rendered after it, and the root's reach what it parses.
     */
    public void testRootMacroReachesParsedTemplates()
    {
        // what a parsed template defines
        assertEquals("C", local("p.vm", "#parse('c.vm')", "c.vm", "#macro(m)C#end#parse('g.vm')", "g.vm", "#m()"));
        assertEquals("#m()||C", local("p.vm", "#m()|#parse('c.vm')|#m()", "c.vm", "#macro(m)C#end"));
        assertEquals("|C", local("p.vm", "#parse('c.vm')|#parse('d.vm')", "c.vm", "#macro(m)C#end", "d.vm", "#m()"));

        // what the root defines (not shared before 2.5)
        assertEquals("P", local("p.vm", "#macro(m)P#end#parse('c.vm')", "c.vm", "#m()"));
        assertEquals("P", local("p.vm", "#macro(m)P#end#parse('c.vm')", "c.vm", "#parse('g.vm')", "g.vm", "#m()"));
        assertEquals("[A]", local("p.vm", "#macro(a)A#end#parse('c.vm')#b()", "c.vm", "#macro(b)[#a()]#end"));
        assertEquals("[P]|[P]|[P]", local(
            "p.vm", "#macro(a)[#b()]#end#macro(b)P#end#a()|#parse('c.vm')|#a()", "c.vm", "#macro(b)C#end#a()"));
    }

    /**
     * The root is the first template of its merge: a later one is asked before it.
     */
    public void testRootMacroIsAskedLast()
    {
        assertEquals("P|G|G|P", local(
            "p.vm", "#macro(m)P#end#parse('c.vm')|#m()", "c.vm", "#m()|#parse('g.vm')|#m()", "g.vm", "#macro(m)G#end#m()"));
    }

    /**
     * A template's own macro wins inside that template, whether or not inline macros replace global ones.
     */
    public void testOwnMacroWinsOverRoot()
    {
        String[] templates = { "p.vm", "#macro(m)P#end#m()|#parse('c.vm')|#m()", "c.vm", "#macro(m)C#end#m()" };
        assertEquals("P|C|P", local(templates));
        assertEquals("P|C|P", localReplacing(templates));
    }

    /**
     * Replacing global macros gives the root no rank over the other templates of the merge.
     */
    public void testRootHasNoRankWhenReplacing()
    {
        assertEquals("|C", localReplacing(
            "p.vm", "#macro(m)P#end#parse('c.vm')|#parse('d.vm')", "c.vm", "#macro(m)C#end", "d.vm", "#m()"));
        assertEquals("P|G|G|P", localReplacing(
            "p.vm", "#macro(m)P#end#parse('c.vm')|#m()", "c.vm", "#m()|#parse('g.vm')|#m()", "g.vm", "#macro(m)G#end#m()"));
    }

    /**
     * A library macro calls another one: any template of the merge may replace the second, not only the root.
     */
    public void testParsedTemplateReplacesLibraryMacro()
    {
        String library = "#macro(a)[#b()]#end#macro(b)L#end";

        // the root's replaces it
        assertEquals("[P]", localReplacing(LIBRARY, library, "p.vm", "#macro(b)P#end#a()"));

        // and so does a parsed template's (the root alone could before 2.5)
        assertEquals("[L]|[C]|[C]", localReplacing(
            LIBRARY, library, "p.vm", "#a()|#parse('c.vm')|#a()", "c.vm", "#macro(b)C#end#a()"));
        assertEquals("[P]|[C]|[C]", localReplacing(
            LIBRARY, library, "p.vm", "#macro(b)P#end#a()|#parse('c.vm')|#a()", "c.vm", "#macro(b)C#end#a()"));
        assertEquals("L|C|C", localReplacing(
            LIBRARY, "#macro(m)L#end", "p.vm", "#m()|#parse('c.vm')|#m()", "c.vm", "#macro(m)C#end#m()"));
    }

    /**
     * What held before 2.5 as well.
     */
    public void testWhatHolds()
    {
        // without local scope, one engine-wide namespace: the first definition stays, or the later one replaces it
        String[] twice = { "p.vm", "#macro(m)P#end#m()|#parse('c.vm')|#m()", "c.vm", "#macro(m)C#end#m()" };
        assertEquals("P|P|P", merge(false, false, twice));
        assertEquals("P|C|C", merge(false, true, twice));

        // with local scope, a template's own macro wins over the library's inside that template only
        assertEquals("P|L", local(LIBRARY, "#macro(m)L#end", "p.vm", "#macro(m)P#end#m()|#parse('c.vm')", "c.vm", "#m()"));
        assertEquals("P|P", localReplacing(LIBRARY, "#macro(m)L#end", "p.vm", "#macro(m)P#end#m()|#parse('c.vm')", "c.vm", "#m()"));

        // and the library's wins over another template's unless inline macros replace global ones
        assertEquals("L|C|L", local(LIBRARY, "#macro(m)L#end", "p.vm", "#m()|#parse('c.vm')|#m()", "c.vm", "#macro(m)C#end#m()"));
        assertEquals("[L]", local(LIBRARY, "#macro(a)[#b()]#end#macro(b)L#end", "p.vm", "#macro(b)P#end#a()"));

        // among parsed templates, the latest is asked first
        assertEquals("C|D|D", local("p.vm", "#parse('c.vm')|#parse('d.vm')|#m()", "c.vm", "#macro(m)C#end#m()", "d.vm", "#macro(m)D#end#m()"));
    }

    /**
     * Reloading the macro libraries does not change which macro a call reaches.
     */
    public void testLibraryAutoreloadKeepsTheOrder() throws Exception
    {
        autoreload = true;
        String[] templates = { LIBRARY, "#macro(m)L#end", "p.vm", "#macro(m)P#end#m()|#parse('c.vm')", "c.vm", "#m()" };
        assertEquals("P|L", local(templates));
        assertEquals("[L]", local(LIBRARY, "#macro(a)[#b()]#end#macro(b)L#end", "p.vm", "#macro(b)P#end#a()"));

        // nor does it for a lookup which names the root template: it has no rank unless inline macros replace global ones
        RuntimeInstance ri = new RuntimeInstance();
        ri.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ri.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        ri.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        ri.addProperty("resource.loader.string.repository.name", stringRepoName);
        ri.addProperty("resource.loader.string.repository.static", "false");
        ri.setProperty(RuntimeConstants.VM_LIBRARY, LIBRARY);
        ri.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, true);
        ri.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, true);
        StringResourceRepository repository = new StringResourceRepositoryImpl();
        for (int i = 0; i < templates.length; i += 2)
        {
            repository.putStringResource(templates[i], templates[i + 1]);
        }
        ri.setApplicationAttribute(stringRepoName, repository);
        ri.init();
        Template root = ri.getTemplate("p.vm");
        Template child = ri.getTemplate("c.vm");
        assertSame(ri.getVelocimacro("m", null, child), ri.getVelocimacro("m", root, child));
    }
}
