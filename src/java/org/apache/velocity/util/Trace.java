package org.apache.velocity.util;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
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
 * This class was originally written by Carl Ludwig <carl@destinymusic.com>. 
 * We appreciate his contributions. 
 */

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * A Java trace facility useful for generating stack traces and timing
 * information as a program executes. Each function that is traced and can
 * print out a summary performance report. All timings are real time
 * (i.e. wall clock). Below is an instrumented program with some sample output.
 * The <code>try</code> and <code>finally</code> blocks guarantee that the
 * trace is completed even if the method throws an exception:
 * <blockquote>
 * <pre>
 * import org.apache.velocity.util.*;
 * &nbsp;
 * class TraceTest
 * {
 *  public static void main(String argv[])
 *      throws InterruptedException
 *  {
 *      if (Trace.ON) Trace.start("main"); try {
 * &nbsp;
 *      Thread.currentThread().sleep(500);
 *      f();
 *      int i = g();
 *      if (Debug.ON) Debug.log("i: " + i);
 * &nbsp;
 *      } finally { if (Trace.ON) Trace.stop("main"); }
 *      if (Trace.ON) Trace.printTotals();
 *  }
 * &nbsp;
 *  public static void f()
 *      throws InterruptedException
 *  {
 *      if (Trace.ON) Trace.start("f"); try {
 * &nbsp;
 *      Thread.currentThread().sleep(200);
 *      //throw new InterruptedException();
 * &nbsp;
 *      } finally { if (Trace.ON) Trace.stop("f"); }
 *  }
 * &nbsp;
 *  public static int g()
 *      throws InterruptedException
 *  {
 *      if (Trace.ON) Trace.start("g"); try {
 * &nbsp;
 *      Thread.currentThread().sleep(100);
 *      f();
 *      return 3;
 * &nbsp;
 *      } finally { if (Trace.ON) Trace.stop("g"); }
 *  }
 * }
 * &nbsp;
 * % java -Dtrace=true TraceTest
 * enter main-main
 *   enter f-main
 *   leave f-main 0.2s
 *   enter g-main
 *     enter f-main
 *     leave f-main 0.2s
 *   leave g-main 0.3s
 * leave main-main 1.011s
 * &nbsp;
 * Trace Totals
 * ------------
 * main 1 calls 1.011s total    1.011s avg.
 * g    1 calls 0.3s total  0.3s avg.
 * f    2 calls 0.4s total  0.2s avg.
 * </pre>
 * </blockquote>
 * Other properties which can be set are <i>trace.detail</i>, which may be set 
 * to false to disable the printing of enter/leave statements, and 
 * <i>trace.pattern</i>, which will cause only the methods containing the 
 * string specified to be traced. You may also set <i>trace.log</i> to the
 * name of a file to redirect output to a file.
 *
 * @author <a href="mailto:carl@destinymusic.com">Carl Ludwig</a>
 * @version $Id: Trace.java,v 1.3 2001/01/02 23:43:44 dlr Exp $
 */ 
public class Trace
{
    /** Set via the trace property. */
    public static final boolean ON = active();

    /** The logging interface. */
    private static final PrintWriter LOG = getLog();

    /** Whether to log in detail. */
    private static final boolean SHOW_DETAIL = showDetail();

    /** String contained by the names of methods to be traced. */
    private static final String PATTERN = getPattern();

    /** Text which can be printed when entering a method. */
    private static final String ENTER = "enter ";

    /** Text which can be printed when leaving a method. */
    private static final String LEAVE = "leave ";

    /** Text to use for padding. */
    private static final String PAD = "  ";

    /** The numerical format to use when printing floating point numbers. */
    private static NumberFormat format = getFormat();

    /** Entry per thread. */
    private static Hashtable levels = new Hashtable();

    /** Entry per method+thread. */
    private static Hashtable timers = new Hashtable();

    /** Entry per method. */
    private static Hashtable totals = new Hashtable();

    /** Sorts and formats totals in a manner suitable for printing. */  
    public static final synchronized String getFormattedTotals()
    {
        // Sort the data from high to low time used.
        int i = 0;
        TraceData data[] = new TraceData[totals.size()];
        Enumeration e = totals.keys();
        while (e.hasMoreElements())
        {
            String method = (String)e.nextElement();
            TraceTotal t = (TraceTotal)totals.get(method);
            data[i++] = new TraceData(method, t);
        }
        Arrays.sort(data);
        StringBuffer buf = new StringBuffer(512);
        buf.append("Trace Totals\n");
        buf.append("------------\n");
        for (i = 0; i < data.length; i++)
        {
            buf.append(
                pad(data[i].method, 32, false) + " " +
                pad(Integer.toString(data[i].total.getCount()), 4, true) + " calls " +
                pad(format.format(data[i].total.getTotal()), 8, true) + "s total " +
                pad(format.format(data[i].total.getAverage()), 8, true) + "s avg.\n");
        }
        return buf.toString();
    }

