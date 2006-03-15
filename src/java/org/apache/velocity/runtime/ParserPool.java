package org.apache.velocity.runtime;

import org.apache.velocity.runtime.parser.Parser;

/*
 * Copyright 2000-2006 The Apache Software Foundation.
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

/**
 * Provides instances of parsers as needed.  get() will return a new parser if
 * available.  If a parser is acquired from the pool, put() should be called
 * with that parser to make it available again for reuse.
 *
 * @author <a href="mailto:sergek@lokitech.com">Serge Knystautas</a>
 * @version $Id: RuntimeInstance.java 384374 2006-03-08 23:19:30Z nbubna $
 */
public interface ParserPool
{
    /**
     * Initialize the pool so that it can begin serving parser instances.
     */
    void initialize(RuntimeServices svc);

    /**
     * Retrieve an instance of a parser pool.
     */
    Parser get();

    /**
     * Return the parser to the pool so that it may be reused.
     */
    void put(Parser parser);
}
