package org.apache.velocity.test.provider;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
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

import java.util.*;

/**
 * This class is used by the testbed. Instances of the class
 * are fed into the context that is set before the AST
 * is traversed and dynamic content generated.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: TestProvider.java,v 1.19 2001/08/07 22:20:29 geirm Exp $
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
        List list = new ArrayList();
        return list;
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
        StringBuffer result = new StringBuffer();
        
        for (int i = 0; i < strings.length; i++)
        {
            result.append((String) strings[i]).append(' ');
        }
        
        return result.toString();
    }

    public String concat(List strings)
    {
        StringBuffer result = new StringBuffer();
        
        for (int i = 0; i < strings.size(); i++)
        {
            result.append((String) strings.get(i)).append(' ');
        }
        
        return result.toString();
    }

    public String objConcat(List objects)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < objects.size(); i++)
        {
            result.append(objects.get(i)).append(' ');
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
        
        for (int i = 0; i < size; i++)
            if (list[i].toString().length() > 0)
                return false;
        
        return true;
    }

    /*
     * This can't have the signature
    
    public void setState(boolean state)
    
    or dynamically invoking the method
    doesn't work ... you would have to
    put a wrapper around a method for a
    real boolean property that takes a 
    Boolean object if you wanted this to
    work. Not really sure how useful it
    is anyway. Who cares about boolean
    values you can just set a variable.
    
    */

    public void setState(Boolean state)
    {
    }

    public void setBangStart( Integer i )
    {
        System.out.println("SetBangStart() : called with val = " + i );
        stateint = i.intValue();
    }
    public Integer bang()
    {
        System.out.println("Bang! : " + stateint );
        Integer ret = new Integer( stateint );
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
        System.out.println("Hello from getfoo");

        throw new Exception("From getFoo()");
    }

    public String getThrow()
        throws Exception
    {
        System.out.println("Hello from geThrow");
       throw new Exception("From getThrow()");
    }
}
