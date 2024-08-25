package org.apache.velocity.runtime;

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

/**
 * This class defines the keys that are used in the velocity.properties file so that they can be referenced as a constant within
 * Java code.
 *
 * @author  <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author  <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author  <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version  $Id$
 */

public interface RuntimeConstants extends DeprecatedRuntimeConstants
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

    /** externally provided logger. */
    String RUNTIME_LOG_INSTANCE = "runtime.log.instance";

    /** externally provided logger name. */
    String RUNTIME_LOG_NAME = "runtime.log.name";

    /** Logging of invalid references. */
    String RUNTIME_LOG_REFERENCE_LOG_INVALID = "runtime.log.log_invalid_references";

    /** Logging of invalid method calls. */
    String RUNTIME_LOG_METHOD_CALL_LOG_INVALID = "runtime.log.log_invalid_method_calls";

    /** <p>Whether to:</p>
     *  <ul>
     *      <li>populate slf4j's MDC with location in template file</li>
     *      <li>display VTL stack trace on errors</li>
     *  </ul>
     *  @since 2.2
     */
    String RUNTIME_LOG_TRACK_LOCATION = "runtime.log.track_location";

    /*
     * ----------------------------------------------------------------------
     * D I R E C T I V E  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     * Directive properties are of the form:
     *
     * directive.<directive-name>.<property>
     * ----------------------------------------------------------------------
     */

    /** Maximum allowed number of loops. */
    String MAX_NUMBER_LOOPS = "directive.foreach.max_loops";

    /**
     * Whether to throw an exception or just skip bad iterables. Default is true.
     * @since 1.6
     */
    String SKIP_INVALID_ITERATOR = "directive.foreach.skip_invalid";

    /**
     * An empty object (string, collection) or zero number is false.
     * @since 2.0
     */
    String CHECK_EMPTY_OBJECTS = "directive.if.empty_check";

    /**
     * Starting tag for error messages triggered by passing a parameter not allowed in the #include directive. Only string literals,
     * and references are allowed.
     * @deprecated if/how errors are displayed is not the concern of the engine, which should throw in all cases
     */
    String ERRORMSG_START = "directive.include.output_error_start";

    /**
     * Ending tag for error messages triggered by passing a parameter not allowed in the #include directive. Only string literals,
     * and references are allowed.
     * @deprecated if/how errors are displayed is not the concern of the engine, which should throw in all cases
     */
    String ERRORMSG_END = "directive.include.output_error_end";

    /** Maximum recursion depth allowed for the #parse directive. */
    String PARSE_DIRECTIVE_MAXDEPTH = "directive.parse.max_depth";

    /** Maximum recursion depth allowed for the #define directive. */
    String DEFINE_DIRECTIVE_MAXDEPTH = "directive.define.max_depth";

    /**
     * Used to suppress various scope control objects (property suffix).
     * @since 1.7
     * @deprecated use <code>context.scope_control.&lt;scope_name&gt; = true/false</code>
     * @see #CONTEXT_SCOPE_CONTROL
     */
    @Deprecated
    String PROVIDE_SCOPE_CONTROL = "provide.scope.control";

    /**
     * Used to enable or disable a scope control (false by default):
     * <code>context.scope_control.&lt;scope_name&gt; = true/false</code>
     * where <i>scope_name</i> is one of: <code>template, evaluate, foreach, macro, define</code>
     * or the name of a body macro.
     * @since 2.1
     */
    String CONTEXT_SCOPE_CONTROL = "context.scope_control";

    /**
     * Vector of custom directives
     */
    String CUSTOM_DIRECTIVES = "runtime.custom_directives";

    /*
     * ----------------------------------------------------------------------
     *  R E S O U R C E   M A N A G E R   C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /**
     * The <code>resource.manager.instance</code> property specifies an existing instance of a
     * {@link org.apache.velocity.runtime.resource.ResourceManager} implementation to use
     */
    String RESOURCE_MANAGER_INSTANCE = "resource.manager.instance";

    /**
    * The <code>resource.manager.class</code> property specifies the name of the
    * {@link org.apache.velocity.runtime.resource.ResourceManager} implementation to use.
    */
    String RESOURCE_MANAGER_CLASS = "resource.manager.class";

    /**
     * The <code>resource.manager.cache.class</code> property specifies the name of the
     * {@link org.apache.velocity.runtime.resource.ResourceCache} implementation to use.
     */
    String RESOURCE_MANAGER_CACHE_CLASS = "resource.manager.cache.class";

    /** The <code>resource.manager.cache.size</code> property specifies the cache upper bound (if relevant). */
    String RESOURCE_MANAGER_DEFAULTCACHE_SIZE = "resource.manager.cache.default_size";

    /*
     * ----------------------------------------------------------------------
     * R E S O U R C E  L O A D E R  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** controls if the finding of a resource is logged. */
    String RESOURCE_MANAGER_LOGWHENFOUND = "resource.manager.log_when_found";

    /**
     * Key used to retrieve the names of the resource loaders to be used. In a properties file they may appear as the following:
     *
     * <p>resource.loaders = file,classpath</p>
     */
    String RESOURCE_LOADERS = "resource.loaders";

    /**
     * Key prefix for a specific resource loader properties
     *
     * <p>resource.loader.file.path = ...</p>
     */
    String RESOURCE_LOADER = "resource.loader";

    /** The public handle for setting paths in the FileResourceLoader.
     * (this constant is used by test cases only)
     */
    String FILE_RESOURCE_LOADER_PATH = "resource.loader.file.path";

    /** The public handle for turning the caching on in the FileResourceLoader. */
    String FILE_RESOURCE_LOADER_CACHE = "resource.loader.file.cache";

    /**
     * Resource loader class property suffix
     */
    String RESOURCE_LOADER_CLASS = "class";

    /**
     * Resource loader instance property suffix
     */
    String RESOURCE_LOADER_INSTANCE = "instance";

    /**
     * Resource loader cache property suffix
     */
    String RESOURCE_LOADER_CACHE = "cache";

    /**
     * File resource loader paths property suffix
     */
    String RESOURCE_LOADER_PATHS = "path";

    /**
     * Resource loader modification check interval property suffix
     */
    String RESOURCE_LOADER_CHECK_INTERVAL = "modification_check_interval";

    /**
     * Datasource loader datasource url
     */
    String DS_RESOURCE_LOADER_DATASOURCE_URL = "resource.loader.ds.resource.datasource_url";

    /**
     * @deprecated Use {@link #DS_RESOURCE_LOADER_DATASOURCE_URL} instead.
     */
    @Deprecated
    String DS_RESOURCE_LOADER_DATASOURCE = DS_RESOURCE_LOADER_DATASOURCE_URL;

    /**
     * Datasource loader templates table
     */
    String DS_RESOURCE_LOADER_TABLE = "resource.loader.ds.resource.table";

    /**
     * Datasource loader template key column
     */
    String DS_RESOURCE_LOADER_KEY_COLUMN = "resource.loader.ds.resource.key_column";

    /**
     * Datasource loader template content column
     */
    String DS_RESOURCE_LOADER_TEMPLATE_COLUMN = "resource.loader.ds.resource.template_column";

    /**
     * Datasource loader template timestamp column
     */
    String DS_RESOURCE_LOADER_TIMESTAMP_COLUMN = "resource.loader.ds.resource.timestamp_column";

    /**
     * Datasource loader statements pool max size
     */
    String DS_RESOURCE_LOADER_STMT_POOL_MAX_SIZE = "resource.loader.ds.statements_pool_max_size";

    /** The default character encoding for the templates. Used by the parser in processing the input streams. */
    String INPUT_ENCODING = "resource.default_encoding";

    /** Default Encoding is UTF-8. */
    String ENCODING_DEFAULT = "UTF-8";

    /*
     * ----------------------------------------------------------------------
     *  E V E N T  H A N D L E R  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /**
     * The <code>event_handler.reference_insertion.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.ReferenceInsertionEventHandler} implementations to use.
     */
    String EVENTHANDLER_REFERENCEINSERTION = "event_handler.reference_insertion.class";

    /**
     * The <code>event_handler.method_exception.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.MethodExceptionEventHandler} implementations to use.
     */
    String EVENTHANDLER_METHODEXCEPTION = "event_handler.method_exception.class";

    /**
     * The <code>event_handler.include.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.IncludeEventHandler} implementations to use.
     */
    String EVENTHANDLER_INCLUDE = "event_handler.include.class";

    /**
     * The <code>event_handler.invalid_references.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.InvalidReferenceEventHandler} implementations to use.
     */
    String EVENTHANDLER_INVALIDREFERENCES = "event_handler.invalid_references.class";

    /**
     * The <code>event_handler.invalid_references.quiet</code> property specifies if invalid quiet references
     * (as in <code>$!foo</code>) trigger events (defaults to false).
     * {@link org.apache.velocity.app.event.InvalidReferenceEventHandler} implementations to use.
     * @since 2.2
     */
    String EVENTHANDLER_INVALIDREFERENCES_QUIET = "event_handler.invalid_references.quiet";

    /**
     * The <code>event_handler.invalid_references.null</code> property specifies if invalid null references
     * (aka the value is present in the context or parent object but is null or a method returned null)
     * trigger invalid reference events (defaults to false).
     * {@link org.apache.velocity.app.event.InvalidReferenceEventHandler} implementations to use.
     * @since 2.2
     */
    String EVENTHANDLER_INVALIDREFERENCES_NULL = "event_handler.invalid_references.null";

    /**
     * The <code>event_handler.invalid_references.tested</code> property specifies if invalid tested references
     * (as in <code>#if($foo)</code> ) trigger invalid reference events (defaults to false).
     * {@link org.apache.velocity.app.event.InvalidReferenceEventHandler} implementations to use.
     * @since 2.2
     */
    String EVENTHANDLER_INVALIDREFERENCES_TESTED = "event_handler.invalid_references.tested";

    /*
     * ----------------------------------------------------------------------
     * V E L O C I M A C R O  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** Filename of local Velocimacro library template. */
    String VM_LIBRARY = "velocimacro.library.path";

    /** Default Velocimacro library template. */
    String VM_LIBRARY_DEFAULT = "velocimacros.vtl";

    /** switch for autoloading library-sourced VMs (for development). */
    String VM_LIBRARY_AUTORELOAD = "velocimacro.library.autoreload";

    /** boolean (true/false) default true: allow inline (in-template) macro definitions. */
    String VM_PERM_ALLOW_INLINE = "velocimacro.inline.allow";

    /** boolean (true/false) default false: allow inline (in-template) macro definitions to replace existing. */
    String VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL = "velocimacro.inline.replace_global";

    /** Switch for forcing inline macros to be local: default false. */
    String VM_PERM_INLINE_LOCAL = "velocimacro.inline.local_scope";

    /** if true, throw an exception for wrong number of arguments **/
    String VM_ARGUMENTS_STRICT = "velocimacro.arguments.strict";

    /**
     * This flag enable the 1.7 backward compatible mode for velocimacros (defaults to false):
     * <ul>
     *     <li>
     *         preserve argument literals: when displaying null or invalid non-quiet references,
     *         use the argument literal reference instead of the one in the macro block. Defaults to false.
     *     </li>
     *     <li>
     *         use global values for missing arguments: when calling a macro with fewer arguments than declared,
     *         if those arguments don't have an explicit default value in the macro definition, default values will
     *         be looked for in the global context
     *     </li>
     * </ul>
     * @since 2.2
     */
    String VM_ENABLE_BC_MODE = "velocimacro.enable_bc_mode";

    /**
     * Specify the maximum depth for macro calls
     * @since 1.6
     */
    String VM_MAX_DEPTH = "velocimacro.max_depth";

    /**
     * Defines name of the reference that can be used to get the AST block passed to block macro calls.
     * @since 1.7
     */
    String VM_BODY_REFERENCE = "velocimacro.body_reference";

    /**
     * <p>Switch for VM blather: default true. Unused since 2.0.</p>
     * @deprecated since 2.1
     */
    @Deprecated
    String VM_MESSAGES_ON = "velocimacro.messages.on";

    /*
     * ----------------------------------------------------------------------
     * S T I C T   M O D E  B E H A V I O U R
     * ----------------------------------------------------------------------
     */

    /**
     * Properties referenced in the template are required to exist the object
     */
    String RUNTIME_REFERENCES_STRICT = "runtime.strict_mode.enable";

    /**
     * Indicates we are going to use modified escape behavior in strict mode
     */
    String RUNTIME_REFERENCES_STRICT_ESCAPE = "runtime.strict_mode.escape";

    /*
     * ----------------------------------------------------------------------
     * I N T R O S P E C T I O N  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** key name for uberspector. Multiple classnames can be specified,in which case uberspectors will be chained. */
    String UBERSPECT_CLASSNAME = "introspector.uberspect.class";

    /** A comma separated list of packages to restrict access to in the SecureIntrospector. */
    String INTROSPECTOR_RESTRICT_PACKAGES = "introspector.restrict.packages";

    /** A comma separated list of classes to restrict access to in the SecureIntrospector. */
    String INTROSPECTOR_RESTRICT_CLASSES = "introspector.restrict.classes";

    /** key for Conversion Manager class */
    String CONVERSION_HANDLER_CLASS = "introspector.conversion_handler.class";

    /** key for Conversion Manager instance */
    String CONVERSION_HANDLER_INSTANCE = "introspector.conversion_handler.instance";

    /*
     * ----------------------------------------------------------------------
     * P A R S E R  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /**
     * Property specifying the parser class to use
     * @since 2.2
     */
    String PARSER_CLASS = "parser.class";

    /**
     * Default parser class
     * @since 2.2
     */
    String DEFAULT_PARSER_CLASS = "org.apache.velocity.runtime.parser.StandardParser";

    /**
     * The <code>parser.pool.class</code> property specifies the name of the {@link org.apache.velocity.util.SimplePool}
     * implementation to use.
     */
    String PARSER_POOL_CLASS = "parser.pool.class";

    /**
     * @see  #NUMBER_OF_PARSERS
     */
    String PARSER_POOL_SIZE = "parser.pool.size";

    /**
     * Allow hyphen in identifiers (backward compatibility option)
     * @since 2.1
     */
    String PARSER_HYPHEN_ALLOWED = "parser.allow_hyphen_in_identifiers";

    /*
     * ----------------------------------------------------------------------
     * G E N E R A L  R U N T I M E  C O N F I G U R A T I O N
     * ----------------------------------------------------------------------
     */

    /** Whether to use string interning. */
    String RUNTIME_STRING_INTERNING = "runtime.string_interning";

    /** Switch for the interpolation facility for string literals. */
    String INTERPOLATE_STRINGLITERALS = "runtime.interpolate_string_literals";

    /** Switch for the immutability of integer ranges. */
    String IMMUTABLE_RANGES = "runtime.immutable_ranges";

    /** Switch for ignoring nulls in math equations vs throwing exceptions. */
    String STRICT_MATH = "runtime.strict_math";

    /** Key upon which a context should be accessible within itself */
    String CONTEXT_AUTOREFERENCE_KEY = "context.self_reference_key";

    /**
     * Space gobbling mode
     * @since 2.0
     */
    String SPACE_GOBBLING = "parser.space_gobbling";

    /**
     * Space gobbling modes
     * @since 2.0
     */
    enum SpaceGobbling
    {
        NONE, BC, LINES, STRUCTURED
    }

    /*
     * ----------------------------------------------------------------------
     * These constants are used internally by the Velocity runtime i.e.
     * the constants listed below are strictly used in the Runtime
     * class itself.
     * ----------------------------------------------------------------------
     */

    /** Default Runtime properties. */
    String DEFAULT_RUNTIME_PROPERTIES = "org/apache/velocity/runtime/defaults/velocity.properties";

    /** Default Runtime properties. */
    String DEFAULT_RUNTIME_DIRECTIVES = "org/apache/velocity/runtime/defaults/directive.properties";

    /** externally provided logger name. */
    String DEFAULT_RUNTIME_LOG_NAME = "org.apache.velocity";

    /** token used to identify the loader internally. */
    String RESOURCE_LOADER_IDENTIFIER = "_RESOURCE_LOADER_IDENTIFIER_";

    /**
     * The default number of parser instances to create. Configurable via the parameter named by the {@link #PARSER_POOL_SIZE}
     * constant.
     */
    int NUMBER_OF_PARSERS = 20;
}
