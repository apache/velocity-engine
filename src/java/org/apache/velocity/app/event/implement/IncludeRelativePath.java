package org.apache.velocity.app.event.implement;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.velocity.app.event.RuntimeServicesAware;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Event handler that looks for included files relative to the path of the
 * current template. The handler assumes that paths are separated by a forward
 * slash "/" or backwards slash "\".
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain </a>
 * @version $Id: EventCartridge.java,v 1.5 2004/03/19 17:13:33 dlr Exp $
 */

public class IncludeRelativePath implements IncludeEventHandler,RuntimeServicesAware {

    private RuntimeServices rs;
    String notfound;

    /**
     * Return path relative to the current template's path.
     */
    public String includeEvent(
        String includeResourcePath,
        String currentResourcePath,
        String directiveName)
    {

        // strip the starting slash from includeResourcePath, if it exists
        if (includeResourcePath.startsWith("/") || includeResourcePath.startsWith("\\") )
            includeResourcePath = includeResourcePath.substring(1);

        int slashpos1 = currentResourcePath.lastIndexOf("/");
        int slashpos2 = currentResourcePath.lastIndexOf("\\");
        int lastslashpos = -1;
        if ( (slashpos1 != -1) && (slashpos2 != -1) && (slashpos1 <= slashpos2) )
            lastslashpos = slashpos2;

        else if ( (slashpos1 != -1) && (slashpos2 != -1) && (slashpos1 > slashpos2) )
            lastslashpos = slashpos1;

        else if ( (slashpos1 != -1) && (slashpos2 == -1) )
            lastslashpos = slashpos1;

        else if ( (slashpos1 == -1) && (slashpos2 != -1) )
            lastslashpos = slashpos2;

        // root of resource tree
        if ( (lastslashpos == -1) || (lastslashpos == 0) )
            return includeResourcePath;

        // prepend path to the include path
        else
            return currentResourcePath.substring(0,lastslashpos) + "/" + includeResourcePath;

    }


    public void setRuntimeServices(RuntimeServices rs) throws Exception {
         this.rs = rs;
     }

}
