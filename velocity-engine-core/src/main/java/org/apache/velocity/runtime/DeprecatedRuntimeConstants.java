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
 * This class gathers deprecated runtime constants
 *
 * @author  <a href="mailto:claude@renegat.net">Claude Brisson</a>
 * @version  $$
 */

@Deprecated
public interface DeprecatedRuntimeConstants
{
    /**
     * Logging of invalid references.
     * @deprecated see {@link RuntimeConstants#RUNTIME_LOG_REFERENCE_LOG_INVALID}
     */
    String OLD_RUNTIME_LOG_REFERENCE_LOG_INVALID = "runtime.log.invalid.references";

    /**
     * Maximum allowed number of loops.
     * @deprecated see {@link RuntimeConstants#MAX_NUMBER_LOOPS}
     */
    String OLD_MAX_NUMBER_LOOPS = "directive.foreach.maxloops";

    /**
     * Whether to throw an exception or just skip bad iterables. Default is true.
     * @since 1.6
     * @deprecated see {@link RuntimeConstants#SKIP_INVALID_ITERATOR}
     */
    String OLD_SKIP_INVALID_ITERATOR = "directive.foreach.skip.invalid";

    /**
     * An empty object (string, collection) or zero number is false.
     * @since 2.0
     * @deprecated see {@link RuntimeConstants#CHECK_EMPTY_OBJECTS}
     */
    String OLD_CHECK_EMPTY_OBJECTS = "directive.if.emptycheck";

    /**
     * Starting tag for error messages triggered by passing a parameter not allowed in the #include directive. Only string literals,
     * and references are allowed.
     * @deprecated see {@link RuntimeConstants#ERRORMSG_START}
     */
    String OLD_ERRORMSG_START = "directive.include.output.errormsg.start";

    /**
     * Ending tag for error messages triggered by passing a parameter not allowed in the #include directive. Only string literals,
     * and references are allowed.
     * @deprecated see {@link RuntimeConstants#ERRORMSG_END}
     */
    String OLD_ERRORMSG_END = "directive.include.output.errormsg.end";

    /**
     * Maximum recursion depth allowed for the #parse directive.
     * @deprecated see {@link RuntimeConstants#PARSE_DIRECTIVE_MAXDEPTH}
     */
    String OLD_PARSE_DIRECTIVE_MAXDEPTH = "directive.parse.max.depth";

    /**
     * Maximum recursion depth allowed for the #define directive.
     * @deprecated see {@link RuntimeConstants#DEFINE_DIRECTIVE_MAXDEPTH}
     */
    String OLD_DEFINE_DIRECTIVE_MAXDEPTH = "directive.define.max.depth";

    /**
     * Vector of custom directives
     * @deprecated  see {@link RuntimeConstants#CUSTOM_DIRECTIVES}
     */
    String OLD_CUSTOM_DIRECTIVES = "userdirective";

    /**
     * The <code>resource.manager.cache.size</code> property specifies the cache upper bound (if relevant).
     * @deprecated see {@link RuntimeConstants#RESOURCE_MANAGER_DEFAULTCACHE_SIZE}
     */
    String OLD_RESOURCE_MANAGER_DEFAULTCACHE_SIZE = "resource.manager.defaultcache.size";

    /**
     * controls if the finding of a resource is logged.
     * @deprecated see {@link RuntimeConstants#RESOURCE_MANAGER_LOGWHENFOUND}
     */
    String OLD_RESOURCE_MANAGER_LOGWHENFOUND = "resource.manager.logwhenfound";

    /**
     * Key used to retrieve the names of the resource loaders to be used. In a properties file they may appear as the following:
     * <p>resource.loader = file,classpath</p>
     * @deprecated see {@link RuntimeConstants#RESOURCE_LOADERS}
     */
    String OLD_RESOURCE_LOADERS = "resource.loader";

