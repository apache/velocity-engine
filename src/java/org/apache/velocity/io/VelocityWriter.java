package org.apache.velocity.io;

/*
 * $Header: /home/cvs/jakarta-velocity/src/java/org/apache/velocity/io/VelocityWriter.java,v 1.2 2000/12/20 07:07:25 jvanzyl Exp $
 * $Revision: 1.2 $
 * $Date: 2000/12/20 07:07:25 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * <p>
 * The actions and template data in a JSP page is written using the
 * JspWriter object that is referenced by the implicit variable out which
 * is initialized automatically using methods in the PageContext object.
 * <p>
 * This abstract class emulates some of the functionality found in the
 * java.io.BufferedWriter and java.io.PrintWriter classes,
 * however it differs in that it throws java.io.IOException from the print
 * methods while PrintWriter does not.
 * <p><B>Buffering</B>
 * <p>
 * The initial JspWriter object is associated with the PrintWriter object
 * of the ServletResponse in a way that depends on whether the page is or
 * is not buffered. If the page is not buffered, output written to this
 * JspWriter object will be written through to the PrintWriter directly,
 * which will be created if necessary by invoking the getWriter() method
 * on the response object. But if the page is buffered, the PrintWriter
 * object will not be created until the buffer is flushed and
 * operations like setContentType() are legal. Since this flexibility
 * simplifies programming substantially, buffering is the default for JSP
 * pages.
 * <p>
 * Buffering raises the issue of what to do when the buffer is
 * exceeded. Two approaches can be taken:
 * <ul>
 * <li>
 * Exceeding the buffer is not a fatal error; when the buffer is
 * exceeded, just flush the output.
 * <li>
 * Exceeding the buffer is a fatal error; when the buffer is exceeded,
 * raise an exception.
 * </ul>
 * <p>
 * Both approaches are valid, and thus both are supported in the JSP
 * technology. The behavior of a page is controlled by the autoFlush
 * attribute, which defaults to true. In general, JSP pages that need to
 * be sure that correct and complete data has been sent to their client
 * may want to set autoFlush to false, with a typical case being that
 * where the client is an application itself. On the other hand, JSP
 * pages that send data that is meaningful even when partially
 * constructed may want to set autoFlush to true; such as when the
 * data is sent for immediate display through a browser. Each application
 * will need to consider their specific needs.
 * <p>
 * An alternative considered was to make the buffer size unbounded; but,
 * this had the disadvantage that runaway computations would consume an
 * unbounded amount of resources.
 * <p>
 * The "out" implicit variable of a JSP implementation class is of this type.
 * If the page directive selects autoflush="true" then all the I/O operations
 * on this class shall automatically flush the contents of the buffer if an
 * overflow condition would result if the current operation were performed
 * without a flush. If autoflush="false" then all the I/O operations on this
 * class shall throw an IOException if performing the current operation would
 * result in a buffer overflow condition.
 *
 * @see java.io.Writer
 * @see java.io.BufferedWriter
 * @see java.io.PrintWriter
 *
 * Write text to a character-output stream, buffering characters so as
 * to provide for the efficient writing of single characters, arrays,
 * and strings. 
 *
 * Provide support for discarding for the output that has been 
 * buffered. 
 * 
 * This needs revisiting when the buffering problems in the JSP spec
 * are fixed -akv 
 *
 * @author Anil K. Vijendran
 */
public final class VelocityWriter extends Writer
{
    /**
     * constant indicating that the Writer is not buffering output
     */
    public static final int	NO_BUFFER = 0;

    /**
     * constant indicating that the Writer is buffered and is using the 
     * implementation default buffer size
     */
    public static final int	DEFAULT_BUFFER = -1;

    /**
     * constant indicating that the Writer is buffered and is unbounded; 
     * this is used in BodyContent
     */
    public static final int	UNBOUNDED_BUFFER = -2;

    protected int     bufferSize;
    protected boolean autoFlush;

    private Writer writer;
    
    private char cb[];
    private int nextChar;

    private static int defaultCharBufferSize = 8 * 1024;

    private boolean flushed = false;

    /**
     * Create a buffered character-output stream that uses a default-sized
     * output buffer.
     *
     * @param  response  A Servlet Response
     */
    public VelocityWriter(Writer writer)
    {
        this(writer, defaultCharBufferSize, true);
    }

    /**
     * private constructor.
     */
    private VelocityWriter(int bufferSize, boolean autoFlush)
    {
        this.bufferSize = bufferSize;
        this.autoFlush  = autoFlush;
    }

    /**
     * This method returns the size of the buffer used by the JspWriter.
     *
     * @return the size of the buffer in bytes, or 0 is unbuffered.
     */
    public int getBufferSize() { return bufferSize; }

    /**
     * This method indicates whether the JspWriter is autoFlushing.
     *
     * @return if this JspWriter is auto flushing or throwing IOExceptions on buffer overflow conditions
     */
    public boolean isAutoFlush() { return autoFlush; }

