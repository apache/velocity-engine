package org.apache.velocity.runtime.resource;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Default implementation of the resource cache for the default
 * ResourceManager.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: ResourceCacheImpl.java,v 1.2.8.1 2004/03/03 23:23:01 geirm Exp $
 */
public class ResourceCacheImpl implements ResourceCache
{
    /**
     * Cache storage, assumed to be thread-safe.
     */
    protected Map cache = new Hashtable();

    /**
     * Runtime services, generally initialized by the
     * <code>initialize()</code> method.
     */
    protected RuntimeServices rsvc = null;
    
    public void initialize( RuntimeServices rs )
    {
        rsvc = rs;
        
        rsvc.info("ResourceCache : initialized. (" + this.getClass() + ")");
    }
    
    public Resource get( Object key )
    {
        return (Resource) cache.get( key );
    }
    
    public Resource put( Object key, Resource value )
    {
        return (Resource) cache.put( key, value );
    }
    
    public Resource remove( Object key )
    {
        return (Resource) cache.remove( key );
    }
    
    public Iterator enumerateKeys()
    {
        return cache.keySet().iterator();
    }
}
