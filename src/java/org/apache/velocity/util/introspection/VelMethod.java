package org.apache.velocity.util.introspection;

/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  Method used for regular method invocation
 *
 *    $foo.bar()
 *
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelMethod.java,v 1.2.4.1 2004/03/03 23:23:08 geirm Exp $
 */
public interface VelMethod
{
    /**
     *  invocation method - called when the method invocationshould be
     *  preformed and a value returned
     */
    public Object invoke(Object o, Object[] params)
        throws Exception;

    /**
     *  specifies if this VelMethod is cacheable and able to be
     *  reused for this class of object it was returned for
     *
     *  @return true if can be reused for this class, false if not
     */
    public boolean isCacheable();

    /**
     *  returns the method name used
     */
    public String getMethodName();

    /**
     *  returns the return type of the method invoked
     */
    public Class getReturnType();
}
