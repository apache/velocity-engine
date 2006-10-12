package org.apache.velocity.app.event.implement;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.velocity.util.introspection.Info;

/**
 * Convenience class to use when reporting out invalid syntax 
 * with line, column, and template name.
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain </a>
 * @version $Id$
 */
public class InvalidReferenceInfo extends Info
{
    private String invalidReference;
    
    public InvalidReferenceInfo(String invalidReference, Info info)
    {
        super(info.getTemplateName(),info.getLine(),info.getColumn());
        this.invalidReference = invalidReference; 
    }

    /**
     * Get the specific invalid reference string.
     * @return the invalid reference string
     */
    public String getInvalidReference()
    {
        return invalidReference;
    }
    
    

    /**
     * Formats a textual representation of this object as <code>SOURCE
     * [line X, column Y]</code>.
     *
     * @return String representing this object.
     */
    public String toString()
    {
        return getTemplateName() + " [line " + getLine() + ", column " +
            getColumn() + "]: " + invalidReference;
    }
}
