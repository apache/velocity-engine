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
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

/******************************************************************************
 * A ByteData implementation that reads the contents of a file.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public class FileByteData implements ByteData {
    private File mFile;
    private boolean mTriedToOpen;
    private RandomAccessFile mRAF;

    public FileByteData(File file) {
        mFile = file;
        // Open here so that if file isn't found, error is logged earlier.
        open();
    }

    public long getByteCount() throws IOException {
        // Keep file open to ensure that length doesn't change between call to
        // getByteCount and writeTo.

        RandomAccessFile raf = open();
        if (raf == null) {
            return 0;
        }
        else {
            return raf.length();
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        RandomAccessFile raf = open();
        if (raf == null) {
            return;
        }

        try {
            long length = raf.length();
            int bufSize;
            if (length > 4000) {
                bufSize = 4000;
            }
            else {
                bufSize = (int)length;
            }
            
            byte[] inputBuffer = new byte[bufSize];

            raf.seek(0);
        
            int readAmount;
            while ((readAmount = raf.read(inputBuffer, 0, bufSize)) > 0) {
                out.write(inputBuffer, 0, readAmount);
            }
        }
        finally {
            try {
                finalize();
            }
            catch (IOException e) {
            }
        }
    }

    public void reset() throws IOException {
        if (mRAF != null) {
            try {
                mRAF.close();
            }
            finally {
                mTriedToOpen = false;
                mRAF = null;
            }
        }       
    }

    protected final void finalize() throws IOException {
        reset();
    }

    private RandomAccessFile open() {
        if (!mTriedToOpen) {
            mTriedToOpen = true;
            try {
                mRAF = new RandomAccessFile(mFile, "r");
            }
            catch (IOException e) {
                Thread t = Thread.currentThread();
                t.getThreadGroup().uncaughtException(t, e);
            }
        }
        return mRAF;
    }
}
