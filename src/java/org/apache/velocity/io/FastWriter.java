package org.apache.velocity.io;

/* ====================================================================
 * Copyright (c) 1997-2000 Semiotek Inc.  All rights reserved.
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
 *    software must display the following acknowledgment: "This product 
 *    includes software developed by Justin Wells and Semiotek Inc. for 
 *    use in the WebMacro Servlet Framework (http://www.webmacro.org)."
 *
 * 4. The names "Semiotek Inc." and "WebMacro" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please contact
 *    justin@webmacro.org
 *
 * 5. Products derived from this software may not be called "WebMacro"
 *    nor may "WebMacro" appear in their names without prior written
 *    permission of Justin Wells.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment: "This product includes software developed by 
 *    Justin Wells and Semiotek Inc. for use in the WebMacro Servlet
 *    Framework (http://www.webmacro.org)."
 *
 * THIS SOFTWARE IS PROVIDED BY SEMIOTEK INC. ``AS IS'' AND ANY EXPRESSED 
 * OR IMPLIED WARRANTIES OR CONDITIONS, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SEMIOTEK INC. OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information on Semiotek Inc. and the WebMacro Servlet 
 * Framework project, please see <http://www.webmacro.org/>.
 *
 */

import java.io.*;


/**
  * This class allows you to write both char[] and byte[] data to an 
  * eventual outputstream. You have to specify the encoding. It tries
  * to be somewhat efficient. It is buffered. If you set the 
  * setAsciiHack(true) then performance increases dramatically but
  * the stream commits errors if it attempts to write non-ascii data.
  * You can turn the ascii hack on around segments of output where you
  * know no unicode data will be output.
  */

final public class FastWriter extends Writer
{

   OutputStream _out;                    // where our output goes
   String _encoding;                     // what encoding we use

   final sun.io.CharToByteConverter _c2b; // sun.io?! ugh. thank you sun.

   static private final int CSIZE = 512; // how much we buffer
   final char[] _cbuf = new char[CSIZE]; // buffer chars prior to convert
   int _cpos;                            // how much we used?


   private final int _BSIZE;             // size of the byte buffer
   final byte[] _bbuf;                   // reusable byte buffer

   boolean _asciiHack;                   // are we in fast mode?


   /**
    * Create a FastWriter to the target outputstream. You must specify
    * a character encoding. Try ut
    */
   public FastWriter(OutputStream target, String encoding) 
      throws java.io.UnsupportedEncodingException
   {
      _encoding = encoding;
      _out = new BufferedOutputStream(target); // XXX: we should re-use this somehow

      _c2b = sun.io.CharToByteConverter.getConverter(encoding);
      _BSIZE = CSIZE * _c2b.getMaxBytesPerChar();
      _bbuf = new byte[_BSIZE];

      _cpos = 0;

      _asciiHack = false;
   }

   public String getEncoding() {
      return _encoding;
   }

   /**
    * Reset the output stream to enable re-use of the FastWriter. If 
    * there is anything buffered this wipes it out.
    */
   public void recycle(OutputStream newOut, String encoding) {
      _out = new BufferedOutputStream(newOut); // XXX: we should re-use this somehow
      _encoding = encoding;
      _cpos = 0;
   }

   /**
    * Ordinarily an expensive char-to-byte routine is used to convert
    * strings and char[]'s to byte format. If you know that your data
    * is going to be ASCII only for some number of writes, turn on 
    * this AsciiHack and then write the ASCII data. It's much faster. 
    * Remember to turn the AsciiHack off before writing true Unicode
    * characters, otherwise they'll be mangled.
    */
   public void setAsciiHack(boolean on) throws IOException
   {
      if (_cpos != 0) cflush();
      _asciiHack = on;
   }

   /**
    * Write a character to the output stream
    */
   public void write(int b) throws IOException 
   {
      if ((_cpos + 1) > CSIZE) cflush();
      _cbuf[_cpos] = (char) b;
      _cpos++; 
   }

   /**
    * Write a byte to the output stream.
    */
   public void write(byte b) throws IOException
   {
      if (_cpos != 0) cflush();
      _out.write(b);
   }

   /**
     * Write unicode characters. 
     */
   public void write(char[] c) throws IOException 
   {
      this.write(c,0,c.length);
   }

