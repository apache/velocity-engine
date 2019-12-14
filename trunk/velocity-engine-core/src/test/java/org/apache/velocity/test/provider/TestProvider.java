package org.apache.velocity.test.provider;

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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * This class is used by the testbed. Instances of the class
 * are fed into the context that is set before the AST
 * is traversed and dynamic content generated.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class TestProvider
{
    String title = "lunatic";
    boolean state;
    Object ob = null;

    public static String PUB_STAT_STRING = "Public Static String";

    int stateint = 0;


    public String getName()
    {
        return "jason";
    }

    public Stack getStack()
    {
        Stack stack = new Stack();
        stack.push("stack element 1");
        stack.push("stack element 2");
        stack.push("stack element 3");
        return stack;
    }

    public List getEmptyList()
    {
        return new ArrayList();
    }

    public List getList()
    {
        List list = new ArrayList();
        list.add("list element 1");
        list.add("list element 2");
        list.add("list element 3");

        return list;
    }

    public Hashtable getSearch()
    {
        Hashtable h = new Hashtable();
        h.put("Text", "this is some text");
        h.put("EscText", "this is escaped text");
        h.put("Title", "this is the title");
        h.put("Index", "this is the index");
        h.put("URL", "http://periapt.com");

        ArrayList al = new ArrayList();
        al.add(h);

        h.put("RelatedLinks", al);

        return h;
    }

    public Hashtable getHashtable()
    {
        Hashtable h = new Hashtable();
        h.put("key0", "value0");
        h.put("key1", "value1");
        h.put("key2", "value2");

        return h;
    }

    public ArrayList getRelSearches()
    {
        ArrayList al = new ArrayList();
        al.add(getSearch());

        return al;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Object[] getMenu()
    {
        //ArrayList al = new ArrayList();
        Object[] menu = new Object[3];
        for (int i = 0; i < 3; i++)
        {
            Hashtable item = new Hashtable();
            item.put("id", "item" + Integer.toString(i+1));
            item.put("name", "name" + Integer.toString(i+1));
            item.put("label", "label" + Integer.toString(i+1));
            //al.add(item);
            menu[i] = item;
        }

        //return al;
        return menu;
    }

    public ArrayList getCustomers()
    {
        ArrayList list = new ArrayList();

        list.add("ArrayList element 1");
        list.add("ArrayList element 2");
        list.add("ArrayList element 3");
        list.add("ArrayList element 4");

        return list;
    }

    public ArrayList getCustomers2()
    {
        ArrayList list = new ArrayList();

        list.add(new TestProvider());
        list.add(new TestProvider());
        list.add(new TestProvider());
        list.add(new TestProvider());

        return list;
    }

    public Object me()
    {
        return this;
    }

    public String toString()
    {
        return ("test provider");
    }

    public Vector getVector()
    {
        Vector list = new Vector();

        list.addElement("vector element 1");
        list.addElement("vector element 2");

        return list;
    }

    public String[] getArray()
    {
        String[] strings = new String[2];
        strings[0] = "first element";
        strings[1] = "second element";
        return strings;
    }

    public boolean theAPLRules()
    {
        return true;
    }

    public boolean getStateTrue()
    {
        return true;
    }

    public boolean getStateFalse()
    {
        return false;
    }

    public String objectArrayMethod(Object[] o)
    {
        return "result of objectArrayMethod";
    }

    public String concat(Object[] strings)
    {
        StringBuilder result = new StringBuilder();

        for (Object string : strings)
        {
            result.append((String) string).append(' ');
        }

        return result.toString();
    }

    public String concat(List strings)
    {
        StringBuilder result = new StringBuilder();

        for (Object string : strings)
        {
            result.append((String) string).append(' ');
        }

        return result.toString();
    }

    public String objConcat(List objects)
    {
        StringBuilder result = new StringBuilder();

        for (Object object : objects)
        {
            result.append(object).append(' ');
        }

        return result.toString();
    }

    public String parse(String a, Object o, String c, String d)
    {
        return a + o.toString() + c + d;
    }

    public String concat(String a, String b)
    {
        return a + b;
    }

    // These two are for testing subclasses.

    public Person getPerson()
    {
        return new Person();
    }

    public Child getChild()
    {
        return new Child();
    }

    public String showPerson(Person person)
    {
        return person.getName();
    }

    /**
     * Chop i characters off the end of a string.
     *
     * @param string String to chop.
     * @param i Number of characters to chop.
     * @return String with processed answer.
     */
    public String chop(String string, int i)
    {
        return(string.substring(0, string.length() - i));
    }

    public boolean allEmpty(Object[] list)
    {
        int size = list.length;

        for (Object aList : list)
            if (aList.toString().length() > 0)
                return false;

        return true;
    }

    /*
     * This can't have the signature
     *
     *    public void setState(boolean state)
     *
     *    or dynamically invoking the method
     *    doesn't work ... you would have to
     *    put a wrapper around a method for a
     *    real boolean property that takes a
     *    Boolean object if you wanted this to
     *    work. Not really sure how useful it
     *    is anyway. Who cares about boolean
     *    values you can just set a variable.
     *
     */

    public void setState(Boolean state)
    {
    }

    public void setBangStart( Integer i )
    {
        System.out.println("SetBangStart() : called with val = " + i );
        stateint = i;
    }
    public Integer bang()
    {
        System.out.println("Bang! : " + stateint );
        Integer ret = stateint;
        stateint++;
        return ret;
    }

    /**
     * Test the ability of vel to use a get(key)
     * method for any object type, not just one
     * that implements the Map interface.
     */
    public String get(String key)
    {
        return key;
    }

    /**
     * Test the ability of vel to use a put(key)
     * method for any object type, not just one
     * that implements the Map interface.
     */
    public String put(String key, Object o)
    {
        ob = o;
        return key;
    }

    public String getFoo()
        throws Exception
    {
        throw new Exception("From getFoo()");
    }

    public String getThrow()
        throws Exception
    {
       throw new Exception("From getThrow()");
    }
}
