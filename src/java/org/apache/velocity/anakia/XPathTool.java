
package org.apache.velocity.anakia;

import com.werken.xpath.XPath;

import java.util.List;

public class XPathTool
{
    public XPathTool()
    {

    }

    List applyTo(String xpathSpec,
                 Document doc)
    {
        XPath xpath = new XPath( xpathSpec );

        return xpath.applyTo( doc );
    }

    List applyTo(String xpathSpec,
                 Element elem)
    {
        XPath xpath = new XPath(xpathSpec);

        return xpath.applyTo( elem );
    }

    List applyTo(String xpathSpec,
                 List nodeSet)
    {
        XPath xpath = new XPath(xpathSpec);

        return xpath.applyTo( nodeSet );
    }
        
}
