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
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;


/**
 * A introspector that allows testing when methods are not found.
 */
public class UberspectTestImpl extends UberspectImpl
{

    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        VelMethod method = super.getMethod(obj, methodName, args, i);

        if (method == null)
        {
            if (obj == null)
                throw new UberspectTestException("Can't call method '" + methodName + "' on null object",i);
            else
                throw new UberspectTestException("Did not find method "+ obj.getClass().getName()+"."+methodName, i);
        }

        return method;
    }

    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        VelPropertyGet propertyGet = super.getPropertyGet(obj, identifier, i);

        if (propertyGet == null)
        {
            if (obj == null)
                throw new UberspectTestException("Can't call getter '" + identifier + "' on null object",i);
            else
                throw new UberspectTestException("Did not find "+ obj.getClass().getName()+"."+identifier, i);
        }

        return propertyGet;
    }

}
