package org.apache.velocity.util.introspection;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 */

import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.util.EnumerationIterator;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeLogger;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.PropertyExecutor;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.apache.velocity.runtime.parser.node.BooleanPropertyExecutor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.Enumeration;
import java.util.ArrayList;

/**
 *  Implementation of Uberspect to provide the default introspective
 *  functionality of Velocity
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: UberspectImpl.java,v 1.1 2002/04/21 18:41:21 geirm Exp $
 */
public class UberspectImpl implements Uberspect, UberspectLoggable
{
    /**
     *  Our runtime logger.
     */
    private RuntimeLogger rlog;

    /**
     *  the default Velocity introspector
     */
    private static Introspector introspector;

    /**
     *  init - does nothing - we need to have setRuntimeLogger
     *  called before getting our introspector, as the default
     *  vel introspector depends upon it.
     */
    public void init()
        throws Exception
    {
    }

    /**
     *  Sets the runtime logger - this must be called before anything
     *  else besides init() as to get the logger.  Makes the pull
     *  model appealing...
     */
    public void setRuntimeLogger(RuntimeLogger runtimeLogger)
    {
        rlog = runtimeLogger;
        introspector = new Introspector(rlog);
    }

    /**
     *  To support iteratives - #foreach()
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
            rlog.warn ("Warning! The iterative "
                          + " is an Iterator in the #foreach() loop at ["
                          + i.getLine() + "," + i.getColumn() + "]"
                          + " in template " + i.getTemplateName()
                          + ". Because it's not resetable,"
                          + " if used in more than once, this may lead to"
                          + " unexpected results.");

            return ((Iterator) obj);
        }
        else if (obj instanceof Enumeration)
        {
            rlog.warn ("Warning! The iterative "
                          + " is an Enumeration in the #foreach() loop at ["
                          + i.getLine() + "," + i.getColumn() + "]"
                          + " in template " + i.getTemplateName()
                          + ". Because it's not resetable,"
                          + " if used in more than once, this may lead to"
                          + " unexpected results.");

            return new EnumerationIterator((Enumeration) obj);
        }

        /*  we have no clue what this is  */
        rlog.warn ("Could not determine type of iterator in "
                      +  "#foreach loop "
                      + " at [" + i.getLine() + "," + i.getColumn() + "]"
                      + " in template " + i.getTemplateName() );

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

        executor = new PropertyExecutor(rlog,introspector, claz, identifier);

        /*
         *  if that didn't work, look for get("foo")
         */

        if (executor.isAlive() == false)
        {
            executor = new GetExecutor(rlog, introspector, claz, identifier);
        }

        /*
         *  finally, look for boolean isFoo()
         */

        if( executor.isAlive() == false)
        {
            executor = new BooleanPropertyExecutor(rlog, introspector, claz, identifier);
        }

        return (executor != null) ? new VelGetterImpl(executor) : null;
    }

    /**
     * Property setter
     */
    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info i)
            throws Exception
    {
        Class claz = obj.getClass();

        VelPropertySet vs = null;
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
    public class VelMethodImpl implements VelMethod
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
    }

    public class VelGetterImpl implements VelPropertyGet
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

    public class VelSetterImpl implements VelPropertySet
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
