package org.apache.velocity.runtime;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

public interface RuntimeConstants
{
    /** Prefix for warning messages */
    final static String WARN  = "  [warn] ";
    
    /** Prefix for info messages */
    final static String INFO  = "  [info] ";
    
    /**  Prefix for debug messages */
    final static String DEBUG = " [debug] ";
    
    /** Prefix for error messages */
    final static String ERROR = " [error] ";

    /** Turn Runtime debugging on with this field */
    final static boolean DEBUG_ON = true;

    /** Default Runtime properties */
    final static String DEFAULT_RUNTIME_PROPERTIES = 
        "org/apache/velocity/runtime/defaults/velocity.properties";

    /** Default Runtime properties */
    final static String DEFAULT_RUNTIME_DIRECTIVES = 
        "org/apache/velocity/runtime/defaults/directive.properties";

    /**
      * Number of parsers to create
      */
    final static int NUMBER_OF_PARSERS = 20;

    /** Location of the log file */
    public static final String RUNTIME_LOG = "runtime.log";
    
   /**  stack trace output */
    public static final String RUNTIME_LOG_ERROR_STACKTRACE = "runtime.log.error.stacktrace";
    public static final String RUNTIME_LOG_WARN_STACKTRACE = "runtime.log.warn.stacktrace";
    public static final String RUNTIME_LOG_INFO_STACKTRACE = "runtime.log.info.stacktrace";

    /** The encoding to use for the template */
    public static final String TEMPLATE_ENCODING = "template.encoding";
    
    /** Initial counter value in #foreach directives */
    public static final String COUNTER_NAME = "counter.name";

    /** Initial counter value in #foreach directives */
    public static final String COUNTER_INITIAL_VALUE = "counter.initial.value";

    /** Content type */
    public static final String DEFAULT_CONTENT_TYPE = "default.contentType";

    /**
     * The public handle for setting the base path of the standard
     * FileResourceLoader.
     */
    public static final String FILE_RESOURCE_LOADER_PATH = "file.resource.path";
    
    /**
     * The public handle for turning the caching on in the
     * FileResourceLoader.
     */
    public static final String FILE_RESOURCE_LOADER_CACHING = "file.cache";

    public static final String RUNTIME_LOG_REFERENCE_LOG_INVALID  = 
        "runtime.log.reference.log_invalid";

    /**
     * Starting tag for error messages triggered by passing
     * a parameter not allowed in the #include directive. Only
     * string literals, and references are allowed.
     */
    public static String ERRORMSG_START =  "include.output.errormsg.start";
    
    /**
     * Ending tag for error messages triggered by passing
     * a parameter not allowed in the #include directive. Only
     * string literals, and references are allowed.
     */
    public static String ERRORMSG_END  = "include.output.errormsg.end";

    /**
     * Maximum recursion depth allowed for the #parse directive.
     */
    public static String PARSE_DIRECTIVE_MAXDEPTH = "parse_directive.maxdepth";

    /**
     *  Switch for the interpolation facility for string literals
     */
    public static String INTERPOLATE_STRINGLITERALS = "stringliterals.interpolate";

    /** name of global Velocimacro library template */
    public static final String VM_GLOBAL_LIBRARY = "velocimacro.library.global";

    /** name of local Velocimacro library template */
    public static final String VM_LOCAL_LIBRARY  = "velocimacro.library.local";

    /** boolean (true/false) default true : allow inline (in-template) macro definitions */
    public static final String VM_PERM_ALLOW_INLINE  = "velocimacro.permissions.allowInline";

    /** boolean (true/false) default false : allow inline (in-template) macro definitions to replace existing */
    public final static String VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL  = "velocimacro.permissions.allowInlineToReplaceGlobal";
    
    /** switch for forcing inline macros to be local : default false */
    public final static String VM_PERM_INLINE_LOCAL = "velocimacro.permissions.allowInlineLocalScope";

    /** switch for VM blather : default true  */
    public final static String VM_MESSAGES_ON = "velocimacro.messages.on";
}
