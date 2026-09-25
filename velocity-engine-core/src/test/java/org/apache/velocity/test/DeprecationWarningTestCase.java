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
import org.apache.velocity.exception.MathException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.test.misc.TestVelocityEngine;

import java.util.Properties;

/**
 * Tests the gated VTL syntax deprecation warnings (VELOCITY-995):
 * <ul>
 *   <li>{@code parser.allow_hyphen_in_identifiers = true}</li>
 *   <li>a lone backslash right before the closing quote of a double-quoted string</li>
 *   <li>{@code runtime.strict_math = false}
 * </ul>
 * and the settings the next major version does not provide any more:
 * <ul>
 *   <li>{@code velocimacro.enable_bc_mode}</li>
 *   <li>an implicitly loaded macro library</li>
 *   <li>{@code runtime.immutable_ranges = false}</li>
 *   <li>{@code parser.class} and the {@code parser.char.dollar} build property</li>
 * </ul>
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

    /* ---- the '|' alternate value: supported, never warns ---- */

    public void testPipeAlternateValueNeverWarns()
    {
        assertNoWarn("${foo|'bar'}");
        assertNoWarn("$!{foo|'bar'}");
        assertNoWarn("${foo.bar()[1]|'bar'}");
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

    /* ---- the word spellings of the operators: supported, never warn ---- */

    public void testTextualOperatorsNeverWarn()
    {
        context.put("a", 2);
        context.put("b", 3);
        assertNoWarn("#if($a and $b)x#end");
        assertNoWarn("#if($a or $b)x#end");
        assertNoWarn("#if(not $a)x#end");
        assertNoWarn("#if($a eq $b)x#end");
        assertNoWarn("#if($a ne $b)x#end");
        assertNoWarn("#if($a lt $b)x#end");
        assertNoWarn("#if($a le $b)x#end");
        assertNoWarn("#if($a gt $b)x#end");
        assertNoWarn("#if($a ge $b)x#end");
        assertNoWarn("#set($x = $a lt $b)");
    }

    /* ---- a lone backslash right before the closing quote of a double-quoted string ---- */

    /** the authored VTL is #set($s = "a\") - a backslash against the closing quote */
    private static final String TRAILING_BACKSLASH = "#set($s = \"a\\\")";

    public void testTrailingBackslashInDoubleQuotesWarns()
    {
        assertWarns(TRAILING_BACKSLASH);
        /* #m("a\") - a macro argument warns just the same */
        assertWarns("#macro(m $x)$x#end#m(\"a\\\")");
        /* "$foo\" - an interpolated literal warns just the same */
        assertWarns("#set($s = \"$foo\\\")");
    }

    public void testTrailingBackslashWarningNamesTheSingleQuotedRecipe()
    {
        String out = warningsFor(TRAILING_BACKSLASH);
        assertTrue("the warning must name the single-quoted spelling, log was:\n" + out,
                   out.contains("use a single-quoted string instead ('a\\')"));
    }

    public void testEvenRunOfBackslashesNeverWarns()
    {
        /* "a\\" - both backslashes are kept literally here and \\ escapes a backslash there */
        assertNoWarn("#set($s = \"a\\\\\")");
        /* "a\\\\" */
        assertNoWarn("#set($s = \"a\\\\\\\\\")");
    }

    public void testOtherStringsNeverWarn()
    {
        /* "a\nb", "a\\b", "ab", 'a\', 'a\\' */
        assertNoWarn("#set($s = \"a\\nb\")");
        assertNoWarn("#set($s = \"a\\\\b\")");
        assertNoWarn("#set($s = \"ab\")");
        assertNoWarn("#set($s = '')");
        assertNoWarn("#set($s = \"\")");
        assertNoWarn("#set($s = 'a\\')");
        assertNoWarn("#set($s = 'a\\\\')");
    }

    public void testOneWarningPerDoubleQuotedLiteral()
    {
        /* "a\" then 'b\' then "c\" - only the two double-quoted ones count */
        String out = warningsFor("#set($s = \"a\\\")#set($t = 'b\\')#set($u = \"c\\\")");
        assertEquals("one warning per double-quoted literal expected, log was:\n" + out,
                     2, out.split("right before the closing quote", -1).length - 1);
    }

    public void testTrailingBackslashSilentWhenDeprecationOff()
    {
        String out = warningsWithoutSetting(TRAILING_BACKSLASH, "false");
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains("deprecated"));
    }

    /** the warning changes nothing: the backslash is still a literal backslash here */
    public void testTrailingBackslashStillRendersTheBackslash()
    {
        assertEvalEquals("a\\", "#set($s = \"a\\\")$s");
        assertEvalEquals("a\\\\", "#set($s = \"a\\\\\")$s");
    }

    /* ---- lenient math (runtime.strict_math = false): deprecated, warned once at engine init ---- */

    /** the init-time warning, whichever of its two wordings applies */
    private static final String LENIENT_MATH = "lenient math is deprecated";

    private VelocityEngine mathEngine(boolean warn, String strictMath)
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        ve.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.RUNTIME_DEPRECATION_WARN, String.valueOf(warn));
        if (strictMath != null)
        {
            ve.setProperty(RuntimeConstants.STRICT_MATH, strictMath);
        }
        return ve;
    }

    private int occurrences(String needle, String out)
    {
        return out.split(needle, -1).length - 1;
    }

    public void testLenientMathWarnsAtInitWhenDefaulted()
    {
        String out = warningsAtInit(mathEngine(true, null));
        assertTrue("the default must warn, log was:\n" + out, out.contains(LENIENT_MATH));
        assertTrue("the warning must name the way out, log was:\n" + out,
                   out.contains("runtime.strict_math is false"));
        assertEquals("one warning per engine init expected, log was:\n" + out,
                     1, occurrences(LENIENT_MATH, out));
    }

    public void testLenientMathWarnsAtInitWhenSetExplicitly()
    {
        String out = warningsAtInit(mathEngine(true, "false"));
        assertEquals("one warning per engine init expected, log was:\n" + out,
                     1, occurrences(LENIENT_MATH, out));
    }

    public void testStrictMathSilentAtInit()
    {
        String out = warningsAtInit(mathEngine(true, "true"));
        assertFalse("strict math has nothing to warn about, log was:\n" + out,
                    out.contains(LENIENT_MATH));
    }

    public void testLenientMathSilentAtInitWhenDeprecationOff()
    {
        String out = warningsAtInit(mathEngine(false, null));
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains(LENIENT_MATH));
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
        String out = warningsWithoutSetting(TRAILING_BACKSLASH, null);
        assertTrue("a warning is expected with runtime.deprecation.warn left alone, log was:\n" + out,
                   out.contains("deprecated"));
    }

    public void testExplicitFalseSilencesTheWarning() throws Exception
    {
        String out = warningsWithoutSetting(TRAILING_BACKSLASH, "false");
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains("deprecated"));
    }

    /* ---- settings the next major version does not provide: warned once at engine init ---- */

    /**
     * An engine with one setting of interest, and nothing else to warn about: strict math is on,
     * so the lenient-math warning stays out of these logs.
     *
     * @param warn value of runtime.deprecation.warn
     * @param key the setting to apply, or null to apply none
     * @param value its value
     * @return the engine, not initialized yet
     */
    private VelocityEngine settingEngine(boolean warn, String key, String value)
    {
        VelocityEngine ve = new TestVelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        ve.addProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.RUNTIME_DEPRECATION_WARN, String.valueOf(warn));
        if (key != null && value != null)
        {
            ve.setProperty(key, value);
        }
        return ve;
    }

    private void assertWarnsAtInit(String needle, boolean warn, String key, String value)
    {
        String out = warningsAtInit(settingEngine(warn, key, value));
        assertTrue("expected a deprecation warning naming '" + needle + "', log was:\n" + out,
                   out.contains(needle) && out.contains("deprecated"));
    }

    private void assertSilentAtInit(String needle, boolean warn, String key, String value)
    {
        String out = warningsAtInit(settingEngine(warn, key, value));
        assertFalse("unexpected deprecation warning naming '" + needle + "', log was:\n" + out,
                    out.contains(needle));
    }

    /* ---- velocimacro.enable_bc_mode: deprecated without replacement ---- */

    public void testBcModeWarnsWhenEnabled()
    {
        assertWarnsAtInit(RuntimeConstants.VM_ENABLE_BC_MODE, true, RuntimeConstants.VM_ENABLE_BC_MODE, "true");
    }

    public void testBcModeWarningSaysWithoutReplacement()
    {
        String out = warningsAtInit(settingEngine(true, RuntimeConstants.VM_ENABLE_BC_MODE, "true"));
        assertTrue("the warning must say there is no replacement, log was:\n" + out,
                   out.contains("without replacement"));
    }

    public void testBcModeSilentWhenDisabled()
    {
        assertSilentAtInit(RuntimeConstants.VM_ENABLE_BC_MODE, true, RuntimeConstants.VM_ENABLE_BC_MODE, "false");
    }

    public void testBcModeSilentWhenDefaulted()
    {
        assertSilentAtInit(RuntimeConstants.VM_ENABLE_BC_MODE, true, null, null);
    }

    public void testBcModeSilentWhenDeprecationOff()
    {
        assertSilentAtInit(RuntimeConstants.VM_ENABLE_BC_MODE, false, RuntimeConstants.VM_ENABLE_BC_MODE, "true");
    }


    /* ---- an implicitly loaded macro library: deprecated, declare it instead ---- */

    private static final String OLD_DEFAULT_LIBRARY = "pre-2.1 default name";

    /**
     * Initializes an engine whose loader can serve a library under the given name, which the engine
     * then looks for on its own unless a library is declared.
     *
     * @param warn value of runtime.deprecation.warn
     * @param libraryName the name the library is served under
     * @param declaredPath value of velocimacro.library.path, or null to leave it unset
     * @return what was logged at init
     */
    private String warningsAtInitWithLibrary(boolean warn, String libraryName, String declaredPath)
    {
        StringResourceRepository repository = StringResourceLoader.getRepository();
        repository.putStringResource(libraryName, "#macro(fromlibrary)x#end");
        try
        {
            VelocityEngine ve = settingEngine(warn, RuntimeConstants.VM_LIBRARY, declaredPath);
            return warningsAtInit(ve);
        }
        finally
        {
            repository.removeStringResource(libraryName);
        }
    }

    public void testOldDefaultMacroLibraryNameWarns()
    {
        String out = warningsAtInitWithLibrary(true, RuntimeConstants.OLD_VM_LIBRARY_DEFAULT, null);
        assertTrue("a library found under the pre-2.1 name must warn, log was:\n" + out,
                   out.contains(OLD_DEFAULT_LIBRARY) && out.contains(RuntimeConstants.OLD_VM_LIBRARY_DEFAULT));
        assertTrue("the warning must say how to fix it, log was:\n" + out,
                   out.contains(RuntimeConstants.VM_LIBRARY_DEFAULT) && out.contains(RuntimeConstants.VM_LIBRARY));
    }

    public void testDefaultMacroLibraryNameNeverWarns()
    {
        // velocimacros.vtl is still searched for in the next major version: nothing to warn about
        String out = warningsAtInitWithLibrary(true, RuntimeConstants.VM_LIBRARY_DEFAULT, null);
        assertFalse("the current default name has nothing to warn about, log was:\n" + out,
                    out.contains(OLD_DEFAULT_LIBRARY));
    }

    public void testDeclaredMacroLibraryNeverWarns()
    {
        String out = warningsAtInitWithLibrary(true, RuntimeConstants.OLD_VM_LIBRARY_DEFAULT, RuntimeConstants.OLD_VM_LIBRARY_DEFAULT);
        assertFalse("a declared library has nothing to warn about, log was:\n" + out,
                    out.contains(OLD_DEFAULT_LIBRARY));
    }

    public void testOldDefaultMacroLibrarySilentWhenNoneIsThere()
    {
        String out = warningsAtInit(settingEngine(true, null, null));
        assertFalse("no library, no warning, log was:\n" + out, out.contains(OLD_DEFAULT_LIBRARY));
    }

    public void testOldDefaultMacroLibrarySilentWhenDeprecationOff()
    {
        String out = warningsAtInitWithLibrary(false, RuntimeConstants.OLD_VM_LIBRARY_DEFAULT, null);
        assertFalse("no warning expected with runtime.deprecation.warn = false, log was:\n" + out,
                    out.contains(OLD_DEFAULT_LIBRARY));
    }


    /* ---- runtime.immutable_ranges = false: deprecated, ranges are always immutable ---- */

    public void testMutableRangesWarn()
    {
        assertWarnsAtInit(RuntimeConstants.IMMUTABLE_RANGES, true, RuntimeConstants.IMMUTABLE_RANGES, "false");
    }

    public void testImmutableRangesSilent()
    {
        assertSilentAtInit(RuntimeConstants.IMMUTABLE_RANGES, true, RuntimeConstants.IMMUTABLE_RANGES, "true");
        assertSilentAtInit(RuntimeConstants.IMMUTABLE_RANGES, true, null, null);
    }

    public void testMutableRangesSilentWhenDeprecationOff()
    {
        assertSilentAtInit(RuntimeConstants.IMMUTABLE_RANGES, false, RuntimeConstants.IMMUTABLE_RANGES, "false");
    }



    /* ---- the parser settings: deprecated, a pluggable lexer replaces them ---- */

    /** the distinctive words of each of the two wordings */
    private static final String PLUGGABLE_LEXER = "pluggable lexer";
    private static final String DOLLAR_FIXED = "will not be configurable";

    /** the shipped parser: the setting has to name a usable class for the engine to initialize */
    @SuppressWarnings("deprecation")
    private static final String STANDARD_PARSER = RuntimeConstants.DEFAULT_PARSER_CLASS;

    @SuppressWarnings("deprecation")
    public void testParserClassWarnsWhenSet()
    {
        String out = warningsAtInit(settingEngine(true, RuntimeConstants.PARSER_CLASS, STANDARD_PARSER));
        assertTrue("setting the parser class must warn, log was:\n" + out,
                   out.contains("deprecated") && out.contains("'parser.class'"));
        assertTrue("the warning must say what replaces it, log was:\n" + out,
                   out.contains(PLUGGABLE_LEXER));
    }

    /** the dollar is not configurable at all in the next major version */
    public void testDollarSigilWarnsWhenSet()
    {
        String out = warningsAtInit(settingEngine(true, "parser.char.dollar", "$"));
        assertTrue("setting 'parser.char.dollar' must warn, log was:\n" + out,
                   out.contains("deprecated") && out.contains("'parser.char.dollar'"));
        assertTrue("the warning must say the dollar stays fixed, log was:\n" + out,
                   out.contains(DOLLAR_FIXED));
    }

    /** the other build properties of the parser generation are not runtime settings: no warning */
    public void testOtherSigilKeysNeverWarn()
    {
        for (String key : new String[] { "parser.char.hash", "parser.char.at", "parser.char.asterisk" })
        {
            String out = warningsAtInit(settingEngine(true, key, "%"));
            assertFalse("no warning expected for '" + key + "', log was:\n" + out,
                        out.contains("'" + key + "'"));
        }
    }

    /** nothing is shipped for either of them: an engine that asked for none has nothing to warn about */
    public void testParserSettingsSilentWhenDefaulted()
    {
        String out = warningsAtInit(settingEngine(true, null, null));
        assertFalse("no parser warning expected, log was:\n" + out,
                    out.contains(PLUGGABLE_LEXER) || out.contains(DOLLAR_FIXED));
    }

    @SuppressWarnings("deprecation")
    public void testParserSettingsSilentWhenDeprecationOff()
    {
        assertSilentAtInit(PLUGGABLE_LEXER, false, RuntimeConstants.PARSER_CLASS, STANDARD_PARSER);
        assertSilentAtInit(DOLLAR_FIXED, false, "parser.char.dollar", "$");
    }

}
