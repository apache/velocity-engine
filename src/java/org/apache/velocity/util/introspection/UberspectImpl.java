package org.apache.velocity.util.introspection;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.log.RuntimeLoggerLog;
import org.apache.velocity.runtime.RuntimeLogger;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.BooleanPropertyExecutor;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.apache.velocity.runtime.parser.node.PropertyExecutor;
import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.util.EnumerationIterator;

/**
 *  Implementation of Uberspect to provide the default introspective
 *  functionality of Velocity
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class UberspectImpl implements Uberspect, UberspectLoggable
{
    /**
     *  Our runtime logger.
     */
    private Log log;

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
            log.debug("The iterative object in the #foreach() loop at " +
                       i + " is of type java.util.Iterator.  Because " +
                       "it is not resettable, if used in more than once it " +
                       "may lead to unexpected results.");

            return ((Iterator) obj);
        }
        else if (obj instanceof Enumeration)
        {
            log.debug("The iterative object in the #foreach() loop at " +
                       i + " is of type java.util.Enumeration.  Because " +
                       "it is not resettable, if used in more than once it " +
                       "may lead to unexpected results.");

            return new EnumerationIterator((Enumeration) obj);
        }

        /*  we have no clue what this is  */
        log.warn("Could not determine type of iterator in " +
                  "#foreach loop at " + i);

        return null;
    }

    /**
     *  Method
     */
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
            throws Exception
    {
        if (obj == null)
            return null;

        Method m = introspector.getMethod(obj.getClass(), methodName, args);

        return (m != null) ? new VelMethodImpl(m) : null;
    }

    /**
     * Property  getter
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception
    {
        AbstractExecutor executor;

        Class claz = obj.getClass();

        /*
         *  first try for a getFoo() type of property
         *  (also getfoo() )
         */

        executor = new PropertyExecutor(log,introspector, claz, identifier);

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
     */
    public VelPropertySet getPropertySet(Object obj, String identifier,
                                         Object arg, Info i)
            throws Exception
    {
        Class claz = obj.getClass();

        VelMethod vm = null;
        try
        {
            /*
             *  first, we introspect for the set<identifier> setter method
             */

            Object[] params = {arg};

            try
            {
                vm = getMethod(obj, "set" + identifier, params, i);

                if (vm == null)
                {
                   throw new NoSuchMethodException();
                }
            }
            catch(NoSuchMethodException nsme2)
            {
                StringBuffer sb = new StringBuffer("set");
                sb.append(identifier);

                if (Character.isLowerCase( sb.charAt(3)))
                {
                    sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
                }
                else
                {
                    sb.setCharAt(3, Character.toLowerCase(sb.charAt(3)));
                }

                vm = getMethod(obj, sb.toString(), params, i);

                if (vm == null)
                {
                   throw new NoSuchMethodException();
                }
            }
        }
        catch (NoSuchMethodException nsme)
        {
            /*
             *  right now, we only support the Map interface
             */

            if (Map.class.isAssignableFrom(claz))
            {
                Object[] params = {new Object(), new Object()};

                vm = getMethod(obj, "put", params, i);

                if (vm!=null)
                    return new VelSetterImpl(vm, identifier);
            }
       }

       return (vm!=null) ?  new VelSetterImpl(vm) : null;
    }

    /**
     *  Implementation of VelMethod
     */
    public static class VelMethodImpl implements VelMethod
    {
        Method method = null;

        public VelMethodImpl(Method m)
        {
            method = m;
        }

        private VelMethodImpl()
        {
        }

        public Object invoke(Object o, Object[] params)
            throws Exception
        {
            return method.invoke(o, params);
        }

        public boolean isCacheable()
        {
            return true;
        }

        public String getMethodName()
        {
            return method.getName();
        }

        public Class getReturnType()
        {
            return method.getReturnType();
        }
    }

    public static class VelGetterImpl implements VelPropertyGet
    {
        AbstractExecutor ae = null;

        public VelGetterImpl(AbstractExecutor exec)
        {
            ae = exec;
        }

        private VelGetterImpl()
        {
        }

        public Object invoke(Object o)
            throws Exception
        {
            return ae.execute(o);
        }

        public boolean isCacheable()
        {
            return true;
        }

        public String getMethodName()
        {
            return ae.getMethod().getName();
        }
    }

    public static class VelSetterImpl implements VelPropertySet
    {
        VelMethod vm = null;
        String putKey = null;

        public VelSetterImpl(VelMethod velmethod)
        {
            this.vm = velmethod;
        }

        public VelSetterImpl(VelMethod velmethod, String key)
        {
            this.vm = velmethod;
            putKey = key;
        }

        private VelSetterImpl()
        {
        }

        public Object invoke(Object o, Object value)
            throws Exception
        {
            ArrayList al = new ArrayList();

            if (putKey != null)
            {
                al.add(putKey);
                al.add(value);
            }
            else
            {
                al.add(value);
            }

            return vm.invoke(o,al.toArray());
        }

        public boolean isCacheable()
        {
            return true;
        }

        public String getMethodName()
        {
            return vm.getMethodName();
        }
    }
}
