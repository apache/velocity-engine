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

// Java stuff.
import java.util.*;

// Turbine stuff.
//import org.apache.turbine.services.resources.TurbineResources;

/*
 * The idea of the RefreshableCachedObject is that, rather than
 * removing items from the cache when they become stale, we'll tell them to
 * refresh themselves instead.  That way they'll always be in the
 * cache, and the code to refresh them will be run by the background
 * thread rather than by a user request thread.  You can also set a TTL (Time
 * To Live) for the object.  This way, if the object hasn't been touched
 * for the TTL period, then it will be removed from the cache.
 *
 * This extends CachedObject and provides a method for refreshing the
 * cached object, and resetting its expire time.
 *
 * @author <a href="mailto:nissim@nksystems.com">Nissim Karpenstein</a>
 * @version $Id: RefreshableCachedObject.java,v 1.1 2000/11/16 01:46:58 jvanzyl Exp $
 */
public class RefreshableCachedObject extends CachedObject
    implements Refreshable
{

    /**
     * How long to wait before removing an untouched object from the cache.
     * Negative numbers mean never remove (the default).
     */
    private long timeToLive = -1;

    /**
     * The last time the Object was accessed from the cache.
     */
    private long lastAccess;
    
    /**
     * Constructor; sets the object to expire in the default time (30
     * minutes).
     *
     * @param o The object you want to cache.
     */
    public RefreshableCachedObject(Refreshable o)
    {
        super(o);
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Constructor.
     *
     * @param o The object to cache.
     * @param expires How long before the object expires, in ms,
     * e.g. 1000 = 1 second.
     */
    public RefreshableCachedObject(Refreshable o,
                                   long expires)
    {
        super(o, expires);
        lastAccess = System.currentTimeMillis();
    }

    /** sets the timeToLive member (in milliseconds) */
    public synchronized void setTTL(long l) { timeToLive = l; }

    /** gets the timeToLive member (in milliseconds) */
    public synchronized long getTTL() { return timeToLive; }

    /**
     * Sets the last acccess time to the current time.
     */
    public synchronized void touch()
    {
        lastAccess = System.currentTimeMillis();
    }

    /**
     * Returns true if the object hasn't been touched  
     * in the previous TTL period.
     */
    public synchronized boolean isUntouched()
    {
        if (timeToLive < 0)
            return false;

        if (lastAccess + timeToLive < System.currentTimeMillis())
            return true;
        else
            return false;
    }

    /**
     * Refresh the object and the created time.
     */
    public void refresh()
    {
        Refreshable r = (Refreshable)getContents();
        synchronized (this)
        {
            r.refresh();
            created = created + getExpires();
        }
    }
}
