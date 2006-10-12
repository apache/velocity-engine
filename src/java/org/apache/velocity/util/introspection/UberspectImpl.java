package org.apache.velocity.util.introspection;

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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.apache.velocity.runtime.RuntimeLogger;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.log.RuntimeLoggerLog;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.BooleanPropertyExecutor;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.apache.velocity.runtime.parser.node.MapGetExecutor;
import org.apache.velocity.runtime.parser.node.MapSetExecutor;
import org.apache.velocity.runtime.parser.node.PropertyExecutor;
import org.apache.velocity.runtime.parser.node.PutExecutor;
import org.apache.velocity.runtime.parser.node.SetExecutor;
import org.apache.velocity.runtime.parser.node.SetPropertyExecutor;
import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.util.EnumerationIterator;

/**
 *  Implementation of Uberspect to provide the default introspective
 *  functionality of Velocity
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class UberspectImpl implements Uberspect, UberspectLoggable
{
    /**
     *  Our runtime logger.
     */
    protected Log log;

    /**
     *  the default Velocity introspector
     */
    protected Introspector introspector;

    /**
     *  init - generates the Introspector. As the setup code
     *  makes sure that the log gets set before this is called,
     *  we can initialize the Introspector using the log object.
     */
    public void init()
    {
        introspector = new Introspector(log);
    }

    /**
     *  Sets the runtime logger - this must be called before anything
     *  else.
     *
     * @param log The logger instance to use.
     */
    public void setLog(Log log)
    {
        this.log = log;
    }

    /**
     * @param runtimeLogger
     * @deprecated Use setLog(Log log) instead.
     */
    public void setRuntimeLogger(RuntimeLogger runtimeLogger)
    {
        // in the off chance anyone still uses this method
        // directly, use this hack to keep it working
        setLog(new RuntimeLoggerLog(runtimeLogger));
    }

    /**
     *  To support iterative objects used in a <code>#foreach()</code>
     *  loop.
     *
     * @param obj The iterative object.
     * @param i Info about the object's location.
     * @return An {@link Iterator} object.
     * @throws Exception
     */
    public Iterator getIterator(Object obj, Info i)
            throws Exception
    {
        if (obj.getClass().isArray())
        {
            return new ArrayIterator(obj);
        }
        else if (obj instanceof Collection)
        {
            return ((Collection) obj).iterator();
        }
        else if (obj instanceof Map)
        {
            return ((Map) obj).values().iterator();
        }
        else if (obj instanceof Iterator)
        {
            if (log.isDebugEnabled())
            {
                log.debug("The iterative object in the #foreach() loop at " +
                           i + " is of type java.util.Iterator.  Because " +
                           "it is not resettable, if used in more than once it " +
                           "may lead to unexpected results.");
            }
            return ((Iterator) obj);
        }
        else if (obj instanceof Enumeration)
        {
            if (log.isDebugEnabled())
            {
                log.debug("The iterative object in the #foreach() loop at " +
                           i + " is of type java.util.Enumeration.  Because " +
                           "it is not resettable, if used in more than once it " +
                           "may lead to unexpected results.");
            }
            return new EnumerationIterator((Enumeration) obj);
        }

        /*  we have no clue what this is  */
        log.info("Could not determine type of iterator in #foreach loop at " + i);

        return null;
    }

    /**
     *  Method
     * @param obj
     * @param methodName
     * @param args
     * @param i
     * @return A Velocity Method.
     * @throws Exception
     */
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
            throws Exception
    {
        if (obj == null)
        {
            return null;
        }

        Method m = introspector.getMethod(obj.getClass(), methodName, args);

        return (m != null) ? new VelMethodImpl(m) : null;
    }

    /**
     * Property  getter
     * @param obj
     * @param identifier
     * @param i
     * @return A Velocity Getter Method.
     * @throws Exception
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception
    {
        if (obj == null)
        {
            return null;
        }

        Class claz = obj.getClass();

        /*
         *  first try for a getFoo() type of property
         *  (also getfoo() )
         */
        AbstractExecutor executor = new PropertyExecutor(log, introspector, claz, identifier);

        /*
         * Let's see if we are a map...
         */
        if (!executor.isAlive()) 
        {
            executor = new MapGetExecutor(log, claz, identifier);
        }

        /*
         *  if that didn't work, look for get("foo")
         */

        if (!executor.isAlive())
        {
            executor = new GetExecutor(log, introspector, claz, identifier);
        }

        /*
         *  finally, look for boolean isFoo()
         */

        if (!executor.isAlive())
        {
            executor = new BooleanPropertyExecutor(log, introspector, claz,
                                                   identifier);
        }

        return (executor.isAlive()) ? new VelGetterImpl(executor) : null;
    }

    /**
     * Property setter
     * @param obj
     * @param identifier
     * @param arg
     * @param i
     * @return A Velocity Setter method.
     * @throws Exception
     */
    public VelPropertySet getPropertySet(Object obj, String identifier,
                                         Object arg, Info i)
            throws Exception
    {
        if (obj == null)
        {
            return null;
        }

        Class claz = obj.getClass();

        /*
         *  first try for a setFoo() type of property
         *  (also setfoo() )
         */
        SetExecutor executor = new SetPropertyExecutor(log, introspector, claz, identifier, arg);

        /*
         * Let's see if we are a map...
         */
        if (!executor.isAlive())  {
            executor = new MapSetExecutor(log, claz, identifier);
        }

        /*
         *  if that didn't work, look for put("foo", arg)
         */

        if (!executor.isAlive())
        {
            executor = new PutExecutor(log, introspector, claz, arg, identifier);
        }

        return (executor.isAlive()) ? new VelSetterImpl(executor) : null;
    }

    /**
     *  Implementation of VelMethod
     */
    public static class VelMethodImpl implements VelMethod
    {
        final Method method;

        /**
         * @param m
         */
        public VelMethodImpl(Method m)
        {
            method = m;
        }

        private VelMethodImpl()
        {
            method = null;
        }

        /**
         * @see VelMethod#invoke(java.lang.Object, java.lang.Object[])
         */
        public Object invoke(Object o, Object[] params)
            throws Exception
        {
            return method.invoke(o, params);
        }

        /**
         * @see org.apache.velocity.util.introspection.VelMethod#isCacheable()
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * @see org.apache.velocity.util.introspection.VelMethod#getMethodName()
         */
        public String getMethodName()
        {
            return method.getName();
        }

        /**
         * @see org.apache.velocity.util.introspection.VelMethod#getReturnType()
         */
        public Class getReturnType()
        {
            return method.getReturnType();
        }
    }

    /**
     *
     *
     */
    public static class VelGetterImpl implements VelPropertyGet
    {
        final AbstractExecutor getExecutor;

        /**
         * @param exec
         */
        public VelGetterImpl(AbstractExecutor exec)
        {
            getExecutor = exec;
        }

        private VelGetterImpl()
        {
            getExecutor = null;
        }

        /**
         * @see org.apache.velocity.util.introspection.VelPropertyGet#invoke(java.lang.Object)
         */
        public Object invoke(Object o)
            throws Exception
        {
            return getExecutor.execute(o);
        }

        /**
         * @see org.apache.velocity.util.introspection.VelPropertyGet#isCacheable()
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * @see org.apache.velocity.util.introspection.VelPropertyGet#getMethodName()
         */
        public String getMethodName()
        {
            return getExecutor.isAlive() ? getExecutor.getMethod().getName() : null;
        }
    }

    /**
     *
     */
    public static class VelSetterImpl implements VelPropertySet
    {
        private final SetExecutor setExecutor;

        /**
         * @param setExecutor
         */
        public VelSetterImpl(final SetExecutor setExecutor)
        {
            this.setExecutor = setExecutor;
        }

        private VelSetterImpl()
        {
            setExecutor = null;
        }

        /**
         * Invoke the found Set Executor.
         *
         * @param o is the Object to invoke it on.
         * @param value in the Value to set.
         * @return The resulting Object.
         * @throws Exception
         */
        public Object invoke(final Object o, final Object value)
            throws Exception
        {
            return setExecutor.execute(o, value);
        }

        /**
         * @see org.apache.velocity.util.introspection.VelPropertySet#isCacheable()
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * @see org.apache.velocity.util.introspection.VelPropertySet#getMethodName()
         */
        public String getMethodName()
        {
            return setExecutor.isAlive() ? setExecutor.getMethod().getName() : null;
        }
    }
}