    /**
     * Create a new buffered character-output stream that uses an output
     * buffer of the given size.
     *
     * @param  response A Servlet Response
     * @param  sz   	Output-buffer size, a positive integer
     *
     * @exception  IllegalArgumentException  If sz is <= 0
     */
    public VelocityWriter(Writer writer, int sz, boolean autoFlush)
    {
        this(sz, autoFlush);
        if (sz < 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.writer = writer;
        cb = sz == 0 ? null : new char[sz];
        nextChar = 0;
    }

    private final void init( Writer writer, int sz, boolean autoFlush )
    {
        this.writer= writer;
        if( sz > 0 && ( cb == null || sz > cb.length ) )
            cb=new char[sz];
        nextChar = 0;
        this.autoFlush=autoFlush;
        this.bufferSize=sz;
    }

    /**
     * Flush the output buffer to the underlying character stream, without
     * flushing the stream itself.  This method is non-private only so that it
     * may be invoked by PrintStream.
     */
    private final void flushBuffer() throws IOException
    {
        if (bufferSize == 0)
            return;
        flushed = true;
        if (nextChar == 0)
            return;
        writer.write(cb, 0, nextChar);
        nextChar = 0;
    }

    /**
     * Discard the output buffer.
     */
    public final void clear()
    {
        nextChar = 0;
    }

    private final void bufferOverflow() throws IOException
    {
        throw new IOException("overflow");
    }

    /**
     * Flush the stream.
     *
     */
    public final void flush()  throws IOException
    {
        flushBuffer();
        if (writer != null)
        {
            writer.flush();
        }
    }

    /**
     * Close the stream.
     *
     */
    public final void close() throws IOException {
        if (writer == null)
            return;
        flush();
    }

    /**
     * @return the number of bytes unused in the buffer
     */
    public final int getRemaining()
    {
        return bufferSize - nextChar;
    }

    /**
     * Write a single character.
     *
     */
    public final void write(int c) throws IOException
    {
        if (bufferSize == 0)
        {
            writer.write(c);
        }
        else
        {
            if (nextChar >= bufferSize)
                if (autoFlush)
                    flushBuffer();
                else
                    bufferOverflow();
            cb[nextChar++] = (char) c;
        }
    }

    /**
     * Our own little min method, to avoid loading java.lang.Math if we've run
     * out of file descriptors and we're trying to print a stack trace.
     */
    private final int min(int a, int b)
    {
	    if (a < b) return a;
    	    return b;
    }

    /**
     * Write a portion of an array of characters.
     *
     * <p> Ordinarily this method stores characters from the given array into
     * this stream's buffer, flushing the buffer to the underlying stream as
     * needed.  If the requested length is at least as large as the buffer,
     * however, then this method will flush the buffer and write the characters
     * directly to the underlying stream.  Thus redundant
     * <code>DiscardableBufferedWriter</code>s will not copy data unnecessarily.
     *
     * @param  cbuf  A character array
     * @param  off   Offset from which to start reading characters
     * @param  len   Number of characters to write
     *
     */
    public final void write(char cbuf[], int off, int len) 
        throws IOException 
    {
        if (bufferSize == 0)
        {
            writer.write(cbuf, off, len);
            return;
        }

        if (len == 0)
        {
            return;
        } 

        if (len >= bufferSize)
        {
            /* If the request length exceeds the size of the output buffer,
            flush the buffer and then write the data directly.  In this
            way buffered streams will cascade harmlessly. */
            if (autoFlush)
                flushBuffer();
            else
                bufferOverflow();
                writer.write(cbuf, off, len);
            return;
        }

        int b = off, t = off + len;
        while (b < t)
        {
            int d = min(bufferSize - nextChar, t - b);
            System.arraycopy(cbuf, b, cb, nextChar, d);
            b += d;
            nextChar += d;
            if (nextChar >= bufferSize) 
                if (autoFlush)
                    flushBuffer();
                else
                    bufferOverflow();
        }
    }

    /**
     * Write an array of characters.  This method cannot be inherited from the
     * Writer class because it must suppress I/O exceptions.
     */
    public final void write(char buf[]) throws IOException
    {
    	write(buf, 0, buf.length);
    }

    /**
     * Write a portion of a String.
     *
     * @param  s     String to be written
     * @param  off   Offset from which to start reading characters
     * @param  len   Number of characters to be written
     *
     */
    public final void write(String s, int off, int len) throws IOException
    {
        if (bufferSize == 0)
        {
            writer.write(s, off, len);
            return;
        }
        int b = off, t = off + len;
        while (b < t)
        {
            int d = min(bufferSize - nextChar, t - b);
            s.getChars(b, b + d, cb, nextChar);
            b += d;
            nextChar += d;
            if (nextChar >= bufferSize) 
                if (autoFlush)
                    flushBuffer();
                else
                    bufferOverflow();
        }
    }

    /**
     * Write a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     */
    public final void write(String s) throws IOException
    {
    	write(s, 0, s.length());
    }

    /**
     * resets this class so that it can be reused
     *
     */
    public final void recycle(OutputStreamWriter writer)
    {
        this.writer = writer;
        flushed = false;
        clear();
    }
}
