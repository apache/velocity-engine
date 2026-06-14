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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

/**
 * Tests the gated VTL syntax deprecation warnings (VELOCITY-995): the informal
 * {@code $foo.bar} notation in output position, and the {@code |} default-value
 * operator. The warning fires at parse/init time and is independent of whether the
 * reference resolves, so the templates need no context.
 */
public class DeprecationWarningTestCase extends BaseTestCase
{
    public DeprecationWarningTestCase(String name)
    {
        super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(RuntimeConstants.RUNTIME_DEPRECATION_WARN, "true");
    }

    private String warningsFor(String vtl)
    {
        log.startCapture();
        evaluate(vtl);
        log.stopCapture();
        return log.getLog();
    }

    private void assertWarns(String vtl)
    {
        String out = warningsFor(vtl);
        assertTrue("expected a deprecation warning for [" + vtl + "], log was:\n" + out,
                   out.contains("deprecated"));
    }

    private void assertNoWarn(String vtl)
    {
        String out = warningsFor(vtl);
        assertFalse("unexpected deprecation warning for [" + vtl + "], log was:\n" + out,
                    out.contains("deprecated"));
    }

    /* ---- informal notation in output position: deprecated ---- */

    public void testInformalDottedInOutputWarns()
    {
        assertWarns("$foo.bar");
        assertWarns("$foo.bar()");
        assertWarns("$!foo.bar");          // quiet informal still informal
    }

    public void testBareReferenceNeverWarns()
    {
        assertNoWarn("$foo");
        assertNoWarn("$!foo");
    }

    public void testFormalNotationNeverWarns()
    {
        assertNoWarn("${foo.bar}");
        assertNoWarn("$!{foo.bar}");       // quiet + formal: formal flag must be set
    }

    /* ---- informal notation outside output position: safe ---- */

    public void testInformalSafeInExpressionContexts()
    {
        assertNoWarn("#set($x = $foo.bar)");          // set RHS
        assertNoWarn("#if($foo.bar)#end");            // directive argument
        assertNoWarn("#foreach($x in $foo.bar)#end"); // directive argument
    }

    public void testSingleQuotedStringIsInert()
    {
        assertNoWarn("#set($x = '$foo.bar')");
    }

    public void testInterpolatedStringCountsAsOutput()
    {
        assertWarns("#set($x = \"$foo.bar\")");
    }

    /* ---- the '|' default-value operator: always deprecated ---- */

    public void testPipeDefaultWarns()
    {
        assertWarns("${foo|'bar'}");
    }

    /* ---- the extra '$' after '{' (${$foo}): always deprecated, but still formal ---- */

    public void testExtraDollarWarns()
    {
        assertWarns("${$foo}");
        assertWarns("${$foo.bar}");
        assertWarns("$!{$foo}");
    }

    public void testExtraDollarStillFormalNoInformalWarning()
    {
        // ${$foo.bar} must warn once (extra '$'), not also as informal notation
        String out = warningsFor("${$foo.bar}");
        assertTrue(out.contains("extra '$'"));
        assertFalse(out.contains("informal"));
    }

    public void testFormalWithoutExtraDollarNeverWarns()
    {
        assertNoWarn("${foo}");
        assertNoWarn("${foo.bar}");
    }

    /* ---- the parser.allow_hyphen_in_identifiers option: deprecated when enabled ---- */

    private VelocityEngine hyphenEngine(boolean warn, boolean hyphen)
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        ve.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.RUNTIME_DEPRECATION_WARN, String.valueOf(warn));
        ve.setProperty(RuntimeConstants.PARSER_HYPHEN_ALLOWED, String.valueOf(hyphen));
        return ve;
    }

    private String warningsAtInit(VelocityEngine ve)
    {
        log.startCapture();
        ve.init();
        log.stopCapture();
        return log.getLog();
    }

    public void testHyphenOptionWarnsWhenEnabled()
    {
        assertTrue(warningsAtInit(hyphenEngine(true, true)).contains("allow_hyphen_in_identifiers"));
    }

    public void testHyphenOptionSilentWhenDisabled()
    {
        assertFalse(warningsAtInit(hyphenEngine(true, false)).contains("allow_hyphen_in_identifiers"));
    }

    public void testHyphenOptionSilentWhenDeprecationOff()
    {
        assertFalse(warningsAtInit(hyphenEngine(false, true)).contains("allow_hyphen_in_identifiers"));
    }

    /* ---- off by default ---- */

    public void testNoWarningWhenPropertyDisabled() throws Exception
    {
        VelocityEngine plain = new VelocityEngine();
        plain.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        plain.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        plain.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        plain.init();

        log.startCapture();
        evaluate("$foo.bar and ${baz|'x'}", plain);
        log.stopCapture();
        assertFalse("no warning expected with runtime.deprecation.warn unset, log was:\n" + log.getLog(),
                    log.getLog().contains("deprecated"));
    }
}
