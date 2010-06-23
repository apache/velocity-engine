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

import org.apache.velocity.util.introspection.Info;



/**
 * Exception that returns an Info object for testing after a introspection problem.
 * This extends Error so that it will stop parsing and allow
 * internal info to be examined.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:isidore@setgame.com">Llewellyn Falco</a>
 * @version $Id$
 */
public class UberspectTestException extends RuntimeException
{

    /**
     * Version Id for serializable
     */
    private static final long serialVersionUID = 3956896150436225712L;

    Info info;

    public UberspectTestException(String message, Info i)
    {
        super(message);
        info = i;
    }

    public Info getInfo()
    {
        return info;
    }

    public String getMessage()
    {
      return super.getMessage() + "\n failed at " + info;
    }

}
