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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/******************************************************************************
 * A ByteBuffer that accepts characters and Strings as well.
 *
 * @author Brian S O'Neill
 * @version
 * <!--$$Revision: 1.1 $-->, <!--$$JustDate:-->  9/07/00 <!-- $-->
 */
public interface CharToByteBuffer extends ByteBuffer {
    /**
     * Set the encoding for converting characters to bytes. Calling getEncoding
     * will return the canonical encoding name and may differ from the
     * encoding name provided to this method.
     */
    public void setEncoding(String enc)
        throws IOException, UnsupportedEncodingException;

    /**
     * Returns the current encoding that is being used to convert characters
     * to bytes or null if no encoding has been set yet. The encoding name
     * that is returned is canonical and may differ from the name passed into
     * setEncoding.
     */
    public String getEncoding() throws IOException;

    /**
     * Add one character to the end of this buffer.
     */
    public void append(char c) throws IOException;

    /**
     * Copy the given characters to the end of this buffer.
     */
    public void append(char[] chars) throws IOException;

    /**
     * Copy the given characters to the end of this buffer, starting at the
     * offset, using the length provided.
     */
    public void append(char[] chars, int offset, int length)
        throws IOException;

    /**
     * Copy the given String to the end of this buffer.
     */
    public void append(String str) throws IOException;

    /**
     * Copy the given String to the end of this buffer, starting at the offset,
     * using the length provided.
     */
    public void append(String str, int offset, int length) throws IOException;
}