    /**
     * The public handle for setting a path in the FileResourceLoader.
     * @deprecated see {@link RuntimeConstants#FILE_RESOURCE_LOADER_PATH}
     */
    String OLD_FILE_RESOURCE_LOADER_PATH = "file.resource.loader.path";

    /**
     * The public handle for turning the caching on in the FileResourceLoader.
     * @deprecated see {@link RuntimeConstants#FILE_RESOURCE_LOADER_CACHE}
     */
    String OLD_FILE_RESOURCE_LOADER_CACHE = "file.resource.loader.cache";

    /**
     * Resource loader modification check interval property suffix
     */
    String OLD_RESOURCE_LOADER_CHECK_INTERVAL = "modificationCheckInterval";

    /**
     * Datasource loader datasource url
     * @deprecated see {@link RuntimeConstants#DS_RESOURCE_LOADER_DATASOURCE}
     */
    String OLD_DS_RESOURCE_LOADER_DATASOURCE = "ds.resource.loader.resource.datasource";

    /**
     * Datasource loader template key column
     * @deprecated see {@link RuntimeConstants#DS_RESOURCE_LOADER_KEY_COLUMN}
     */
    String OLD_DS_RESOURCE_LOADER_KEY_COLUMN = "ds.resource.loader.resource.keycolumn";

    /**
     * Datasource loader template content column
     * @deprecated see {@link RuntimeConstants#DS_RESOURCE_LOADER_TEMPLATE_COLUMN}
     */
    String OLD_DS_RESOURCE_LOADER_TEMPLATE_COLUMN = "ds.resource.loader.resource.templatecolumn";

    /**
     * Datasource loader template timestamp column
     * @deprecated see {@link RuntimeConstants#DS_RESOURCE_LOADER_TIMESTAMP_COLUMN}
     */
    String OLD_DS_RESOURCE_LOADER_TIMESTAMP_COLUMN = "ds.resource.loader.resource.timestampcolumn";

    /**
     * The default character encoding for the templates. Used by the parser in processing the input streams.
     * @deprecated see {@link RuntimeConstants#INPUT_ENCODING}
     */
    String OLD_INPUT_ENCODING = "input.encoding";

    /**
     * The <code>eventhandler.referenceinsertion.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.ReferenceInsertionEventHandler} implementations to use.
     * @deprecated see {@link RuntimeConstants#EVENTHANDLER_REFERENCEINSERTION}
     */
    String OLD_EVENTHANDLER_REFERENCEINSERTION = "eventhandler.referenceinsertion.class";

    /**
     * The <code>eventhandler.methodexception.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.MethodExceptionEventHandler} implementations to use.
     * @deprecated see {@link RuntimeConstants#EVENTHANDLER_METHODEXCEPTION}
     */
    String OLD_EVENTHANDLER_METHODEXCEPTION = "eventhandler.methodexception.class";

    /**
     * The <code>eventhandler.include.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.IncludeEventHandler} implementations to use.
     * @deprecated see {@link RuntimeConstants#EVENTHANDLER_INCLUDE}
     */
    String OLD_EVENTHANDLER_INCLUDE = "eventhandler.include.class";

    /**
     * The <code>eventhandler.invalidreferences.class</code> property specifies a list of the
     * {@link org.apache.velocity.app.event.InvalidReferenceEventHandler} implementations to use.
     * @deprecated see {@link RuntimeConstants#EVENTHANDLER_INVALIDREFERENCES}
     */
    String OLD_EVENTHANDLER_INVALIDREFERENCES = "eventhandler.invalidreferences.class";

    /**
     * Name of local Velocimacro library template.
     * @deprecated see {@link RuntimeConstants#VM_LIBRARY}
     */
    String OLD_VM_LIBRARY = "velocimacro.library";

    /**
     * Default Velocimacro library template.
     * @deprecated see {@link RuntimeConstants#VM_LIBRARY_DEFAULT}
     */
    String OLD_VM_LIBRARY_DEFAULT = "VM_global_library.vm";

