package org.apache.velocity.test.misc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Logger implementation that can easily capture output
 * or suppress it entirely.  By default, both capture and suppress
 * are on. To have this behave like a normal SystemLogChute,
 * you must turn it on() and stopCapture().
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author Nathan Bubna
 * @author <a href="mailto:cbrisson@apache.org">Claude Brisson</a>
 * @version $Id$
 */
public class TestLogger extends MarkerIgnoringBase
{
    private ByteArrayOutputStream log;
    private PrintStream systemDotIn;

    private boolean suppress = true;
    private boolean capture = true;
    private int enabledLevel = LOG_LEVEL_INFO;

    public TestLogger()
    {
        this(true, true);
    }

    public TestLogger(boolean suppress, boolean capture)
    {
        this.suppress = suppress;
        this.capture = capture;
        if (suppress)
        {
            off();
        }
        else if (capture)
        {
            startCapture();
        }
    }

    public static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
    public static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
    public static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
    public static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
    public static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;

    private static int stringToLevel(String levelStr)
    {
        if ("trace".equalsIgnoreCase(levelStr)) return LOG_LEVEL_TRACE;
        else if ("debug".equalsIgnoreCase(levelStr)) return LOG_LEVEL_DEBUG;
        else if ("info".equalsIgnoreCase(levelStr)) return LOG_LEVEL_INFO;
        else if ("warn".equalsIgnoreCase(levelStr)) return LOG_LEVEL_WARN;
        else if ("error".equalsIgnoreCase(levelStr)) return LOG_LEVEL_ERROR;
        // assume INFO by default
        return LOG_LEVEL_INFO;
    }

    private static String getPrefix(int level)
    {
        if (level <= LOG_LEVEL_TRACE) return " [trace] ";
        else if (level <= LOG_LEVEL_DEBUG) return " [debug] ";
        else if (level <= LOG_LEVEL_INFO) return "  [info] ";
        else if (level <= LOG_LEVEL_WARN) return "  [warn] ";
        else return " [error]";
    }

    public synchronized void on()
    {
        if (suppress)
        {
            suppress = false;
            if (capture)
            {
                startCapture();
            }
        }
    }

    public synchronized void off()
    {
        suppress = true;
    }

    public synchronized void startCapture()
    {
        capture = true;
        if (!suppress)
        {
            log = new ByteArrayOutputStream();
            systemDotIn = new PrintStream(log, true);
        }
    }

    public synchronized void stopCapture()
    {
        capture = false;
    }

    public void setEnabledLevel(int level)
    {
        enabledLevel = level;
    }

    public boolean isLevelEnabled(int level)
    {
        return !suppress && level >= enabledLevel;
    }

    /**
     * Return the captured log messages to date.
     * @return log messages
     */
    public String getLog()
    {
        return log.toString();
    }

  private synchronized void log(int level, String msg, Throwable t)
  {
      if(!suppress && level >= enabledLevel)
      {
          PrintStream writer = capture ? systemDotIn : System.err;
          writer.print(getPrefix(enabledLevel));
          writer.println(msg);
          if (t != null)
          {
              writer.println(t.getMessage());
              t.printStackTrace(writer);
          }
          writer.flush();
      }
  }

  /**
   * Logging API
   */

  public boolean isTraceEnabled()
  {
      return isLevelEnabled(LOG_LEVEL_TRACE);
  }

  public void trace(String msg)
  {
      log(LOG_LEVEL_TRACE, msg, null);
  }

  public void trace(String format, Object arg)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      log(LOG_LEVEL_TRACE, ft.getMessage(), ft.getThrowable());
  }

  public void trace(String format, Object arg1, Object arg2)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      log(LOG_LEVEL_TRACE, ft.getMessage(), ft.getThrowable());
  }

  public void trace(String format, Object[] argArray)
  {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      log(LOG_LEVEL_TRACE, ft.getMessage(), ft.getThrowable());
  }

  public void trace(String msg, Throwable t)
  {
      log(LOG_LEVEL_TRACE, msg, t);
  }

  public boolean isDebugEnabled()
  {
      return isLevelEnabled(LOG_LEVEL_DEBUG);
  }

  public void debug(String msg)
  {
      log(LOG_LEVEL_DEBUG, msg, null);
  }

  public void debug(String format, Object arg)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      log(LOG_LEVEL_DEBUG, ft.getMessage(), ft.getThrowable());
  }

  public void debug(String format, Object arg1, Object arg2)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      log(LOG_LEVEL_DEBUG, ft.getMessage(), ft.getThrowable());
  }

  public void debug(String format, Object[] argArray)
  {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      log(LOG_LEVEL_DEBUG, ft.getMessage(), ft.getThrowable());
  }

  public void debug(String msg, Throwable t)
  {
      log(LOG_LEVEL_DEBUG, msg, t);
  }

  public boolean isInfoEnabled()
  {
      return isLevelEnabled(LOG_LEVEL_INFO);
  }

  public void info(String msg)
  {
      log(LOG_LEVEL_INFO, msg, null);
  }

  public void info(String format, Object arg)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      log(LOG_LEVEL_INFO, ft.getMessage(), ft.getThrowable());
  }

  public void info(String format, Object arg1, Object arg2)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      log(LOG_LEVEL_INFO, ft.getMessage(), ft.getThrowable());
  }

  public void info(String format, Object[] argArray)
  {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      log(LOG_LEVEL_INFO, ft.getMessage(), ft.getThrowable());
  }

  public void info(String msg, Throwable t)
  {
      log(LOG_LEVEL_INFO, msg, t);
  }

  public boolean isWarnEnabled()
  {
      return isLevelEnabled(LOG_LEVEL_WARN);
  }

  public void warn(String msg)
  {
      log(LOG_LEVEL_WARN, msg, null);
  }

  public void warn(String format, Object arg)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      log(LOG_LEVEL_WARN, ft.getMessage(), ft.getThrowable());
  }

  public void warn(String format, Object arg1, Object arg2)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      log(LOG_LEVEL_WARN, ft.getMessage(), ft.getThrowable());
  }

  public void warn(String format, Object[] argArray)
  {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      log(LOG_LEVEL_WARN, ft.getMessage(), ft.getThrowable());
  }

  public void warn(String msg, Throwable t)
  {
      log(LOG_LEVEL_WARN, msg, t);
  }

  public boolean isErrorEnabled()
  {
      return isLevelEnabled(LOG_LEVEL_ERROR);
  }

  public void error(String msg)
  {
      log(LOG_LEVEL_ERROR, msg, null);
  }

  public void error(String format, Object arg)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      log(LOG_LEVEL_ERROR, ft.getMessage(), ft.getThrowable());
  }

  public void error(String format, Object arg1, Object arg2)
  {
      FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
      log(LOG_LEVEL_ERROR, ft.getMessage(), ft.getThrowable());
  }

  public void error(String format, Object[] argArray)
  {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      log(LOG_LEVEL_ERROR, ft.getMessage(), ft.getThrowable());
  }

  public void error(String msg, Throwable t)
  {
      log(LOG_LEVEL_ERROR, msg, t);
  }
}
