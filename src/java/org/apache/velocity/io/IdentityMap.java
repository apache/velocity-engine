package org.apache.velocity.io;

/* ====================================================================
 * Trove - Copyright (c) 1997-2000 Walt Disney Internet Group
 * ====================================================================
 * The Tea Software License, Version 1.1
 *
 * Copyright (c) 2000 Walt Disney Internet Group. All rights reserved.
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Walt Disney Internet Group (http://opensource.go.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "BeanDoc" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact opensource@dig.com.
 *
 * 5. Products derived from this software may not be called "Tea",
 *    "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
 *    "Kettle", "Trove" or "BeanDoc" appear in their name, without prior
 *    written permission of the Walt Disney Internet Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE WALT DISNEY INTERNET GROUP OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information about Tea, please see http://opensource.go.com/.
 */

import java.lang.ref.*;
import java.util.*;

/******************************************************************************
 * An IdentityMap is like WeakHashMap, except it uses a key's identity
 * hashcode and equals methods. IdentityMap is not thread-safe and must be
 * wrapped with Collections.synchronizedMap to be made thread-safe. Most of the
 * implementation for this class is ripped off from java.util.HashMap, but not
 * java.util.WeakHashMap, in order to acheive greater efficiency.
 * <p>
 * The documentation for WeakHashMap states that it is intended primarily
 * for use with key objects whose equals methods test for object identity
 * using the == operator. Because WeakHashMap uses a key's own equals and
 * hashcode methods, it is better suited for implementing methods that behave
 * like {@link String#intern}. However, because WeakHashMap stongly references
 * values, {@link Utils#intern Utils.intern} provides a safer intern mechanism.
 * <p>
 * In this implementation, all key objects are tested for equality using the
 * == operator, and null keys are not permitted. IdentityMap is therefore
 * better suited for "canonicalized" mappings.
 * <p>
 * Note: Weakly referenced entries may be automatically removed during
 * either accessor or mutator operations, possibly causing a concurrent
 * modification to be detected. Therefore, even if multiple threads are only
 * accessing this map, be sure to synchronize this map first. Also, do not
 * rely on the value returned by size() when using an iterator from this map.
 * The iterators may return less entries than the amount reported by size().
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 * @see java.util.WeakHashMap
 * @see java.util.HashMap
 */
public class IdentityMap extends AbstractMap implements Map, Cloneable {
    // Types of Iterators
    static final int KEYS = 0;
    static final int VALUES = 1;
    static final int ENTRIES = 2;

    static final Iterator cEmptyHashIterator = new Iterator() {
        public boolean hasNext() {
            return false;
        }
        
        public Object next() {
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new IllegalStateException();
        }
    };

    /**
     * Test program.
     */
    /*
    public static void main(String[] args) throws Exception {
        Map map = new IdentityMap();
        map.put("Hello", "There");
        for (int i=0; i<1000000; i++) {
            if (i % 5 == 0) {
                map.put(new String("Hello"), "Dude");
            }
            map.get("Hello");
            map.get("Stuff");
        }

        System.out.println(map.containsValue("Dude"));
        System.out.println(map.get("Hello"));

        System.gc();

        System.out.println(map);
        System.out.println(map.size());

        System.out.println(map.containsValue("Dude"));
        System.out.println(map.get("Hello"));

        map.remove("Hello");

        System.out.println(map);
        System.out.println(map.size());

        System.out.println(map.containsValue("Dude"));
        System.out.println(map.get("Hello"));
    }
    */

