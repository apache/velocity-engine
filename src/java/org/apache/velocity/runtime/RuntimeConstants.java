package org.apache.velocity.runtime;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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
 * This class defines the keys that are used in the 
 * velocity.properties file so that they can be referenced as a constant
 * within Java code.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: RuntimeConstants.java,v 1.33.4.1 2004/03/03 23:22:55 geirm Exp $
 */
public interface RuntimeConstants
{
    /*
     * ----------------------------------------------------------------------
     * These are public constants that are used as handles for the
     * properties that can be specified in your typical
     * velocity.properties file.
     * ----------------------------------------------------------------------
     */

    /*
     * ----------------------------------------------------------------------
     * L O G G I N G  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** 
     * Location of the velocity log file.
     */
    public static final String RUNTIME_LOG = 
        "runtime.log";
    
    /**
     *  externally provided logger
     */
    public static final String RUNTIME_LOG_LOGSYSTEM = 
        "runtime.log.logsystem";

    /**
     *  class of log system to use
     */
    public static final String RUNTIME_LOG_LOGSYSTEM_CLASS = 
        "runtime.log.logsystem.class";

    /** 
     * Stack trace output for error messages.
     */
    public static final String RUNTIME_LOG_ERROR_STACKTRACE = 
        "runtime.log.error.stacktrace";
    
    /** 
     * Stack trace output for warning messages.
     */
    public static final String RUNTIME_LOG_WARN_STACKTRACE = 
        "runtime.log.warn.stacktrace";
    
    /** 
     * Stack trace output for info messages.
     */
    public static final String RUNTIME_LOG_INFO_STACKTRACE = 
        "runtime.log.info.stacktrace";

    /**
     * Logging of invalid references.
     */
    public static final String RUNTIME_LOG_REFERENCE_LOG_INVALID  = 
        "runtime.log.invalid.references";

    /**
     *  Log message prefixes
     */
    public final static String DEBUG_PREFIX = " [debug] ";
    public final static String INFO_PREFIX  = "  [info] ";
    public final static String WARN_PREFIX  = "  [warn] ";
    public final static String ERROR_PREFIX = " [error] ";
    public final static String UNKNOWN_PREFIX = " [unknown] ";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_PATTERN = 
        "runtime.log.logsystem.log4j.pattern";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_FILE_SIZE = 
        "runtime.log.logsystem.log4j.file.size";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_FILE_BACKUPS = 
        "runtime.log.logsystem.log4j.file.backups";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_SYSLOGD_HOST = 
        "runtime.log.logsystem.log4j.syslogd.host";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_SYSLOGD_FACILITY = 
        "runtime.log.logsystem.log4j.syslogd.facility";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_REMOTE_HOST = 
        "runtime.log.logsystem.log4j.remote.host";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_REMOTE_PORT = 
        "runtime.log.logsystem.log4j.remote.port";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_EMAIL_SERVER = 
        "runtime.log.logsystem.log4j.email.server";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_EMAIL_FROM = 
        "runtime.log.logsystem.log4j.email.from";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_EMAIL_TO = 
        "runtime.log.logsystem.log4j.email.to";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_EMAIL_SUBJECT = 
        "runtime.log.logsystem.log4j.email.subject";

    /**
     * Log4J configuration
     */
    public final static String LOGSYSTEM_LOG4J_EMAIL_BUFFER_SIZE = 
        "runtime.log.logsystem.log4j.email.buffer.size";

    /*
     * ----------------------------------------------------------------------
     * D I R E C T I V E  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     * Directive properties are of the form:
     * 
     * directive.<directive-name>.<property>
     * ----------------------------------------------------------------------
     */

    /** 
     * Initial counter value in #foreach directives.
     */
    public static final String COUNTER_NAME = 
        "directive.foreach.counter.name";

    /** 
     * Initial counter value in #foreach directives.
     */
    public static final String COUNTER_INITIAL_VALUE = 
        "directive.foreach.counter.initial.value";

    /**
     * Starting tag for error messages triggered by passing
     * a parameter not allowed in the #include directive. Only
     * string literals, and references are allowed.
     */
    public static String ERRORMSG_START = 
        "directive.include.output.errormsg.start";
    
    /**
     * Ending tag for error messages triggered by passing
     * a parameter not allowed in the #include directive. Only
     * string literals, and references are allowed.
     */
    public static String ERRORMSG_END  = 
        "directive.include.output.errormsg.end";

