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

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/******************************************************************************
 * A ByteBuffer implementation that keeps byte data in memory.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class DefaultByteBuffer implements ByteBuffer {
    private static final int BUFFER_SIZE = 512;

    // A List of ByteData instances.
    private List mChunks;

    private byte[] mBuffer;
    private int mCursor;

    private int mBaseCount;

    private List mCaptureBuffers;

    public DefaultByteBuffer() {
        mChunks = new ArrayList(100);
    }

    public long getBaseByteCount() {
        if (mBuffer != null) {
            return mBaseCount + mCursor;
        }
        else {
            return mBaseCount;
        }
    }

    public long getByteCount() throws IOException {
        long count;
        if (mBuffer != null) {
            count = mCursor;
        }
        else {
            count = 0;
        }

        int size = mChunks.size();
        for (int i=0; i<size; i++) {
            count += ((ByteData)mChunks.get(i)).getByteCount();
        }

        return count;
    }

    public void writeTo(OutputStream out) throws IOException {
        int size = mChunks.size();
        for (int i=0; i<size; i++) {
            ((ByteData)mChunks.get(i)).writeTo(out);
        }

        if (mBuffer != null && mCursor != 0) {
            out.write(mBuffer, 0, mCursor);
        }
    }

    public void append(byte b) throws IOException {
        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            int size = captureBuffers.size();
            for (int i=0; i<size; i++) {
                ((ByteBuffer)captureBuffers.get(i)).append(b);
            }
        }

        if (mBuffer == null) {
            mBuffer = new byte[BUFFER_SIZE];
            mCursor = 0;
        }
        else if (mCursor >= mBuffer.length) {
            mChunks.add(new ArrayByteData(mBuffer));
            mBaseCount += BUFFER_SIZE;
            mBuffer = new byte[BUFFER_SIZE];
            mCursor = 0;
        }

        mBuffer[mCursor++] = b;
    }

    public void append(byte[] bytes) throws IOException {
        append(bytes, 0, bytes.length);
    }

    public void append(byte[] bytes, int offset, int length)
        throws IOException
    {
        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            int size = captureBuffers.size();
            for (int i=0; i<size; i++) {
                ((ByteBuffer)captureBuffers.get(i)).append
                    (bytes, offset, length);
            }
        }

        while (length > 0) {
            if (mBuffer == null) {
                if (length >= BUFFER_SIZE) {
                    byte[] copy = new byte[length];
                    System.arraycopy(bytes, offset, copy, 0, length);
                    mChunks.add(new ArrayByteData(copy));
                    mBaseCount += length;
                    return;
                }
                
                mBuffer = new byte[BUFFER_SIZE];
                mCursor = 0;
            }
            
            int available = BUFFER_SIZE - mCursor;
            
            if (length <= available) {
                System.arraycopy(bytes, offset, mBuffer, mCursor, length);
                mCursor += length;
                return;
            }
            
            System.arraycopy(bytes, offset, mBuffer, mCursor, available);
            mChunks.add(new ArrayByteData(mBuffer));
            mBaseCount += BUFFER_SIZE;
            mBuffer = null;
            offset += available;
            length -= available;
        }
    }

    public void appendSurrogate(ByteData s) throws IOException {
        if (s == null) {
            return;
        }

        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            int size = captureBuffers.size();
            for (int i=0; i<size; i++) {
                ((ByteBuffer)captureBuffers.get(i)).appendSurrogate(s);
            }
        }

        if (mBuffer != null && mCursor > 0) {
            mChunks.add(new ArrayByteData(mBuffer, 0, mCursor));
            mBaseCount += mCursor;
            mBuffer = null;
        }
        mChunks.add(s);
    }

    public void addCaptureBuffer(ByteBuffer buffer) {
        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) == null) {
            captureBuffers = mCaptureBuffers = new ArrayList();
        }
        captureBuffers.add(buffer);
    }

    public void removeCaptureBuffer(ByteBuffer buffer) {
        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            captureBuffers.remove(buffer);
        }
    }

    public void reset() throws IOException {
        int size = mChunks.size();
        for (int i=0; i<size; i++) {
            ((ByteData)mChunks.get(i)).reset();
        }

        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            size = captureBuffers.size();
            for (int i=0; i<size; i++) {
                ((ByteData)captureBuffers.get(i)).reset();
            }
        }
    }
}
