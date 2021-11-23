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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.runtime.RuntimeInstance;

public abstract class InputStreamResource extends BaseResource {

  private static final long serialVersionUID = 2729791474763118265L;

  private final String file;

  public InputStreamResource(RuntimeInstance engine, String name, Locale locale, String encoding, String file) {
    this(engine, name, locale, encoding, -1, file);
  }

  public InputStreamResource(RuntimeInstance engine, String name, Locale locale, String encoding, long lastModified,
      String file) {
    super(engine, name, locale, encoding, lastModified);
    this.file = file;
  }

  @Override
  public long getLastModified() {
    File f = new File(getFile());
    if (f != null && f.exists()) {
      return f.lastModified();
    }
    return super.getLastModified();
  }

  @Override
  public long getLength() {
    File f = new File(getFile());
    if (f != null) {
      return f.length();
    }
    return super.getLength();
  }

  @Override
  public Reader openReader() throws IOException {
    InputStream in = openStream();
    if (in == null) {
      throw new FileNotFoundException(
          "Not found template " + getName() + " in " + getClass().getSimpleName() + ": " + file);
    }
    String encoding = getEncoding();
    return StringUtils.isEmpty(encoding)
        ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
  }

  public String getFile() {
    return file;
  }

}