   /**
     * Write unicode characters. 
     */
   public void write(char[] c, int offset, int len) throws IOException 
   {
      if ((_cpos + len) > CSIZE) {
         cflush();
         if (len > CSIZE) {
            writeChars(c,offset,len); // won't fit in our buffer anyway
            return;
         }
      }
      System.arraycopy(c, offset, _cbuf, _cpos, len); 
      _cpos += len;
   }

   /**
    * Write bytes directly to the output stream with no unicode
    * conversion
    */
   public void write(byte[] b) throws IOException 
   {
      if (_cpos != 0) cflush();
      _out.write(b);
   }

   /**
    * Write bytes directly to the output stream with no unicode
    * conversion
    */
   public void write(byte[] b, int offset, int len) throws IOException 
   {
      if (_cpos != 0) cflush();
      _out.write(b,offset,len);
   }

   /**
     * If ASCII hack is on, write this string out efficiently. Otherwise
     * do full correct and slow unicode conversion before writing.
     */
   public void write(String s, int offset, int len) throws IOException 
   {
      if (_asciiHack) {
         if (_cpos != 0) cflush();
         writeAsciiBytes(s,offset,len);
         return;
      } 
      if ((_cpos + len) > CSIZE) {
         cflush();
         if (len > CSIZE) {
            _out.write(s.getBytes(_encoding));
            return;
         }
      } 
      s.getChars(offset,offset + len,_cbuf,_cpos);
      _cpos += len;
   }

   /**
     * Just another way to call write(String,int,int)
     */
   public void write(String s) throws IOException 
   {
      this.write(s,0,s.length());
   }

   /**
     * Efficiently write this non-Unicode string to the output 
     * stream. This will mangle the string if it is unicode.
     */
   private void writeAsciiBytes(String buf, int offset, int len) 
   throws IOException 
   {
      while (len > 0) {
         int max = (len < _BSIZE) ? len : _BSIZE;
	 buf.getChars(offset, offset + max, _cbuf, 0); 
         for (int i = 0; i < max; i++) {
            _bbuf[i] = (byte) _cbuf[i];
         }
         len -= max;
         offset += max;
         _out.write(_bbuf,0,max);
      }
   }

   /**
     * Write characters to to the output stream performing slow unicode
     * conversion unless the AsciiHack is on. 
     */
   private void writeChars(char[] cbuf, int offset, int len) 
      throws IOException
   {
      try {
         if (_asciiHack) {
            // cheat
            while (len > 0) {
               int max = (len < _BSIZE) ? len : _BSIZE;
               for (int i = 0; i < max; i++) {
                  _bbuf[i] = (byte) cbuf[i + offset];
               }
               _out.write(_bbuf,0,max);
               len -= max;
               offset += max;
            }
         } else {
            // slow, but correct
            int nextC = 0;
            while (nextC < _cpos) {
               nextC += _c2b.convert(cbuf,nextC,_cpos,_bbuf,0,_BSIZE); 
               _out.write(_bbuf, 0, _c2b.nextByteIndex());
            } 
         }
      } catch (Exception e) {
        throw new IOException("Unicode conversion error: " + e);
      } finally {
        _c2b.reset();
      }
   }

   /**
    * Catch up: flush out the local buffers but not the stream. Character
    * data is buffered locally to avoid too many calls to the unicode 
    * encoding mechanism. This method flushes just those buffers.
    */
   public void cflush() throws IOException
   {
       writeChars(_cbuf,0,_cpos);
       _cpos = 0;
   }

   /**
     * Flush out all the buffers
     */
   public void flush() throws IOException
   {
      cflush();
      _out.flush();
   }

   /**
     * Close the stream. you can use recycle() to re-use this object.
     */
   public void close() throws IOException
   {
      cflush();
      _out.close();
      _out = null;
   }

   public static void main(String arg[]) {

      System.out.println("----START----");
      try {
         FastWriter fw = new FastWriter(System.out, "UTF8");
         fw.setAsciiHack(true);
         for (int i = 0; i < arg.length; i++) {
            fw.write(arg[i]);
            fw.write(arg[i].getBytes());
         }
         fw.flush();
      } catch (Exception e) {
         e.printStackTrace();
      }
      System.out.println("----DONE----");

   }

}
