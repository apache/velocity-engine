package org.apache.velocity.anakia;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DefaultJDOMFactory;

/**
 * A customized JDOMFactory for Anakia that produces {@link AnakiaElement}
 * instances instead of ordinary JDOM {@link Element} instances.
 *
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id: AnakiaJDOMFactory.java,v 1.2.8.1 2004/03/03 23:22:04 geirm Exp $
 */
public class AnakiaJDOMFactory extends DefaultJDOMFactory
{
    public AnakiaJDOMFactory()
    {
    }

    public Element element(String name, Namespace namespace)
    {
        return new AnakiaElement(name, namespace);
    }

    public Element element(String name)
    {
        return new AnakiaElement(name);
    }

    public Element element(String name, String uri)
    {
        return new AnakiaElement(name, uri);
    }

    public Element element(String name, String prefix, String uri)
    {
        return new AnakiaElement(name, prefix, uri);
    }
}
