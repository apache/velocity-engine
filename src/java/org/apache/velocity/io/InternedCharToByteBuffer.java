package org.apache.velocity.io;

/* ====================================================================
 * TeaServlet - Copyright (c) 1999-2000 Walt Disney Internet Group
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

import java.io.*;
import java.util.*;

/******************************************************************************
 * A CharToByteBuffer that keeps track of interned strings (mainly string
 * literals) and statically caches the results of those strings after applying
 * a byte conversion. This can improve performance if many of the strings being
 * passed to the append method have been converted before.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class InternedCharToByteBuffer implements CharToByteBuffer {
    private static final Object MARKER = new Object();

    // Maps String encodings to caches of Strings converted using that
    // encoding. Use a TreeMap because it is good for small maps.
    private static Map cEncodings = new TreeMap();

    private static Random cLastRandom = new Random();

    private static Map getConvertedCache(String encoding) {
        synchronized (cEncodings) {
            Map cache = (Map)cEncodings.get(encoding);
            if (cache == null) {
                cache =
                    Collections.synchronizedMap(new IdentityMap());
                cEncodings.put(encoding, cache);
            }
            return cache;
        }
    }

    private static Random getRandom() {
        synchronized (cLastRandom) {
            return cLastRandom = new Random(cLastRandom.nextLong());
        }
    }

    private CharToByteBuffer mBuffer;
    private Map mConvertedCache;
    private Random mRandom;

    public InternedCharToByteBuffer(CharToByteBuffer buffer)
        throws IOException
    {
        mBuffer = buffer;
        String encoding = buffer.getEncoding();
        if (encoding != null) {
            mConvertedCache = getConvertedCache(buffer.getEncoding());
        }
        mRandom = getRandom();
    }

    public void setEncoding(String enc) throws IOException {
        mBuffer.setEncoding(enc);
        mConvertedCache = getConvertedCache(mBuffer.getEncoding());
    }

    public String getEncoding() throws IOException {
        return mBuffer.getEncoding();
    }

    public long getBaseByteCount() throws IOException {
        return mBuffer.getBaseByteCount();
    }

    public long getByteCount() throws IOException {
        return mBuffer.getByteCount();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        mBuffer.writeTo(out);
    }
    
    public void append(byte b) throws IOException {
        mBuffer.append(b);
    }
    
    public void append(byte[] bytes) throws IOException {
        mBuffer.append(bytes);
    }
    
    public void append(byte[] bytes, int offset, int length)
        throws IOException {

        mBuffer.append(bytes, offset, length);
    }
    
    public void appendSurrogate(ByteData s) throws IOException {
        mBuffer.appendSurrogate(s);
    }

    public void addCaptureBuffer(ByteBuffer buffer) throws IOException {
        mBuffer.addCaptureBuffer(buffer);
    }
    
    public void removeCaptureBuffer(ByteBuffer buffer) throws IOException {
        mBuffer.removeCaptureBuffer(buffer);
    }
    
    public void append(char c) throws IOException {
        mBuffer.append(c);
    }
    
    public void append(char[] chars) throws IOException {
        mBuffer.append(chars);
    }
    
    public void append(char[] chars, int offset, int length) 
        throws IOException {

        mBuffer.append(chars, offset, length);
    }
    
    public void append(String str) throws IOException {
        if (str.length() == 0) {
            return;
        }

        Map cache;
        if ((cache = mConvertedCache) == null) {
            mBuffer.append(str);
            return;
        }

        // Caching performed using a two pass technique. This is done to
        // avoid the cost of String.getBytes() for strings that aren't
        // actually interned.

        Object value;

        if ((value = cache.get(str)) != null) {
            ///*
            byte[] bytes;
            if (value != MARKER) {
                bytes = (byte[])value;
            }
            else {
                // This is at least the second time the string has been seen,
                // so assume it has been interned and call String.getBytes().
                bytes = str.getBytes(getEncoding());
                cache.put(str, bytes);
            }

            mBuffer.append(bytes);
            //*/

            /*
            ByteData data;
            if (value != MARKER) {
                data = (ByteData)value;
            }
            else {
                // This is at least the second time the string has been seen,
                // so assume it has been interned and call String.getBytes().
                data = new ArrayByteData(str.getBytes(getEncoding()));
                cache.put(str, data);
            }

            mBuffer.appendSurrogate(data);
            */
        }
        else {
            // Just put a marker at first to indicate that the string has been
            // seen, but don't call String.getBytes() just yet.
            if ((mRandom.nextInt() % 20) == 0) {
                // Only mark sometimes in order to reduce the amount of times
                // put is called for strings that will never be seen again.
                // Calculating a random number is cheaper than putting into an
                // IdentityMap because no objects are created. A consequence of
                // this optimization is that it will take more iterations to
                // discover the real string literals, but they will be
                // discovered eventually.
                cache.put(str, MARKER);
            }
            mBuffer.append(str);
        }
    }
    
    public void append(String str, int offset, int length) throws IOException {
        mBuffer.append(str, offset, length);
    }

    public void reset() throws IOException {
        mBuffer.reset();
    }
}
