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

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import com.werken.xpath.XPath;
import java.util.List;

/**
 * A JDOM {@link Element} that is tailored for Anakia needs. It has
 * {@link #selectNodes(String)} method as well as a {@link #toString()} that
 * outputs the XML serialized form of the element. This way it acts in much the
 * same way as a single-element {@link NodeList} would.
 *
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id: AnakiaElement.java,v 1.2 2001/08/13 02:52:10 geirm Exp $
 */
public class AnakiaElement extends Element
{
    private static final XMLOutputter DEFAULT_OUTPUTTER = new XMLOutputter();

    /**
     * <p>
     * This will create a new <code>AnakiaElement</code>
     *   with the supplied (local) name, and define
     *   the <code>{@link Namespace}</code> to be used.
     * If the provided namespace is null, the element will have 
     * no namespace.
     * </p>
     *
     * @param name <code>String</code> name of element.
     * @namespace <code>Namespace</code> to put element in.
     */
    public AnakiaElement(String name, Namespace namespace)
    {
        super(name, namespace);
    }

    /**
     * <p>
     *  This will create an <code>AnakiaElement</code> in no
     *    <code>{@link Namespace}</code>.
     * </p>
     *
     * @param name <code>String</code> name of element.
     */
    public AnakiaElement(String name)
    {
        super(name);
    }

    /**
     * <p>
     *  This will create a new <code>AnakiaElement</code> with
     *    the supplied (local) name, and specifies the URI
     *    of the <code>{@link Namespace}</code> the <code>Element</code>
     *    should be in, resulting it being unprefixed (in the default
     *    namespace).
     * </p>
     *
     * @param name <code>String</code> name of element.
     * @param uri <code>String</code> URI for <code>Namespace</code> element
     *        should be in.
     */
    public AnakiaElement(String name, String uri)
    {
        super(name, uri);
    }

    /**
     * <p>
     *  This will create a new <code>AnakiaElement</code> with
     *    the supplied (local) name, and specifies the prefix and URI
     *    of the <code>{@link Namespace}</code> the <code>Element</code>
     *    should be in.
     * </p>
     *
     * @param name <code>String</code> name of element.
     * @param uri <code>String</code> URI for <code>Namespace</code> element
     *        should be in.
     */
    public AnakiaElement(String name, String prefix, String uri)
    {
        super(name, prefix, uri);
    }
    
    /**
     * Applies an XPath expression to this element and returns the resulting
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
     * application of the XPath to the current element. It can be empty.
     */
    public NodeList selectNodes(String xpathExpression)
    {
        return new NodeList(XPathCache.getXPath(xpathExpression).applyTo(this), false);
    }

    /**
     * Returns the XML serialized form of this element, as produced by the default
     * {@link XMLOutputter}.
     */
    public String toString()
    {
        return DEFAULT_OUTPUTTER.outputString(this);
    }
    
    /**
     * <p>
     * This returns the full content of the element as a NodeList which
     * may contain objects of type <code>String</code>, <code>Element</code>,
     * <code>Comment</code>, <code>ProcessingInstruction</code>,
     * <code>CDATA</code>, and <code>EntityRef</code>.  
     * The List returned is "live" in document order and modifications 
     * to it affect the element's actual contents.  Whitespace content is 
     * returned in its entirety.
     * </p>
     *
     * @return a <code>List</code> containing the mixed content of the
     *         element: may contain <code>String</code>,
     *         <code>{@link Element}</code>, <code>{@link Comment}</code>,
     *         <code>{@link ProcessingInstruction}</code>,
     *         <code>{@link CDATA}</code>, and
     *         <code>{@link EntityRef}</code> objects.
     */
    public List getContent()
    {
        return new NodeList(super.getContent(), false);
    }
    
    /**
     * <p>
     * This returns a <code>NodeList</code> of all the child elements
     * nested directly (one level deep) within this element, as 
     * <code>Element</code> objects.  If this target element has no nested 
     * elements, an empty List is returned.  The returned list is "live"
     * in document order and changes to it affect the element's actual 
     * contents.
     * </p>
     * <p>
     * This performs no recursion, so elements nested two levels
     *   deep would have to be obtained with:
     * <pre>
     * <code>
     *   Iterator itr = currentElement.getChildren().iterator();
     *   while (itr.hasNext()) {
     *     Element oneLevelDeep = (Element)nestedElements.next();
     *     List twoLevelsDeep = oneLevelDeep.getChildren();
     *     // Do something with these children
     *   }
     * </code>
     * </pre>
     * </p>
     *
     * @return list of child <code>Element</code> objects for this element
     */
    public List getChildren()
    {
        return new NodeList(super.getChildren(), false);
    }

    /**
     * <p>
     * This returns a <code>NodeList</code> of all the child elements
     * nested directly (one level deep) within this element with the given
     * local name and belonging to no namespace, returned as 
     * <code>Element</code> objects.  If this target element has no nested 
     * elements with the given name outside a namespace, an empty List 
     * is returned.  The returned list is "live" in document order
     * and changes to it affect the element's actual contents.
     * </p>
     * <p>
     * Please see the notes for <code>{@link #getChildren}</code>
     * for a code example.
     * </p>
     *
     * @param name local name for the children to match
     * @return all matching child elements
     */
    public List getChildren(String name)
    {
        return new NodeList(super.getChildren(name));
    }

    /**
     * <p>
     * This returns a <code>NodeList</code> of all the child elements
     * nested directly (one level deep) within this element with the given
     * local name and belonging to the given Namespace, returned as 
     * <code>Element</code> objects.  If this target element has no nested 
     * elements with the given name in the given Namespace, an empty List 
     * is returned.  The returned list is "live" in document order
     * and changes to it affect the element's actual contents.
     * </p>
     * <p>
     * Please see the notes for <code>{@link #getChildren}</code>
     * for a code example.
     * </p>
     *
     * @param name local name for the children to match
     * @param ns <code>Namespace</code> to search within
     * @return all matching child elements
     */
    public List getChildren(String name, Namespace ns)
    {
        return new NodeList(super.getChildren(name, ns));
    }

    /**
     * <p>
     * This returns the complete set of attributes for this element, as a 
     * <code>NodeList</code> of <code>Attribute</code> objects in no particular 
     * order, or an empty list if there are none.  
     * The returned list is "live" and changes to it affect the 
     * element's actual attributes.
     * </p>
     *
     * @return attributes for the element
     */
    public List getAttributes()
    {
        return new NodeList(super.getAttributes());
    }
}
