package org.apache.velocity.util.introspection;

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

/**
 *  Holds information for node-local context data introspection
 *  information.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: IntrospectionCacheData.java,v 1.3.14.1 2004/03/03 23:23:08 geirm Exp $
 */
public class IntrospectionCacheData
{
    /**  
     *  Object to pair with class - currently either a Method or 
     *  AbstractExecutor. It can be used in any way the using node
     *  wishes. 
     */
    public Object thingy;
    
    /*
     *  Class of context data object associated with the introspection
     *  information
     */
    public Class  contextData;
}
