package org.apache.velocity.exception;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 *  Application-level exception thrown when a reference method is 
 *  invoked and an exception is thrown.
 *  <br>
 *  When this exception is thrown, a best effort will be made to have
 *  useful information in the exception's message.  For complete 
 *  information, consult the runtime log.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodInvocationException.java,v 1.2.14.1 2004/03/03 23:22:54 geirm Exp $
 */
public class MethodInvocationException extends VelocityException
{
    private String methodName = "";
    private String referenceName = "";
    private Throwable wrapped = null;

    /**
     *  CTOR - wraps the passed in exception for
     *  examination later
     *
     *  @param message 
     *  @param e Throwable that we are wrapping
     *  @param methodName name of method that threw the exception
     */
    public MethodInvocationException( String message, Throwable e, String methodName )
    {
        super(message);
        this.wrapped = e;
        this.methodName = methodName;
    }       

    /**
     *  Returns the name of the method that threw the
     *  exception
     *
     *  @return String name of method
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     *  returns the wrapped Throwable that caused this
     *  MethodInvocationException to be thrown
     *  
     *  @return Throwable thrown by method invocation
     */
    public Throwable getWrappedThrowable()
    {
        return wrapped;
    }

    /**
     *  Sets the reference name that threw this exception
     *
     *  @param reference name of reference
     */
    public void setReferenceName( String ref )
    {
        referenceName = ref;
    }

    /**
     *  Retrieves the name of the reference that caused the 
     *  exception
     *
     *  @return name of reference
     */
    public String getReferenceName()
    {
        return referenceName;
    }
}
