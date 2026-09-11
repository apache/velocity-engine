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
 * Tests the gated VTL syntax deprecation warnings (VELOCITY-995): the {@code |}
 * spelling of the alternate value, the extra {@code $} of {@code ${$foo}}, and the
 * {@code parser.allow_hyphen_in_identifiers} option. The warning fires at parse/init
 * time and is independent of whether the reference resolves, so the templates need no
 * context.
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

    /* ---- notations that are NOT deprecated: regression pins that nothing warns ----
     *
     * The informal $foo.bar notation was deprecated by 2.5-RC1 and is not any more:
     * it renders out of the box on the next major version's compatibility surface.
     */

    public void testReferenceNotationsNeverWarn()
    {
        assertNoWarn("$foo");
        assertNoWarn("$!foo");
        assertNoWarn("$foo.bar");
        assertNoWarn("$foo.bar()");
        assertNoWarn("$!foo.bar");
        assertNoWarn("${foo.bar}");
        assertNoWarn("$!{foo.bar}");
        assertNoWarn("#set($x = \"$foo.bar\")");
        assertNoWarn("#set($x = '$foo.bar')");
        assertNoWarn("#if($foo.bar)#end");
        assertNoWarn("#foreach($x in $foo.bar)#end");
    }

    /* ---- the '|' spelling of the alternate value: deprecated in favour of '?:' ---- */

    public void testPipeAlternateValueWarns()
    {
        assertWarns("${foo|'bar'}");
        assertWarns("$!{foo|'bar'}");
        assertWarns("${foo.bar()[1]|'bar'}");
    }

    public void testPipeWarningNamesTheElvisSpelling()
    {
        String out = warningsFor("${foo|'bar'}");
        assertTrue("the warning must point at the '?:' spelling, log was:\n" + out,
                   out.contains("${foo?:alt}"));
    }

    public void testElvisAlternateValueNeverWarns()
    {
        assertNoWarn("${foo?:'bar'}");
        assertNoWarn("$!{foo?:'bar'}");
        assertNoWarn("${foo.bar()[1]?:'bar'}");
    }

    public void testOnlyThePipeSpellingWarnsWhenBothAreMixed()
    {
        String out = warningsFor("${foo?:'a'}${bar|'b'}${baz?:'c'}");
        assertEquals("exactly one warning expected, log was:\n" + out,
                     1, out.split("alternate-value notation is deprecated", -1).length - 1);
    }

    /* ---- the extra '$' after '{' (${$foo}, re-added in 2.5 for 1.7 BC): supported, never warns ---- */

    public void testExtraDollarNeverWarns()
    {
        assertNoWarn("${$foo}");
        assertNoWarn("${$foo.bar}");
        assertNoWarn("$!{$foo}");
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

    /* ---- the word spellings of the operators: deprecated in favour of the symbols ---- */

    /** so that the comparisons have something to compare, keeping the log to warnings */
    private void operands()
    {
        context.put("a", 2);
        context.put("b", 3);
    }

    private int warningCount(String vtl)
    {
        String out = warningsFor(vtl);
        return out.split("textual operator", -1).length - 1;
    }

    public void testTextualOperatorsWarn()
    {
        operands();
        assertWarns("#if($a and $b)x#end");
        assertWarns("#if($a or $b)x#end");
        assertWarns("#if(not $a)x#end");
        assertWarns("#if($a eq $b)x#end");
        assertWarns("#if($a ne $b)x#end");
        assertWarns("#if($a lt $b)x#end");
        assertWarns("#if($a le $b)x#end");
        assertWarns("#if($a gt $b)x#end");
        assertWarns("#if($a ge $b)x#end");
        assertWarns("#set($x = $a lt $b)");
    }

    public void testTextualOperatorWarningNamesTheSymbol()
    {
        operands();
        String out = warningsFor("#if($a and $b)x#end");
        assertTrue("the warning must name the '&&' spelling, log was:\n" + out,
                   out.contains("the textual operator 'and' is deprecated; write '&&' instead"));

        out = warningsFor("#if($a ge $b)x#end");
        assertTrue("the warning must name the '>=' spelling, log was:\n" + out,
                   out.contains("the textual operator 'ge' is deprecated; write '>=' instead"));
    }

    public void testSymbolOperatorsNeverWarn()
    {
        operands();
        assertNoWarn("#if($a && $b)x#end");
        assertNoWarn("#if($a || $b)x#end");
        assertNoWarn("#if(!$a)x#end");
        assertNoWarn("#if($a == $b)x#end");
        assertNoWarn("#if($a != $b)x#end");
        assertNoWarn("#if($a < $b)x#end");
        assertNoWarn("#if($a <= $b)x#end");
        assertNoWarn("#if($a > $b)x#end");
        assertNoWarn("#if($a >= $b)x#end");
        assertNoWarn("#set($x = $a + $b * 2 - 1)");
        assertNoWarn("#set($x = \"and or not gt\")");
        assertNoWarn("#set($x = $a)#if($x)x#end");
    }

    public void testOneWarningPerTextualOperatorOccurrence()
    {
        operands();
        context.put("c", 4);
        assertEquals("one warning per operator expected",
                     3, warningCount("#if($a and $b and not $c)x#end"));
        assertEquals("only the word spellings count",
                     1, warningCount("#if($a && $b or $a == $b)x#end"));
        assertEquals("one warning per operator expected, nesting included",
                     2, warningCount("#if(not ($a gt $b))x#end"));
    }

    public void testTextualOperatorWarnsFromATemplateFile()
    {
        operands();
        addTemplate("wordops.vm", "#if($a and $b)x#end");
        log.startCapture();
        assertTmplEquals("x", "wordops.vm");
        log.stopCapture();
        String out = log.getLog();
        assertTrue("a template file must warn just like evaluate(), log was:\n" + out,
                   out.contains("the textual operator 'and' is deprecated"));
        assertTrue("the warning must point at the operator itself, log was:\n" + out,
                   out.contains("wordops.vm [line 1, column 8]"));
    }

    public void testTextualOperatorSilentWhenDeprecationOff()
    {
        operands();
        String out = warningsWithoutSetting("#if($a and not $b)x#end", "false");
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains("deprecated"));
    }

    /* ---- on by default, silenced explicitly ---- */

    private String warningsWithoutSetting(String vtl, String deprecationWarn)
    {
        VelocityEngine plain = new VelocityEngine();
        plain.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        plain.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        plain.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        if (deprecationWarn != null)
        {
            plain.setProperty(RuntimeConstants.RUNTIME_DEPRECATION_WARN, deprecationWarn);
        }
        plain.init();

        log.startCapture();
        evaluate(vtl, plain);
        log.stopCapture();
        return log.getLog();
    }

    public void testWarningIsOnByDefault() throws Exception
    {
        String out = warningsWithoutSetting("${baz|'x'}", null);
        assertTrue("a warning is expected with runtime.deprecation.warn left alone, log was:\n" + out,
                   out.contains("deprecated"));
    }

    public void testExplicitFalseSilencesTheWarning() throws Exception
    {
        String out = warningsWithoutSetting("${baz|'x'}${$foo}", "false");
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains("deprecated"));
    }
}
