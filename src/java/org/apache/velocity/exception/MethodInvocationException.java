package org.apache.velocity.exception;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

/**
 *  Application-level exception thrown when a reference method is 
 *  invoked and an exception is thrown.
 *  <br>
 *  When this exception is thrown, a best effort will be made to have
 *  useful information in the exception's message.  For complete 
 *  information, consult the runtime log.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodInvocationException.java,v 1.2 2001/03/27 02:06:40 geirm Exp $
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
