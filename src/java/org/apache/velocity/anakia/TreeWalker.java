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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * This class allows you to walk a tree of JDOM Element objects.
 * It first walks the tree itself starting at the Element passed 
 * into allElements() and stores each node of the tree 
 * in a Vector which allElements() returns as a result of its
 * execution. You can then use a #foreach in Velocity to walk
 * over the Vector and visit each Element node. However, you can
 * achieve the same effect by calling <code>element.selectNodes("//*")</code>.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id: TreeWalker.java,v 1.5 2001/08/08 04:30:47 jon Exp $
 */
public class TreeWalker
{
    /**
     * Empty constructor
     */
    public TreeWalker()
    {
        // Left blank
    }
    
    /**
     * Creates a new Vector and walks the Element tree.
     *   
     * @param Element the starting Element node
     * @return Vector a vector of Element nodes
     */
    public NodeList allElements(Element e)
    {
        ArrayList theElements = new ArrayList();
        treeWalk (e, theElements);
        return new NodeList(theElements, false);
    }
    
    /**
     * A recursive method to walk the Element tree.
     * @param Element the current Element
     */
    private final void treeWalk(Element e, Collection theElements )
    {
        for (Iterator i=e.getChildren().iterator(); i.hasNext(); )
        {
            Element child = (Element)i.next();
            theElements.add(child);
            treeWalk(child, theElements);
        }            
    }
}    
