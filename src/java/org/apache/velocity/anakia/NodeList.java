package org.apache.velocity.anakia;

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

import com.werken.xpath.XPath;
import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 * Provides a class for wrapping a list of JDOM objects primarily for use in template
 * engines and other kinds of text transformation tools.
 * It has a {@link #toString()} method that will output the XML serialized form of the
 * nodes it contains - again focusing on template engine usage, as well as the
 * {@link #selectNodes(String)} method that helps selecting a different set of nodes
 * starting from the nodes in this list. The class also implements the {@link java.util.List}
 * interface by simply delegating calls to the contained list (the {@link #subList(int, int)}
 * method is implemented by delegating to the contained list and wrapping the returned
 * sublist into a <code>NodeList</code>).
 *
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id: NodeList.java,v 1.1 2001/08/08 04:30:47 jon Exp $
 */
public class NodeList implements List, Cloneable
{
    private static final AttributeXMLOutputter DEFAULT_OUTPUTTER = 
        new AttributeXMLOutputter();
    
    /** The contained nodes */
    private List nodes;

    /**
     * Creates an empty node list.
     */
    public NodeList()
    {
        nodes = new ArrayList();
    }

    /**
     * Creates a node list that holds a single {@link Document} node.
     */
    public NodeList(Document document)
    {
        this((Object)document);
    }

    /**
     * Creates a node list that holds a single {@link Element} node.
     */
    public NodeList(Element element)
    {
        this((Object)element);
    }

    private NodeList(Object object)
    {
        if(object == null)
        {
            throw new IllegalArgumentException(
                "Cannot construct NodeList with null.");
        }
        nodes = new ArrayList(1);
        nodes.add(object);
    }
    
    /**
     * Creates a node list that holds a list of nodes. 
     * @param nodes the list of nodes this template should hold. The created 
     * template will copy the passed nodes list, so changes to the passed list
     * will not affect the model.
     */
    public NodeList(List nodes)
    {
        this(nodes, true);
    }
    
    /**
     * Creates a node list that holds a list of nodes. 
     * @param nodes the list of nodes this template should hold.
     * @param copy if true, the created template will copy the passed nodes
     * list, so changes to the passed list will not affect the model. If false,
     * the model will reference the passed list and will sense changes in it,
     * altough no operations on the list will be synchronized.
     */
    public NodeList(List nodes, boolean copy)
    {
        if(nodes == null)
        {
            throw new IllegalArgumentException(
                "Cannot initialize NodeList with null list");
        }
        this.nodes = copy ? new ArrayList(nodes) : nodes;
    }
    
    /**
     * Retrieves the underlying list used to store the nodes. Note however, that
     * you can fully use the underlying list through the <code>List</code> interface
     * of this class itself. You would probably access the underlying list only for
     * synchronization purposes.
     */
    public List getList()
    {
        return nodes;
    }

    /**
     * This method returns the string resulting from concatenation of string 
     * representations of its nodes. Each node is rendered using its XML
     * serialization format. This greatly simplifies creating XML-transformation
     * templates, as to output a node contained in variable x as XML fragment,
     * you simply write ${x} in the template (or whatever your template engine
     * uses as its expression syntax).
     */
    public String toString()
    {
        if(nodes.isEmpty())
        {
            return "";
        }

        StringWriter sw = new StringWriter(nodes.size() * 128);
        try
        {
            for(Iterator i = nodes.iterator(); i.hasNext();)
            {
                Object node = i.next();
                if(node instanceof Element)
                {
                    DEFAULT_OUTPUTTER.output((Element)node, sw);
                }
                else if(node instanceof Attribute)
                {
                    DEFAULT_OUTPUTTER.output((Attribute)node, sw);
                }
                else if(node instanceof String)
                {
                    DEFAULT_OUTPUTTER.output(node.toString(), sw);
                }
                else if(node instanceof Text)
                {
                    DEFAULT_OUTPUTTER.output((Text)node, sw);
                }
                else if(node instanceof Document)
                {
                    DEFAULT_OUTPUTTER.output((Document)node, sw);
                }
                else if(node instanceof ProcessingInstruction)
                {
                    DEFAULT_OUTPUTTER.output((ProcessingInstruction)node, sw);
                }
                else if(node instanceof Comment)
                {
                    DEFAULT_OUTPUTTER.output((Comment)node, sw);
                }
                else if(node instanceof CDATA)
                {
                    DEFAULT_OUTPUTTER.output((CDATA)node, sw);
                }
                else if(node instanceof DocType)
                {
                    DEFAULT_OUTPUTTER.output((DocType)node, sw);
                }
                else if(node instanceof EntityRef)
                {
                    DEFAULT_OUTPUTTER.output((EntityRef)node, sw);
                }
                else
                {
                    throw new IllegalArgumentException(
                        "Cannot process a " + 
                        (node == null 
                         ? "null node" 
                         : "node of class " + node.getClass().getName()));
                }
            }
        }
        catch(IOException e)
        {
            // Cannot happen as we work with a StringWriter in memory
            throw new Error();
        }
        return sw.toString();
    }

    /**
     * Returns a NodeList that contains the same nodes as this node list.
     * @throws CloneNotSupportedException if the contained list's class does
     * not have an accessible no-arg constructor.
     */
    public Object clone()
        throws CloneNotSupportedException
    {
        NodeList clonedList = (NodeList)super.clone();
        clonedList.cloneNodes();
        return clonedList;
    }
    
    private void cloneNodes()
        throws CloneNotSupportedException
    {
        Class listClass = nodes.getClass();
        try
        {
            List clonedNodes = (List)listClass.newInstance();
            clonedNodes.addAll(nodes);
            nodes = clonedNodes;
        }
        catch(IllegalAccessException e)
        {
            throw new CloneNotSupportedException("Cannot clone NodeList since"
            + " there is no accessible no-arg constructor on class "
            + listClass.getName());
        }
        catch(InstantiationException e)
        {
            // Cannot happen as listClass represents a concrete, non-primitive,
            // non-array, non-void class - there's an instance of it in "nodes"
            // which proves these assumptions.
            throw new Error(); 
        }
    }

    /**
     * Returns the hash code of the contained list.
     */
    public int hashCode()
    {
        return nodes.hashCode();
    }
    
    /**
     * Tests for equality with another object.
     * @param o the object to test for equality
     * @return true if the other object is also a NodeList and their contained
     * {@link List} objects evaluate as equals.
     */
    public boolean equals(Object o)
    {
        return o instanceof NodeList 
            ? ((NodeList)o).nodes.equals(nodes)
            : false;
    }
    
    /**
     * Applies an XPath expression to the node list and returns the resulting
     * node list. In order for this method to work, your application must have
     * access to <a href="http://code.werken.com">werken.xpath</a> library
     * classes. The implementation does cache the parsed format of XPath
     * expressions in a weak hash map, keyed by the string representation of
     * the XPath expression. As the string object passed as the argument is
     * usually kept in the parsed template, this ensures that each XPath
     * expression is parsed only once during the lifetime of the template that
     * first invoked it.
     * @param xpathExpression the XPath expression you wish to apply
     * @return a NodeList representing the nodes that are the result of
     * application of the XPath to the current node list. It can be empty.
     */
    public NodeList selectNodes(String xpathString)
    {
        return new NodeList(XPathCache.getXPath(xpathString).applyTo(nodes), false);
    }

// List methods implemented hereafter

    public boolean add(Object o)
    {
        return nodes.add(o);
    }

    public void add(int index, Object o)
    {
        nodes.add(index, o);
    }

    public boolean addAll(Collection c)
    {
        return nodes.addAll(c);
    }

    public boolean addAll(int index, Collection c)
    {
        return nodes.addAll(index, c);
    }

    public void clear()
    {
        nodes.clear();
    }

    public boolean contains(Object o)
    {
        return nodes.contains(o);
    }

    public boolean containsAll(Collection c)
    {
        return nodes.containsAll(c);
    }

    public Object get(int index)
    {
        return nodes.get(index);
    }

    public int indexOf(Object o)
    {
        return nodes.indexOf(o);
    }

    public boolean isEmpty()
    {
        return nodes.isEmpty();
    }

    public Iterator iterator()
    {
        return nodes.iterator();
    }

    public int lastIndexOf(Object o)
    {
        return nodes.lastIndexOf(o);
    }

    public ListIterator listIterator()
    {
        return nodes.listIterator();
    }

    public ListIterator listIterator(int index)
    {
        return nodes.listIterator(index);
    }

    public Object remove(int index)
    {
        return nodes.remove(index);
    }

    public boolean remove(Object o)
    {
        return nodes.remove(o);
    }

    public boolean removeAll(Collection c)
    {
        return nodes.removeAll(c);
    }

    public boolean retainAll(Collection c)
    {
        return nodes.retainAll(c);
    }

    public Object set(int index, Object o)
    {
        return nodes.set(index, o);
    }

    public int size()
    {
        return nodes.size();
    }

    public List subList(int fromIndex, int toIndex)
    {
        return new NodeList(nodes.subList(fromIndex, toIndex));
    }

    public Object[] toArray()
    {
        return nodes.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return nodes.toArray(a);
    }

    /**
     * A special subclass of XMLOutputter that will be used to output 
     * Attribute nodes. As a subclass of XMLOutputter it can use its protected
     * method escapeAttributeEntities() to serialize the attribute
     * appropriately.
     */
    private static final class AttributeXMLOutputter extends XMLOutputter
    {
        public void output(Attribute attribute, Writer out)
            throws IOException
        {
            out.write(" ");
            out.write(attribute.getQualifiedName());
            out.write("=");
            
            out.write("\"");
            out.write(escapeAttributeEntities(attribute.getValue()));
            out.write("\"");            
        }
    }
}
