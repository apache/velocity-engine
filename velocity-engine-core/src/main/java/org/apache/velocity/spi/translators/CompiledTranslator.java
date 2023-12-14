package org.apache.velocity.spi.translators;

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

import org.apache.velocity.api.Resource;
import org.apache.velocity.api.Template;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.spi.Compiler;
import org.apache.velocity.spi.Translator;
import org.apache.velocity.spi.translators.templates.CompiledVisitor;

public class CompiledTranslator implements Translator {

  private Compiler compiler;

  @Override
  public Template translate(Resource resource, SimpleNode root) {
    CompiledVisitor visitor = new CompiledVisitor();
    String code = visitor.visit(root, resource);
    try {
      return (Template) compiler.compile(code).getConstructor().newInstance();
    } catch (Throwable e) {
      throw new VelocityException(e);
    }
  }

  public void setCompiler(Compiler compiler) {
    this.compiler = compiler;
  }

}