    /**
     * boolean (true/false) default true: allow inline (in-template) macro definitions.
     * @deprecated see {@link RuntimeConstants#VM_PERM_ALLOW_INLINE}
     */
    String OLD_VM_PERM_ALLOW_INLINE = "velocimacro.permissions.allow.inline";

    /**
     * boolean (true/false) default false: allow inline (in-template) macro definitions to replace existing.
     * @deprecated see {@link RuntimeConstants#VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL}
     */
    String OLD_VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL = "velocimacro.permissions.allow.inline.to.replace.global";

    /**
     * Switch for forcing inline macros to be local: default false.
     * @deprecated see {@link RuntimeConstants#VM_PERM_INLINE_LOCAL}
     */
    String OLD_VM_PERM_INLINE_LOCAL = "velocimacro.permissions.allow.inline.local.scope";

    /**
     * Specify the maximum depth for macro calls
     * @since 1.6
     * @deprecated see {@link RuntimeConstants#VM_MAX_DEPTH}
     */
    String OLD_VM_MAX_DEPTH = "velocimacro.max.depth";

    /**
     * Defines name of the reference that can be used to get the AST block passed to block macro calls.
     * @since 1.7
     * @deprecated see {@link RuntimeConstants#VM_BODY_REFERENCE}
     */
    String OLD_VM_BODY_REFERENCE = "velocimacro.body.reference";

    /**
     * Properties referenced in the template are required to exist the object
     * @deprecated see {@link RuntimeConstants#RUNTIME_REFERENCES_STRICT}
     */
    String OLD_RUNTIME_REFERENCES_STRICT = "runtime.references.strict";

    /**
     * Indicates we are going to use modified escape behavior in strict mode
     * @deprecated see {@link RuntimeConstants#RUNTIME_REFERENCES_STRICT_ESCAPE}
     */
    String OLD_RUNTIME_REFERENCES_STRICT_ESCAPE = "runtime.references.strict.escape";

    /**
     * key name for uberspector. Multiple classnames can be specified,in which case uberspectors will be chained.
     * @deprecated see {@link RuntimeConstants#UBERSPECT_CLASSNAME}
     */
    String OLD_UBERSPECT_CLASSNAME = "runtime.introspector.uberspect";

    /**
     * key for Conversion Manager class
     * @deprecated see {@link RuntimeConstants#CONVERSION_HANDLER_INSTANCE}
     */
    String OLD_CONVERSION_HANDLER_CLASS = "introspector.conversion_handler.class";
    
    /**
     * Switch for the interpolation facility for string literals.
     * @deprecated see {@link RuntimeConstants#INTERPOLATE_STRINGLITERALS}
     */
    String OLD_INTERPOLATE_STRINGLITERALS = "runtime.interpolate.string.literals";

    /**
     * Switch for ignoring nulls in math equations vs throwing exceptions.
     * @deprecated see {@link RuntimeConstants#STRICT_MATH}
     */
    String OLD_STRICT_MATH = "runtime.strict.math";

    /**
     * Key upon which a context should be accessible within itself
     * @deprecated see {@link RuntimeConstants#CONTEXT_AUTOREFERENCE_KEY}
     */
    String OLD_CONTEXT_AUTOREFERENCE_KEY = "context.autoreference.key";

    /**
     * Space gobbling mode
     * @since 2.0
     * @deprecated see {@link RuntimeConstants#SPACE_GOBBLING}
     */
    String OLD_SPACE_GOBBLING = "space.gobbling";

    /**
     * When displaying null or invalid non-quiet references, use the argument literal reference
     * instead of the one in the macro block. Defaults to false.
     * @since 2.1
     * @Deprecated since 2.2, see {@link RuntimeConstants#VM_ENABLE_BC_MODE}
     **/
    String OLD_VM_ENABLE_BC_MODE = "velocimacro.arguments.preserve_literals";

}
