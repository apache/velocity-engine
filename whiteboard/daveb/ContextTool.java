/*
 * Copyright 2000,2004 The Apache Software Foundation.
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

import java.io.Serializable;
import org.apache.velocity.context.Context;

/**
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: ContextTool.java,v 1.1.14.1 2004/03/04 00:18:30 geirm Exp $
 */
public abstract class ContextTool implements Serializable
{
    /** Owner */
    protected Context context;
    
    /** 
     * keep a pointer to my context
     * in case there's other stuff in there
     * that I might want to use in the Tool
     */
    public void setContext( Context c )
    {
        this.context = c;
    }
    
    /**
     * Provides the name for the context
     */
    public abstract String getName();
}


