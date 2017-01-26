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

import org.apache.velocity.runtime.RuntimeConstants;

import java.util.ArrayList;
/**
 * Test index syntax e.g, $foo[1]
 */
public class IndexTestCase extends BaseTestCase
{
    public IndexTestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);

        context.put("NULL", null);
        context.put("red", "blue");

        int[] a = {1, 2, 3};
        context.put("a", a);
        String[] str = {"a", "ab", "abc"};
        context.put("str", str);

        ArrayList alist = new ArrayList();
        alist.add(new Integer(1));
        alist.add(new Integer(2));
        alist.add(new Integer(3));
        alist.add(a);
        alist.add(null);
        context.put("alist", alist);

        Foo foo = new Foo();
        foo.bar = alist;
        context.put("foo", foo);

        Boo boo = new Boo();
        context.put("boo", boo);
    }

    public void testCallingIndex()
    {
        assertEvalEquals("1 -3-", "$a[0] -$a[ 2 ]-");
        assertEvalEquals("x21", "#set($i = 1)x$a[$i]1");
        assertEvalEquals("3", "$a[ $a[ $a[0] ] ]");
        assertEvalEquals("ab", "$str[1]");
        assertEvalEquals("123", "$alist[0]$alist[1]$alist[2]");
        assertEvalEquals("1][2-[3]", "$alist[0]][$alist[$a[0]]-[$alist[2]]");

        assertEvalEquals("2", "$alist[3][1]");
        assertEvalEquals("3 [1]", "$alist[2] [1]");

        assertEvalEquals("4", "#set($bar = [3, 4, 5])$bar[1]");
        assertEvalEquals("21", "#set($i = 1)#set($bar = [3, $a[$i], $a[0]])$bar[1]$bar[2]");

        assertEvalEquals("2", "$foo.bar[1]");
        assertEvalEquals("2", "$foo.getBar()[1]");
        assertEvalEquals("2", "$foo.getBar()[3][1]");

        assertEvalEquals(" a  ab  abc ", "#foreach($i in $foo.bar[3]) $str[$foreach.index] #end");

        assertEvalEquals("apple", "#set($hash = {'a':'apple', 'b':'orange'})$hash['a']");

        assertEvalEquals("xx ", "#if($alist[4] == $NULL)xx#end #if($alist[4])yy#end");

        assertEvalEquals("BIG TRUEaBIG FALSE", "$foo[true]a$foo[false]");
        assertEvalEquals("junk foobar ", "$foo[\"junk\"]");
        assertEvalEquals("GOT NULL", "#set($i=$NULL)$boo[$i]");

        assertEvalEquals("321", "$a[-1]$a[ -2]$a[-3 ]");
        assertEvalEquals("67xx", "#set($hash={1:11, 5:67, 23:2})$hash[5]$!hash[6]#if(!$hash[1000])xx#end");

        // Some cases that should be evaluated as text
        assertEvalEquals("[]", "[]");
        assertEvalEquals("$[]", "$[]");
        assertEvalEquals("$.[]", "$.[]");
        assertEvalEquals("$[1]", "$[1]");
        assertEvalEquals("$[1]1", "$[1]1");
        assertEvalEquals("$1[1]1", "$1[1]1");
        assertEvalEquals("blue.[]", "$red.[]");
        assertEvalEquals("blue[]", "${red}[]");
        assertEvalEquals("blue][", "$red][");
        assertEvalEquals("1[]", "${a[0]}[]");
        assertEvalEquals("1a$[]", "$a[0]a$[]");
        assertEvalEquals("$![]", "$![]");
        assertEvalEquals("$\\![]", "$\\![]");
    }

    public void testIndexSetting()
    {
        assertEvalEquals("foo", "#set($str[1] = \"foo\")$str[1]");
        assertEvalEquals("5150", "#set($alist[1] = 5150)$alist[1]");
        assertEvalEquals("15", "$alist[3][0]#set($alist[3][0] = 5)$alist[3][0]");
        assertEvalEquals("orange","#set($blaa = {\"apple\":\"grape\"})#set($blaa[\"apple\"] = \"orange\")$blaa[\"apple\"]");
        assertEvalEquals("null","#set($str[0] = $NULL)#if($str[0] == $NULL)null#end");
        assertEvalEquals("null","#set($blaa = {\"apple\":\"grape\"})#set($blaa[\"apple\"] = $NULL)#if($blaa[\"apple\"] == $NULL)null#end");
        assertEvalEquals("2112", "#set($a[-1] = 2112)$a[2]");
        assertEvalEquals("3344","#set($hash = {1:11, 2:22, 5:66})#set($hash[2]=33)#set($hash[3]=44)$hash[2]$hash[3]");
    }


    public void testErrorHandling()
    {
        assertEvalExceptionAt("$boo['throwex']", 1, 5);
        assertEvalExceptionAt("$boo[]", 1, 6);
        assertEvalExceptionAt("$boo[blaa]", 1, 6);
        assertEvalExceptionAt("#set($foo[1] = 3)", 1, 10);
        assertEvalExceptionAt("$a[500]", 1, 3);
    }


    public static class Foo
    {
        public Object bar = null;
        public Object getBar()
        {
            return bar;
        }

        public String get(Boolean bool)
        {
            if (bool.booleanValue())
                return "BIG TRUE";
            else
                return "BIG FALSE";
        }

        public String get(String str)
        {
            return str + " foobar ";
        }
    }

    public static class Boo
    {
        public Object get(Object obj)
        {
            if (obj == null)
              return "GOT NULL";
            else if (obj.equals("throwex"))
                throw new RuntimeException("Generated Exception");

            return obj;
        }
    }
}
