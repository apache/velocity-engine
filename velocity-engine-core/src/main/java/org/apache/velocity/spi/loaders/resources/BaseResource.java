package org.apache.velocity.spi.loaders.resources;

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
import java.io.Serializable;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.api.Resource;
import org.apache.velocity.runtime.RuntimeInstance;

public abstract class BaseResource implements Resource, Serializable {

  private static final long serialVersionUID = 86551207664766539L;

  private final transient RuntimeInstance engine;
  private final String name;
  private final String encoding;
  private final Locale locale;
  private final long lastModified;

  public BaseResource(RuntimeInstance engine, String name, Locale locale, String encoding, long lastModified) {
    this.engine = engine;
    this.name = name;
    this.encoding = encoding;
    this.locale = locale;
    this.lastModified = lastModified;
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
    return -1;
  }

  @Override
  public String getSource() throws IOException {
    StringBuilder source = new StringBuilder();
    IOUtils.copy(openReader(), source);
    return source.toString();
  }

  @Override
  public RuntimeInstance getEngine() {
    return engine;
  }

}
