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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.SystemLogChute;

/**
 * LogChute implementation that can easily capture output
 * or suppress it entirely.  By default, both capture and suppress
 * are on. To have this behave like a normal SystemLogChute,
 * you must turn it on() and stopCapture().
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author Nathan Bubna
 * @version $Id$
 */
public class TestLogChute extends SystemLogChute
{
    public static final String TEST_LOGGER_LEVEL = "runtime.log.logsystem.test.level";

    private ByteArrayOutputStream log;
    private PrintStream systemDotIn;
    private boolean suppress = true;
    private boolean capture = true;

    public TestLogChute()
    {
        log = new ByteArrayOutputStream();
        systemDotIn = new PrintStream(log, true);
    }

    public TestLogChute(boolean suppress, boolean capture)
    {
        this();
        this.suppress = suppress;
        this.capture = capture;
    }
    
    public void init(RuntimeServices rs) throws Exception
    {
        super.init(rs);

        String level = rs.getString(TEST_LOGGER_LEVEL, "debug");
        setEnabledLevel(toLevel(level));
    }

    public void on()
    {
        suppress = false;
    }

    public void off()
    {
        suppress = true;
    }

    public void startCapture()
    {
        capture = true;
    }

    public void stopCapture()
    {
        capture = false;
    }

    public boolean isLevelEnabled(int level)
    {
        return !suppress && super.isLevelEnabled(level);
    }
            

    protected void write(PrintStream ps, String prefix, String message, Throwable t)
    {
        if (capture)
        {
            super.write(systemDotIn, prefix, message, t);
        }
        else
        {
            super.write(ps, prefix, message, t);
        }
    }

    /**
     * Return the captured log messages to date.
     * @return log messages
     */
    public String getLog()
    {
        return log.toString();
    }

}
