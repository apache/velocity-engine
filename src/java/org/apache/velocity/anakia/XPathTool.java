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

import java.util.List;

import org.apache.velocity.runtime.RuntimeSingleton;

import com.werken.xpath.XPath;

import org.jdom.Document;
import org.jdom.Element;

/**
 * This class adds an entrypoint into XPath functionality,
 * for Anakia.
 * <p>
 * All methods take a string XPath specification, along with
 * a context, and produces a resulting java.util.List.
 * <p>
 * The W3C XPath Specification (http://www.w3.org/TR/xpath) refers
 * to NodeSets repeatedly, but this implementation simply uses
 * java.util.List to hold all Nodes.  A 'Node' is any object in
 * a JDOM object tree, such as an org.jdom.Element, org.jdom.Document,
 * or org.jdom.Attribute.
 * <p>
 * To use it in Velocity, do this:
 * <p>
 * <pre>
 * #set $authors = $xpath.applyTo("document/author", $root)
 * #foreach ($author in $authors)
 *   $author.getValue() 
 * #end
 * #set $chapterTitles = $xpath.applyTo("document/chapter/@title", $root)
 * #foreach ($title in $chapterTitles)
 *   $title.getValue()
 * #end
 * </pre>
 * <p>
 * In newer Anakia builds, this class is obsoleted in favor of calling
 * <code>selectNodes()</code> on the element directly:
 * <pre>
 * #set $authors = $root.selectNodes("document/author")
 * #foreach ($author in $authors)
 *   $author.getValue() 
 * #end
 * #set $chapterTitles = $root.selectNodes("document/chapter/@title")
 * #foreach ($title in $chapterTitles)
 *   $title.getValue()
 * #end
 * </pre>
 * <p>
 *  
 * @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id: XPathTool.java,v 1.12 2001/08/08 04:30:47 jon Exp $
 */
public class XPathTool
{
    /**
     * Constructor does nothing, as this is mostly
     * just objectified static methods
     */
    public XPathTool()
    {
        //        RuntimeSingleton.info("XPathTool::XPathTool()");
        // intentionally left blank
    }

    /**
     * Apply an XPath to a JDOM Document
     *
     * @param xpathSpec The XPath to apply
     * @param doc The Document context
     *
     * @return A list of selected nodes
     */
    public NodeList applyTo(String xpathSpec,
                        Document doc)
    {
        //RuntimeSingleton.info("XPathTool::applyTo(String, Document)");
        return new NodeList(XPathCache.getXPath(xpathSpec).applyTo( doc ), false);
    }

    /**
     * Apply an XPath to a JDOM Element
     *
     * @param xpathSpec The XPath to apply
     * @param doc The Element context
     *
     * @return A list of selected nodes
     */
    public NodeList applyTo(String xpathSpec,
                        Element elem)
    {
        //RuntimeSingleton.info("XPathTool::applyTo(String, Element)");
        return new NodeList(XPathCache.getXPath(xpathSpec).applyTo( elem ), false);
    }

    /**
     * Apply an XPath to a nodeset
     *
     * @param xpathSpec The XPath to apply
     * @param doc The nodeset context
     *
     * @return A list of selected nodes
     */
    public NodeList applyTo(String xpathSpec,
                        List nodeSet)
    {
        //RuntimeSingleton.info("XPathTool::applyTo(String, List)");
        return new NodeList(XPathCache.getXPath(xpathSpec).applyTo( nodeSet ), false);
    }
}



