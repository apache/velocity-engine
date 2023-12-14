package org.apache.velocity.spi.translators.templates;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Locale;
import org.apache.velocity.api.Resource;
import org.apache.velocity.api.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;

public abstract class BaseTemplate implements Template {

  private final Resource resource;
  private final SimpleNode root;
  private final Template parent;
  private final String name;
  private final String encoding;
  private final Locale locale;
  private final long lastModified;
  private final long length;

  public BaseTemplate(Resource resource, SimpleNode node, Template parent) {
    this.resource = resource;
    this.root = node;
    this.parent = parent;
    this.name = resource.getName();
    this.encoding = resource.getEncoding();
    this.locale = resource.getLocale();
    this.lastModified = resource.getLastModified();
    this.length = resource.getLength();
  }

  @Override
  public void render(Context context, Writer out) throws IOException, ParseException {
    try {
      doRender(context, out);
    } catch (IOException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object evaluate(Context context) throws ParseException {
    StringWriter writer = new StringWriter();
    try {
      render(context, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

  @Override
  public void accept(BaseVisitor visitor) {
    visitor.visit(root, resource);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEncoding() {
    return encoding;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public long getLength() {
    return length;
  }

  @Override
  public String getSource() throws IOException {
    return resource.getSource();
  }

  @Override
  public Reader openReader() throws IOException {
    return resource.openReader();
  }

  @Override
  public InputStream openStream() throws IOException {
    return resource.openStream();
  }

  @Override
  public RuntimeInstance getEngine() {
    return resource.getEngine();
  }

  public SimpleNode getRoot() {
    return root;
  }

  public Template getParent() {
    return parent;
  }

  protected abstract void doRender(Context context, Writer out) throws Exception;

}
