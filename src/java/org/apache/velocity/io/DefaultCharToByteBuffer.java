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

/******************************************************************************
 * A CharToByteBuffer implementation that wraps a ByteBuffer for storage.
 * 
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class DefaultCharToByteBuffer implements CharToByteBuffer {
    private ByteBuffer mBuffer;
    private OutputStreamWriter mConvertor;

    private char[] mChars;
    private int mCapacity;
    private int mCursor;
    
    private String mDefaultEncoding;

    /**
     * @param buffer Buffer that receives the characters converted to bytes.
     */    
    public DefaultCharToByteBuffer(ByteBuffer buffer) {
        this(buffer, null);
    }

    /**
     * @param buffer Buffer that receives the characters converted to bytes.
     * @param defaultEncoding Default character encoding to use if setEncoding
     * is not called.
     */    
    public DefaultCharToByteBuffer(ByteBuffer buffer, String defaultEncoding) {
        mBuffer = buffer;
        mChars = new char[4000];
        mCapacity = mChars.length;
        mDefaultEncoding = defaultEncoding;
    }

    public void setEncoding(String enc) throws IOException {
        drain(true);
        mConvertor = new OutputStreamWriter
            (new ByteBufferOutputStream(mBuffer), enc);
    }
    
    public String getEncoding() {
        return (mConvertor == null) ? mDefaultEncoding :
            mConvertor.getEncoding();
    }
    
    public long getBaseByteCount() throws IOException {
        return mBuffer.getBaseByteCount();
    }

    public long getByteCount() throws IOException {
        drain(true);
        return mBuffer.getByteCount();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        drain(true);
        mBuffer.writeTo(out);
    }
    
    public void append(byte b) throws IOException {
        drain(true);
        mBuffer.append(b);
    }
    
    public void append(byte[] bytes) throws IOException {
        append(bytes, 0, bytes.length);
    }
    
    public void append(byte[] bytes, int offset, int length)
        throws IOException {

        if (length != 0) {
            drain(true);
            mBuffer.append(bytes, offset, length);
        }
    }
    
    public void appendSurrogate(ByteData s) throws IOException {
        if (s != null) {
            drain(true);
            mBuffer.appendSurrogate(s);
        }
    }
    
    public void addCaptureBuffer(ByteBuffer buffer) throws IOException {
        drain(true);
        mBuffer.addCaptureBuffer(buffer);
    }

    public void removeCaptureBuffer(ByteBuffer buffer) throws IOException {
        drain(true);
        mBuffer.removeCaptureBuffer(buffer);
    }

    public void append(char c) throws IOException {
        if (mCursor >= mCapacity) {
            drain(false);
        }
        mChars[mCursor++] = c;
    }
    
    public void append(char[] chars) throws IOException {
        append(chars, 0, chars.length);
    }
    
    public void append(char[] chars, int offset, int length) 
        throws IOException 
    {
        if (length == 0) {
            return;
        }

        int capacity = mCapacity;

        if (length < (capacity - mCursor)) {
            System.arraycopy(chars, offset, mChars, mCursor, length);
            mCursor += length;
            return;
        }

        // Make room and try again.
        drain(false);

        if (length < capacity) {
            System.arraycopy(chars, offset, mChars, mCursor, length);
            mCursor += length;
            return;
        }

        // Write the whole chunk out at once.
        getConvertor().write(chars, offset, length);
    }
    
    public void append(String str) throws IOException {
        append(str, 0, str.length());
    }
    
    public void append(String str, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }

        int capacity = mCapacity;
        int avail = capacity - mCursor;

        if (length <= avail) {
            str.getChars(offset, offset + length, mChars, mCursor);
            mCursor += length;
            return;
        }

        // Fill up the rest of the character buffer and drain it.
        str.getChars(offset, offset + avail, mChars, mCursor);
        offset += avail;
        length -= avail;
        mCursor = capacity;
        drain(false);

        // Drain chunks that completely fill the character buffer.
        while (length >= capacity) {
            str.getChars(offset, offset + capacity, mChars, 0);
            offset += capacity;
            length -= capacity;
            mCursor = capacity;
            drain(false);
        }

        // Copy the remainder into the character buffer, but don't drain.
        if (length > 0) {
            str.getChars(offset, offset + length, mChars, 0);
            mCursor = length;
        }
    }

    public void reset() throws IOException {
        mBuffer.reset();
    }

    private OutputStreamWriter getConvertor()
        throws UnsupportedEncodingException
    {
        if (mConvertor == null) {
            if (mDefaultEncoding == null) {
                mConvertor = new OutputStreamWriter
                    (new ByteBufferOutputStream(mBuffer));
            }
            else {
                mConvertor = new OutputStreamWriter
                    (new ByteBufferOutputStream(mBuffer), mDefaultEncoding);
            }
        }
        return mConvertor;
    }

    private void drain(boolean flush) throws IOException {
        if (mCursor != 0) {
            try {
                getConvertor().write(mChars, 0, mCursor);
            }
            finally {
                mCursor = 0;
            }
        }

        if (flush && mConvertor != null) {
            mConvertor.flush();
        }
    }
}