    /** Pads/truncates a string. */
    private static final String pad(String s, int len, boolean rightJustify)
    {
        if (s.length() > len)
            s = s.substring(0, len-1) + '#';
        int padding = len - s.length();
        String pad = "";
        for (int i = 0; i < padding; i++)
            pad += " ";
        if (rightJustify)
            s = pad + s;
        else
            s = s + pad;
        return s;
    }

    /** Resets the trace totals data. */
    public static void reset()
    {
        totals.clear();
    }

    /** Begins trace of method. */
    public static final void start(String method)
    {
        if (!doTrace(method))
            return;

        TraceKey key = new TraceKey(method);
        Timer t = (Timer)timers.get(key);
        if (t == null)
        {
            t = new Timer();
            timers.put(key, t);
        }
        TraceLevel l = (TraceLevel)levels.get(key.threadName);
        if (l == null)
        {
            l = new TraceLevel();
            levels.put(key.threadName, l);
        }
        if (SHOW_DETAIL)
        {
            String pad = new String();
            for (int i = 0; i < l.level; i++)
                pad += PAD;
            LOG.println(pad + ENTER + key);
        }
        l.level++;
        t.start();
    }

    /** Ends trace of method. */
    public static final void stop(String method)
    {
        if (!doTrace(method))
            return;

        TraceKey key = new TraceKey(method);
        Timer t = (Timer)timers.get(key);
        if (t == null)
        {
            System.err.println(
                "Trace.stop: No trace started for " + key);
            return;
        }
        t.stop();
        TraceLevel l = (TraceLevel)levels.get(key.threadName);
        if (l == null)
        {
            System.err.println(
                "Trace.stop: Internal error; no level for thread " 
                + key.threadName);
            return;
        }
        l.level--;
        if (SHOW_DETAIL)
        {
            String pad = new String();
            for (int i = 0; i < l.level; i++)
                pad += PAD;
            LOG.println(pad + LEAVE + key + " " + t.getSeconds() + "s");
        }
        updateTotals(key.method, t.getSeconds());
    }

    /** Prints performance summary. */
    public static final synchronized void printTotals()
    {
        printTotals(LOG);
    }

    /** Prints performance summary. */
    public static final synchronized void printTotals(PrintStream ps)
    {
        printTotals(new PrintWriter(ps, true));
    }

    /** Prints performance summary. */
    public static final synchronized void printTotals(PrintWriter writer)
    {
        writer.println("\n" + getFormattedTotals());
    }

    static final boolean active()
    {
        String property = System.getProperty("trace");
        if (property != null)
            return Boolean.valueOf(property).booleanValue();
        else
            return false;
    }

    static final boolean doTrace(String method)
    {
        return (PATTERN == null || method.indexOf(PATTERN) != -1);
    }

    static final NumberFormat getFormat()
    {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        return format;
    }
    
    static final PrintWriter getLog()
    {
        String fileName = "";
        try
        {
            fileName = System.getProperty("trace.log");
            if (fileName != null && fileName.trim().length() > 0)
            {
                return new PrintWriter(new FileWriter(fileName, true), true);
            }
        }
        catch (Exception e)
        {
            System.err.println("Can't open trace log: " + 
                new File(fileName).getAbsolutePath() + " : " + e);
        }
        return new PrintWriter(System.out, true);
    }
    
    static final String getPattern()
    {
        return System.getProperty("trace.pattern");
    }

    static final boolean showDetail()
    {
        String property = System.getProperty("trace.detail");
        if (property != null)
            return Boolean.valueOf(property).booleanValue();
        else
            return true;
    }

    static final synchronized void updateTotals(String method, double seconds)
    {
        TraceTotal t = (TraceTotal)totals.get(method);
        if (t == null)
        {
            t = new TraceTotal();
            totals.put(method, t);
        }
        t.add(seconds);
    }
}

class TraceKey
{
    String method;
    String threadName;

    TraceKey(String method)
    {
        this.method = method;
        this.threadName = Thread.currentThread().getName();
    }

    public boolean equals(Object o)
    {
        return (this.getClass() == o.getClass()
            && this.method.equals(((TraceKey)o).method)
            && this.threadName.equals(((TraceKey)o).threadName));
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public String toString()
    {
        return new String(method + "-" + threadName);
    }
}

class TraceLevel
{
    int level = 0;
}

class TraceData implements Comparable
{
    String      method;
    TraceTotal  total;

    TraceData(String method, TraceTotal total)
    {
        this.method = method;
        this.total = total;
    }

    public int compareTo(Object comp)
    {
        // Reverse order sort.
        if (this.total.seconds < ((TraceData)comp).total.seconds)
            return 1;
        else if (this.total.seconds > ((TraceData)comp).total.seconds)
            return -1;
        else
            return 0;
    }
}

class TraceTotal
{
    double  seconds;
    int count;

    TraceTotal()
    {
        this.seconds = 0.0;
        count = 0;
    }

    void add(double seconds)
    {
        this.seconds += seconds;
        count++;
    }

    double getAverage()
    {
        return seconds / count;
    }

    int getCount()
    {
        return count;
    }

    double getTotal()
    {
        return seconds;
    }
}