    /**
     * Maximum recursion depth allowed for the #parse directive.
     */
    public static String PARSE_DIRECTIVE_MAXDEPTH 
        = "directive.parse.max.depth";


    /*
     * ----------------------------------------------------------------------
     *  R E S O U R C E   M A N A G E R   C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */
     
     public static String RESOURCE_MANAGER_CLASS = "resource.manager.class";
     
     public static String RESOURCE_MANAGER_CACHE_CLASS =
                "resource.manager.cache.class";
                
    /*
     * ----------------------------------------------------------------------
     * R E S O U R C E  L O A D E R  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /**
     *  controls if the finding of a resource is logged
     */
    public static final String RESOURCE_MANAGER_LOGWHENFOUND = 
        "resource.manager.logwhenfound";

    /**
     * Key used to retrieve the names of the resource loaders
     * to be used. In a properties file they may appear as
     * the following:
     *
     * resource.loader = file,classpath
     */
    public static final String RESOURCE_LOADER = "resource.loader";

    /**
     * The public handle for setting a path in
     * the FileResourceLoader.
     */
    public static final String FILE_RESOURCE_LOADER_PATH =
        "file.resource.loader.path";

    /**
     * The public handle for turning the caching on in the
     * FileResourceLoader.
     */
    public static final String FILE_RESOURCE_LOADER_CACHE = 
        "file.resource.loader.cache";

    /*
     * ----------------------------------------------------------------------
     * V E L O C I M A C R O  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** 
     * Name of local Velocimacro library template.
     */
    public static final String VM_LIBRARY  = "velocimacro.library";

    /** 
     * switch for autoloading library-sourced VMs (for development) 
     */
    public final static String VM_LIBRARY_AUTORELOAD = 
        "velocimacro.library.autoreload";

    /** 
     * boolean (true/false) default true : allow 
     * inline (in-template) macro definitions 
     */
    public static final String VM_PERM_ALLOW_INLINE  = 
        "velocimacro.permissions.allow.inline";

    /**
     * boolean (true/false) default false : allow inline 
     * (in-template) macro definitions to replace existing 
     */
    public final static String VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL  = 
        "velocimacro.permissions.allow.inline.to.replace.global";
    
    /** 
     * Switch for forcing inline macros to be local : default false.
     */
    public final static String VM_PERM_INLINE_LOCAL = 
        "velocimacro.permissions.allow.inline.local.scope";

    /** 
     * Switch for VM blather : default true.
     */
    public final static String VM_MESSAGES_ON = "velocimacro.messages.on";

   /** 
    * switch for local context in VM : default false 
    */
    public final static String VM_CONTEXT_LOCALSCOPE = 
        "velocimacro.context.localscope";
    
    /*
     * ----------------------------------------------------------------------
     * G E N E R A L  R U N T I M E  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /**
     *  Switch for the interpolation facility for string literals
     */
    public static String INTERPOLATE_STRINGLITERALS = 
        "runtime.interpolate.string.literals";

    /** 
     * The character encoding for the templates.  Used by the parser in 
     * processing the input streams.
     */
    public static final String INPUT_ENCODING = "input.encoding";

    /**
     * Encoding for the output stream.  Currently used by Anakia and
     * VelocityServlet
     */
    public static final String OUTPUT_ENCODING = "output.encoding";

    public static final String ENCODING_DEFAULT = "ISO-8859-1";

    /*
     * ----------------------------------------------------------------------
     * These constants are used internally by the Velocity runtime i.e.
     * the constansts listed below are strictly used in the Runtime
     * class itself.
     * ----------------------------------------------------------------------
     */

    /** 
     * Default Runtime properties.
     */
    final static String DEFAULT_RUNTIME_PROPERTIES = 
        "org/apache/velocity/runtime/defaults/velocity.properties";

    /** 
     * Default Runtime properties 
     */
    final static String DEFAULT_RUNTIME_DIRECTIVES = 
        "org/apache/velocity/runtime/defaults/directive.properties";

    /**
     * The default number of parser instances to create.  Configurable
     * via the parameter named by the {@link #PARSER_POOL_SIZE}
     * constant.
     */
    final static int NUMBER_OF_PARSERS = 20;

    /**
     * @see #NUMBER_OF_PARSERS
     */
    final static String PARSER_POOL_SIZE = "parser.pool.size";


    /**
     *  key name for uberspector
     */
    final static String UBERSPECT_CLASSNAME = "runtime.introspector.uberspect";
}
