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
 * A ByteBuffer implementation that initially stores its data in a
 * DefaultByteBuffer, but after a certain threshold is reached, spills over
 * into a FileByteBuffer.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class SpilloverByteBuffer implements ByteBuffer {
    private Group mGroup;
    
    private ByteBuffer mLocalBuffer;
    private ByteBuffer mSpillover;

    private List mCaptureBuffers;

    /**
     * Create a SpilloverByteBuffer against a Group that sets the threshold
     * and can create a spillover FileByteBuffer. The Group can be shared
     * among many SpilloverByteBuffers.
     *
     * @param group a group that can be shared among many SpilloverByteBuffers
     */
    public SpilloverByteBuffer(Group group) {
        mGroup = group;
        mLocalBuffer = new DefaultByteBuffer();
    }

    public long getBaseByteCount() throws IOException {
        if (mSpillover == null) {
            return mLocalBuffer.getBaseByteCount();
        }
        else {
            return mSpillover.getBaseByteCount();
        }
    }

    public long getByteCount() throws IOException {
        if (mSpillover == null) {
            return mLocalBuffer.getByteCount();
        }
        else {
            return mSpillover.getByteCount();
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        if (mSpillover == null) {
            mLocalBuffer.writeTo(out);
        }
        else {
            mSpillover.writeTo(out);
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

        if (mSpillover == null) {
            if (mGroup.adjustLevel(1)) {
                mLocalBuffer.append(b);
                return;
            }
            spillover();
        }

        mSpillover.append(b);
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

        if (mSpillover == null) {
            if (mGroup.adjustLevel(length)) {
                mLocalBuffer.append(bytes, offset, length);
                return;
            }
            spillover();
        }

        mSpillover.append(bytes, offset, length);
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

        if (mSpillover == null) {
            mLocalBuffer.appendSurrogate(s);
        }
        else {
            mSpillover.appendSurrogate(s);
        }
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
        mLocalBuffer.reset();
        if (mSpillover != null) {
            mSpillover.reset();
        }
        
        List captureBuffers;
        if ((captureBuffers = mCaptureBuffers) != null) {
            int size = captureBuffers.size();
            for (int i=0; i<size; i++) {
                ((ByteData)captureBuffers.get(i)).reset();
            }
        }
    }

    protected void finalize() throws IOException {
        if (mLocalBuffer != null) {
            long count = mLocalBuffer.getBaseByteCount();
            mLocalBuffer = null;
            mGroup.adjustLevel(-count);
        }
    }

    private void spillover() throws IOException {
        mSpillover = mGroup.createFileByteBuffer();
        // TODO: This is bad! By writing out the contents of the existing
        // buffer early, surrogates are evaluated too soon!
        mLocalBuffer.writeTo(new ByteBufferOutputStream(mSpillover));

        long count = mLocalBuffer.getBaseByteCount();
        mLocalBuffer = null;
        mGroup.adjustLevel(-count);
    }

    public static abstract class Group {
        private final long mThreshold;
        private long mLevel;

        public Group(long threshold) {
            mThreshold = threshold;
        }

        public final long getThreshold() {
            return mThreshold;
        }

        public final synchronized long getCurrentLevel() {
            return mLevel;
        }

        public abstract FileByteBuffer createFileByteBuffer()
            throws IOException;

        synchronized boolean adjustLevel(long delta) {
            long newLevel;
            if ((newLevel = mLevel + delta) > mThreshold) {
                return false;
            }
            else {
                if (newLevel < 0) {
                    newLevel = 0;
                }
                mLevel = newLevel;
                return true;
            }
        }
    }
}
