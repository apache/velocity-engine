package org.apache.velocity.app;

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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 *  <p>
 *  This is a small utility class allow easy access to static fields in a class,
 *  such as string constants.  Velocity will not introspect for class
 *  fields (and won't in the future :), but writing setter/getter methods to do 
 *  this really is a pain,  so use this if you really have
 *  to access fields.
 *  
 *  <p>
 *  The idea it so enable access to the fields just like you would in Java.  
 *  For example, in Java, you would access a static field like
 *  <blockquote><pre>
 *  MyClass.STRING_CONSTANT
 *  </pre></blockquote>
 *  and that is the same thing we are trying to allow here.
 *
 *  <p>
 *  So to use in your Java code, do something like this :
 *  <blockquote><pre>
 *   context.put("runtime", new FieldMethodizer( "org.apache.velocity.runtime.Runtime" ));
 *  </pre></blockquote>
 *  and then in your template, you can access any of your static fields in this way :
 *  <blockquote><pre>  
 *   $runtime.RUNTIME_LOG_WARN_STACKTRACE
 *  </pre></blockquote>
 *
 *  <p>
 *  Right now, this class only methodizes <code>public static</code> fields.  It seems
 *  that anything else is too dangerous.  This class is for convenience accessing
 *  'constants'.  If you have fields that aren't <code>static</code> it may be better
 *  to handle them by explicitly placing them into the context.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: FieldMethodizer.java,v 1.3 2001/03/05 11:44:48 jvanzyl Exp $ 
 */
public class FieldMethodizer
{
    /** Hold the field objects by field name */
    private HashMap fieldHash = new HashMap();

    /** Hold the class objects by field name */
    private HashMap classHash = new HashMap();

    /**
     * Allow object to be initialized without any data. You would use
     * addObject() to add data later.
     */
    public FieldMethodizer()
    {
    }

    /**
     *  Constructor that takes as it's arg the name of the class
     *  to methodize.
     *
     *  @param s Name of class to methodize.
     */
    public FieldMethodizer( String s )
    {
        try
        {
            addObject(s);
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
    }

  /**
     *  Constructor that takes as it's arg a living
     *  object to methodize.  Note that it will still
     *  only methodized the public static fields of
     *  the class.
     *
     *  @param s Name of class to methodize.
     */
    public FieldMethodizer( Object o )
    {
        try
        {
            addObject(o);
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
    }
    
    /**
     * Add the Name of the class to methodize
     */
    public void addObject ( String s )
        throws Exception
    {
        inspect(Class.forName(s));
    }
    
    /**
     * Add an Object to methodize
     */
    public void addObject ( Object o )
        throws Exception
    {
        inspect(o.getClass());
    }

    /**
     *  Accessor method to get the fields by name.
     *
     *  @param fieldName Name of static field to retrieve
     *
     *  @return The value of the given field.
     */
    public Object get( String fieldName )
    {
        try 
        {
            Field f = (Field) fieldHash.get( fieldName );
            if (f != null)
                return f.get( (Class) classHash.get(fieldName) );
        }
        catch( Exception e )
        {
        }
        return null;
    }

    /**
     *  Method that retrieves all public static fields
     *  in the class we are methodizing.
     */
    private void inspect(Class clas)
    {
        Field[] fields = clas.getFields();
        for( int i = 0; i < fields.length; i++)
        {
            /*
             *  only if public and static
             */
            int mod = fields[i].getModifiers();
            if ( Modifier.isStatic(mod) && Modifier.isPublic(mod) )
            {
                fieldHash.put(fields[i].getName(), fields[i]);
                classHash.put(fields[i].getName(), clas);
            }
        }
    }
}
