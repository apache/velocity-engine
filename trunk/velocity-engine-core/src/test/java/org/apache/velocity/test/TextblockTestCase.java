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
 
import org.apache.velocity.runtime.parser.node.ASTTextblock;

/**
 * This class tests the Textblock directive.
 */
public class TextblockTestCase extends BaseTestCase
{
    // these are all here so that the test case adapts instantly
    // to changes in the textblock start/end sequences
    private static final String START = ASTTextblock.START;
    private static final String END = ASTTextblock.END;
    private static final String PARTIAL_START = START.substring(0, START.length() - 1);
    private static final String PARTIAL_END = END.substring(1, END.length());
    private static final String END_OF_START = START.substring(START.length() - 1, START.length());
    private static final String START_OF_END = END.substring(0, 1);

    public TextblockTestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public String textblock(String s)
    {
        return START + s + END;
    }

    public void assertTextblockEvalEquals(String s) throws Exception
    {
        assertEvalEquals(s, textblock(s));
    }

    /**
     * https://issues.apache.org/jira/browse/VELOCITY-661
     */
    public void testTextblockAjaxcode() throws Exception
    {
        String s = "var myId = 'someID';$('#test).append($.template('<div id=\"${myId}\"></div>').apply({myId: myId}));";
        assertEvalEquals(s + " 123", textblock(s)+" #foreach($i in [1..3])$i#end");
    }

    public void testLooseTextblockEnd() throws Exception
    {
        // just like a multi-line comment end (*#), this must be
        // followed by a character.  by itself, it bombs for some reason.
        assertEvalEquals(END+" ", END+" ");
    }

    public void testTextblockStartInTextblock() throws Exception
    {
        assertTextblockEvalEquals(START);
    }

    public void testTextblockEndBetweenTwoTextblockHalves() throws Exception
    {
        // just like a multi-line comment end (*#), the end token
        // in the middle must be followed by some character.
        // by itself, it bombs.  not sure why that is, but the
        // same has been true of multi-line comments without complaints,
        // so i'm not going to worry about it just yet.
        assertEvalEquals(" "+END+"  ", textblock(" ")+END+" "+textblock(" "));
    }

    public void testZerolengthTextblock() throws Exception
    {
        assertTextblockEvalEquals("");
    }

    public void testTextblockInsideForeachLoop() throws Exception
    {
        String s = "var myId = 'someID';$('#test).append($.template('<div id=\"${myId}\"></div>').apply({myId: myId}));";
        assertEvalEquals("1 "+s+"2 "+s+"3 "+s, "#foreach($i in [1..3])$i "+ textblock(s) + "#end");
    }

    public void testSingleHashInsideTextblock() throws Exception
    {
        assertTextblockEvalEquals(" # ");
    }

    public void testDollarInsideTextblock() throws Exception
    {
        assertTextblockEvalEquals("$");
    }

    public void testTextblockInsideComment() throws Exception
    {
        String s = "FOOBAR";
        assertEvalEquals("", "#* comment "+textblock(s) + " *#");
    }

    public void testPartialStartEndTokensInsideTextblock() throws Exception
    {
        assertTextblockEvalEquals(PARTIAL_START+"foo"+PARTIAL_END);
    }

    public void testDupeTokenChars() throws Exception
    {
        assertTextblockEvalEquals(END_OF_START+START_OF_END);
        assertTextblockEvalEquals(END_OF_START+END_OF_START+START_OF_END+START_OF_END);
        assertTextblockEvalEquals(END_OF_START+END_OF_START+"#"+START_OF_END+START_OF_END);
    }

    /**
     * https://issues.apache.org/jira/browse/VELOCITY-584
     */
    public void testServerSideIncludeEscaping() throws Exception
    {
        assertTextblockEvalEquals("<!--#include file=\"wisdom.inc\"--> ");
    }
    
    /**
     * https://issues.apache.org/jira/browse/VELOCITY-676
     */
    public void testLineCommentInsideTextblock() throws Exception
    {
        assertTextblockEvalEquals("##x");
    }

}
