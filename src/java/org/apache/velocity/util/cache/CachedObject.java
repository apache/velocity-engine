package org.apache.velocity.util.cache;

/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
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
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine", "Turbine",
 *    "Apache Turbine", "Turbine Project", "Apache Turbine Project" and
 *    "Java Apache Project" must not be used to endorse or promote products
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

// Java Core Classes
//import java.util.*;

/*
 * Wrapper for an object you want to store in a cache for a period of
 * time.
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @version $Id: CachedObject.java,v 1.1 2000/11/16 01:46:58 jvanzyl Exp $
 */
public class CachedObject implements java.io.Serializable
{
    /** The object to be cached. */
    private Object contents = null;

    /** Default age (30 minutes). */
    //private long defaultage = TurbineResources.getLong("cachedobject.defaultage", 1800000);
    private long defaultage = 1800000L;

    /** When created. **/
    protected long created = 0;

    /** When it expires. **/
    private long expires = 0;

    /** Is this object stale/expired? */
    private boolean stale = false;


    /**
     * Constructor; sets the object to expire in the default time (30
     * minutes).
     *
     * @param o The object you want to cache.
     */
    public CachedObject(Object o)
    {
        this.contents = o;
        this.expires = defaultage;
        this.created = System.currentTimeMillis();
    }

    /**
     * Constructor.
     *
     * @param o The object to cache.
     * @param expires How long before the object expires, in ms,
     * e.g. 1000 = 1 second.
     */
    public CachedObject(Object o,
                        long expires)
    {
        if ( expires == 0 )
        {
            this.expires = defaultage;
        }

        this.contents = o;
        this.expires = expires;
        this.created = System.currentTimeMillis();
    }

    /**
     * Returns the cached object.
     *
     * @return The cached object.
     */
    public Object getContents()
    {
        return contents;
    }

    /**
     * Returns the creation time for the object.
     *
     * @return When the object was created.
     */
    public long getCreated()
    {
        return created;
    }

    /**
     * Returns the expiration time for the object.
     *
     * @return When the object expires.
     */
    public long getExpires()
    {
        return expires;
    }

    /**
     * Set the stale status for the object.
     *
     * @param stale Whether the object is stale or not.
     */
    public synchronized void setStale ( boolean stale )
    {
        this.stale = stale;
    }

    /**
     * Get the stale status for the object.
     *
     * @return Whether the object is stale or not.
     */
    public synchronized boolean getStale()
    {
        return stale;
    }

    /**
     * Is the object stale?
     *
     * @return True if the object is stale.
     */
    public synchronized boolean isStale()
    {
        setStale( (System.currentTimeMillis() - created) > expires );
        return getStale();
    }
}
