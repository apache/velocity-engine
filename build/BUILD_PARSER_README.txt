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

Modifying and building the parser
=================================

The parser is a 'special' piece of the build tree - currently it doesn't
behave as everything else due to javacc and the package layout. 

In the simple case of modifying the parser via Parser.jjt, use the
"ant parser" task in the main build script. It runs 'jjtree' on Parser.jjt
to make the AST nodes (which are then deleted later - more on this in a bit)
and creates Parser.jj for javacc.

Javacc is then run on Parser.jj to make Parser.java, which will be compiled
like any other piece of java source via the main build script.

The build script then removes generated files that are not needed:
    - Node.java, ParserVisitor.java for later versions of JavaCC.
    - Node.java, ParserVisitor.java, AST* and SimpleNode.java for earlier
      versions of JavaCC.
               
In the event that something 'serious' changes, such as an AST node is created
or altered, it must be *manually* moved to the node subdirectory, and have its
package declaration fixed.  This should be an extremely rare event at this point
and will change with javacc 2.0.

When committing changes, to aid readability to those watching the cvs commit 
messages, please commit Parser.jjt separately from the .jj and .java
files generated from .jjt. 

-gmj


Finally, note that in order to create code that will compile with JDK 1.5,
you will need to use JavaCC 3.2 or later (replaces variable "enumeration" or "enum"
with "e". (WGH)

The parser was last built using JavaCC 4.1.