    /**
     * Converts a string to a collection without calling size(). Iterators from
     * this map may return less entries than the amount reported by size().
     */
    static String toString(Collection c) {
        StringBuffer buf = new StringBuffer();
        Iterator it = c.iterator();
        buf.append("[");
        for (int i = 0; it.hasNext(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(String.valueOf(it.next()));
        }
        buf.append("]");
        return buf.toString();
    }

    /**
     * The hash table data.
     */
    private transient Entry mTable[];

    /**
     * The total number of mappings in the hash table.
     */
    private transient int mCount;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     *
     * @serial
     */
    private int mThreshold;

    /**
     * The load factor for the hashtable.
     *
     * @serial
     */
    private float mLoadFactor;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    private transient int mModCount = 0;

    // Views
    
    private transient Set mKeySet = null;
    private transient Set mEntrySet = null;
    private transient Collection mValues = null;

    /**
     * Constructs a new, empty map with the specified initial 
     * capacity and the specified load factor. 
     *
     * @param      initialCapacity   the initial capacity of the HashMap.
     * @param      loadFactor        the load factor of the HashMap
     * @throws     IllegalArgumentException  if the initial capacity is less
     *               than zero, or if the load factor is nonpositive.
     */
    public IdentityMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Initial Capacity: "+
                                               initialCapacity);
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load factor: "+
                                               loadFactor);
        }

        if (initialCapacity == 0) {
            initialCapacity = 1;
        }

        mLoadFactor = loadFactor;
        mTable = new Entry[initialCapacity];
        mThreshold = (int)(initialCapacity * loadFactor);
    }
    
    /**
     * Constructs a new, empty map with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param   initialCapacity   the initial capacity of the HashMap.
     * @throws    IllegalArgumentException if the initial capacity is less
     *              than zero.
     */
    public IdentityMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty map with a default capacity and load
     * factor, which is <tt>0.75</tt>.
     */
    public IdentityMap() {
        this(11, 0.75f);
    }

    /**
     * Constructs a new map with the same mappings as the given map.  The
     * map is created with a capacity of twice the number of mappings in
     * the given map or 11 (whichever is greater), and a default load factor,
     * which is <tt>0.75</tt>.
     */
    public IdentityMap(Map t) {
        this(Math.max(2 * t.size(), 11), 0.75f);
        putAll(t);
    }

    /**
     * Returns the number of key-value mappings in this map, but this value
     * may be larger than actual amount of entries produced by an iterator.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return mCount;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return mCount == 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
        Entry tab[] = mTable;
        
        if (value == null) {
            for (int i = tab.length ; i-- > 0 ;) {
                for (Entry e = tab[i], prev = null; e != null; e = e.mNext) {
                    if (e.getKey() == null) {
                        // Clean up after a cleared Reference.
                        mModCount++;
                        if (prev != null) {
                            prev.mNext = e.mNext;
                        }
                        else {
                            tab[i] = e.mNext;
                        }
                        mCount--;
                    }
                    else if (e.mValue == null) {
                        return true;
                    }
                    else {
                        prev = e;
                    }
                }
            }
        }
        else {
            for (int i = tab.length ; i-- > 0 ;) {
                for (Entry e = tab[i], prev = null; e != null; e = e.mNext) {
                    if (e.getKey() == null) {
                        // Clean up after a cleared Reference.
                        mModCount++;
                        if (prev != null) {
                            prev.mNext = e.mNext;
                        }
                        else {
                            tab[i] = e.mNext;
                        }
                        mCount--;
                    }
                    else if (value.equals(e.mValue)) {
                        return true;
                    }
                    else {
                        prev = e;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * @param key key whose presence in this Map is to be tested.
     */
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }

        Entry tab[] = mTable;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
            Object entryKey = e.getKey();

            if (entryKey == null) {
                // Clean up after a cleared Reference.
                mModCount++;
                if (prev != null) {
                    prev.mNext = e.mNext;
                }
                else {
                    tab[index] = e.mNext;
                }
                mCount--;
            }
            else if (e.mHash == hash && key == entryKey) {
                return true;
            }
            else {
                prev = e;
            }
        }

        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    public Object get(Object key) {
        if (key == null) {
            return null;
        }

        Entry tab[] = mTable;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
            Object entryKey = e.getKey();

            if (entryKey == null) {
                // Clean up after a cleared Reference.
                mModCount++;
                if (prev != null) {
                    prev.mNext = e.mNext;
                }
                else {
                    tab[index] = e.mNext;
                }
                mCount--;
            }
            else if (e.mHash == hash && key == entryKey) {
                return e.mValue;
            }
            else {
                prev = e;
            }
        }

        return null;
    }

    /**
     * Scans the contents of this map, removing all entries that have a
     * cleared weak key.
     */
    private void cleanup() {
        Entry tab[] = mTable;
        
        for (int i = tab.length ; i-- > 0 ;) {
            for (Entry e = tab[i], prev = null; e != null; e = e.mNext) {
                if (e.getKey() == null) {
                    // Clean up after a cleared Reference.
                    mModCount++;
                    if (prev != null) {
                        prev.mNext = e.mNext;
                    }
                    else {
                        tab[i] = e.mNext;
                    }
                    mCount--;
                }
                else {
                    prev = e;
                }
            }
        }
    }

    /**
     * Rehashes the contents of this map into a new <tt>HashMap</tt> instance
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     */
    private void rehash() {
        int oldCapacity = mTable.length;
        Entry oldMap[] = mTable;
        
        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];
        
        mModCount++;
        mThreshold = (int)(newCapacity * mLoadFactor);
        mTable = newMap;
        
        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry old = oldMap[i] ; old != null ; ) {
                Entry e = old;
                old = old.mNext;

                // Only copy entry if its key hasn't been cleared.
                if (e.getKey() == null) {
                    mCount--;
                }
                else {
                    int index = (e.mHash & 0x7FFFFFFF) % newCapacity;
                    e.mNext = newMap[index];
                    newMap[index] = e;
                }
            }
        }
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the HashMap previously associated
     *         <tt>null</tt> with the specified key.
     */
    public Object put(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("Null key is not permitted");
        }

        // Makes sure the key is not already in the HashMap.
        Entry tab[] = mTable;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
            Object entryKey = e.getKey();

            if (entryKey == null) {
                // Clean up after a cleared Reference.
                mModCount++;
                if (prev != null) {
                    prev.mNext = e.mNext;
                }
                else {
                    tab[index] = e.mNext;
                }
                mCount--;
            }
            else if (e.mHash == hash && key == entryKey) {
                Object old = e.mValue;
                e.mValue = value;
                return old;
            }
            else {
                prev = e;
            }
        }

        mModCount++;

        if (mCount >= mThreshold) {
            // Cleanup the table if the threshold is exceeded.
            cleanup();
        }

        if (mCount >= mThreshold) {
            // Rehash the table if the threshold is still exceeded.
            rehash();
            tab = mTable;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }
        
        // Creates the new entry.
        Entry e = new Entry(hash, (Object)key, value, tab[index]);
        tab[index] = e;
        mCount++;
        return null;
    }
    
    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    public Object remove(Object key) {
        Entry tab[] = mTable;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
            
        for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
            Object entryKey = e.getKey();

            if (entryKey == null) {
                // Clean up after a cleared Reference.
                mModCount++;
                if (prev != null) {
                    prev.mNext = e.mNext;
                }
                else {
                    tab[index] = e.mNext;
                }
                mCount--;
            }
            else if (e.mHash == hash && key == entryKey) {
                mModCount++;
                if (prev != null) {
                    prev.mNext = e.mNext;
                }
                else {
                    tab[index] = e.mNext;
                }
                mCount--;

                Object oldValue = e.mValue;
                e.mValue = null;
                return oldValue;
            }
            else {
                prev = e;
            }
        }

        return null;
    }
    
    /**
     * Copies all of the mappings from the specified map to this one.
     * 
     * These mappings replace any mappings that this map had for any of the
     * keys currently in the specified Map.
     *
     * @param t Mappings to be stored in this map.
     */
    public void putAll(Map t) {
        Iterator i = t.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        Entry tab[] = mTable;
        mModCount++;
        for (int index = tab.length; --index >= 0; ) {
            tab[index] = null;
        }
        mCount = 0;
    }

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map.
     */
    public Object clone() {
        try { 
            IdentityMap t = (IdentityMap)super.clone();
            t.mTable = new Entry[mTable.length];
            for (int i = mTable.length ; i-- > 0 ; ) {
                t.mTable[i] = (mTable[i] != null) 
                    ? (Entry)mTable[i].clone() : null;
            }
            t.mKeySet = null;
            t.mEntrySet = null;
            t.mValues = null;
            t.mModCount = 0;
            return t;
        }
        catch (CloneNotSupportedException e) { 
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
    
    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set keySet() {
        if (mKeySet == null) {
            mKeySet = new AbstractSet() {
                public Iterator iterator() {
                    return getHashIterator(KEYS);
                }
                public int size() {
                    return mCount;
                }
                public boolean contains(Object o) {
                    return containsKey(o);
                }
                public boolean remove(Object o) {
                    return o == null ? false : IdentityMap.this.remove(o) == o;
                }
                public void clear() {
                    IdentityMap.this.clear();
                }
                public String toString() {
                    return IdentityMap.this.toString(this);
                }
            };
        }
        return mKeySet;
    }
    
    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection values() {
        if (mValues==null) {
            mValues = new AbstractCollection() {
                public Iterator iterator() {
                    return getHashIterator(VALUES);
                }
                public int size() {
                    return mCount;
                }
                public boolean contains(Object o) {
                    return containsValue(o);
                }
                public void clear() {
                    IdentityMap.this.clear();
                }
                public String toString() {
                    return IdentityMap.this.toString(this);
                }
            };
        }
        return mValues;
    }

    /**
     * Returns a collection view of the mappings contained in this map.  Each
     * element in the returned collection is a <tt>Map.Entry</tt>.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see Map.Entry
     */
    public Set entrySet() {
        if (mEntrySet==null) {
            mEntrySet = new AbstractSet() {
                public Iterator iterator() {
                    return getHashIterator(ENTRIES);
                }
                
                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry entry = (Map.Entry)o;
                    Object key = entry.getKey();

                    Entry tab[] = mTable;
                    int hash = System.identityHashCode(key);
                    int index = (hash & 0x7FFFFFFF) % tab.length;

                    for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
                        Object entryKey = e.getKey();
                        
                        if (entryKey == null) {
                            // Clean up after a cleared Reference.
                            mModCount++;
                            if (prev != null) {
                                prev.mNext = e.mNext;
                            }
                            else {
                                tab[index] = e.mNext;
                            }
                            mCount--;
                        }
                        else if (e.mHash == hash && e.identityEquals(entry)) {
                            return true;
                        }
                        else {
                            prev = e;
                        }
                    }

                    return false;
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry entry = (Map.Entry)o;
                    Object key = entry.getKey();
                    Entry tab[] = mTable;
                    int hash = System.identityHashCode(key);
                    int index = (hash & 0x7FFFFFFF) % tab.length;

                    for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
                        Object entryKey = e.getKey();
                        
                        if (entryKey == null) {
                            // Clean up after a cleared Reference.
                            mModCount++;
                            if (prev != null) {
                                prev.mNext = e.mNext;
                            }
                            else {
                                tab[index] = e.mNext;
                            }
                            mCount--;
                        }
                        else if (e.mHash == hash && e.identityEquals(entry)) {
                            mModCount++;
                            if (prev != null) {
                                prev.mNext = e.mNext;
                            }
                            else {
                                tab[index] = e.mNext;
                            }
                            mCount--;

                            e.mValue = null;
                            return true;
                        }
                        else {
                            prev = e;
                        }
                    }
                    return false;
                }

                public int size() {
                    return mCount;
                }
                
                public void clear() {
                    IdentityMap.this.clear();
                }

                public String toString() {
                    return IdentityMap.this.toString(this);
                }
            };
        }
        
        return mEntrySet;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator it = entrySet().iterator();
        
        buf.append("{");
        for (int i = 0; it.hasNext(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            Map.Entry e = (Map.Entry)it.next();
            buf.append(e.getKey() + "=" + e.getValue());
        }
        buf.append("}");
        return buf.toString();
    }

    private Iterator getHashIterator(int type) {
        if (mCount == 0) {
            return cEmptyHashIterator;
        }
        else {
            return new HashIterator(type);
        }
    }

    /**
     * HashMap collision list entry.
     */
    private static class Entry implements Map.Entry {
        int mHash;
        Object mValue;
        Entry mNext;
        
        private Reference mKey;

        Entry(int hash, Object key, Object value, Entry next) {
            mHash = hash;
            mKey = new WeakReference(key);
            mValue = value;
            mNext = next;
        }
        
        private Entry(int hash, Reference key, Object value, Entry next) {
            mHash = hash;
            mKey = key;
            mValue = value;
            mNext = next;
        }
        
        protected Object clone() {
            return new Entry(mHash, (Reference)mKey, mValue,
                             (mNext==null ? null : (Entry)mNext.clone()));
        }
        
        // Map.Entry Ops 
        
        public Object getKey() {
            return mKey.get();
        }
        
        public Object getValue() {
            return mValue;
        }
        
        public Object setValue(Object value) {
            Object oldValue = mValue;
            mValue = value;
            return oldValue;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;

            Object key = getKey();
            
            return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
                (mValue==null ? e.getValue()==null : mValue.equals(e.getValue()));
        }

        public boolean identityEquals(Map.Entry e) {
            return (getKey() == e.getKey()) &&
                (mValue==null ? e.getValue()==null : mValue.equals(e.getValue()));
        }
        
        public int hashCode() {
            return mHash ^ (mValue==null ? 0 : mValue.hashCode());
        }
        
        public String toString() {
            return getKey() + "=" + mValue;
        }
    }

    private class HashIterator implements Iterator {
        private Entry[] mTable = IdentityMap.this.mTable;
        private int mIndex = mTable.length;
        private Entry mEntry;
        // To ensure that the iterator doesn't return cleared entries, keep a
        // hard reference to the key. Its existence will prevent the weak
        // key from being cleared.
        private Object mEntryKey;
        private Entry mLastReturned;
        private int mType;
        
        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = mModCount;
        
        HashIterator(int type) {
            mType = type;
        }
        
        public boolean hasNext() {
            while (mEntry == null ||
                   (mEntryKey = mEntry.getKey()) == null) {
                if (mEntry != null) {
                    // Clean up after a cleared Reference.
                    remove(mEntry);
                    mEntry = mEntry.mNext;
                }

                if (mEntry == null) {
                    if (mIndex <= 0) {
                        return false;
                    }
                    else {
                        mEntry = mTable[--mIndex];
                    }
                }
            }

            return true;
        }
        
        public Object next() {
            if (mModCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            mLastReturned = mEntry;
            mEntry = mEntry.mNext;

            return mType == KEYS ? mLastReturned.getKey() :
                (mType == VALUES ? mLastReturned.getValue() : mLastReturned);
        }
        
        public void remove() {
            if (mLastReturned == null) {
                throw new IllegalStateException();
            }
            if (mModCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            remove(mLastReturned);
            mLastReturned = null;
        }

        private void remove(Entry toRemove) {
            Entry[] tab = mTable;
            int index = (toRemove.mHash & 0x7FFFFFFF) % tab.length;
            
            for (Entry e = tab[index], prev = null; e != null; e = e.mNext) {
                if (e == toRemove) {
                    mModCount++;
                    expectedModCount++;
                    if (prev == null) {
                        tab[index] = e.mNext;
                    }
                    else {
                        prev.mNext = e.mNext;
                    }
                    mCount--;
                    return;
                }
                else {
                    prev = e;
                }
            }
            throw new ConcurrentModificationException();
        }
    }
}
