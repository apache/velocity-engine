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
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipFile;
import org.apache.velocity.runtime.RuntimeInstance;

public class ZipResource extends InputStreamResource {

  private static final long serialVersionUID = -5614543506378211399L;

  public ZipResource(RuntimeInstance engine, String name, Locale locale, String encoding, String file) {
    super(engine, name, locale, encoding, file);
  }

  @Override
  public InputStream openStream() throws IOException {
    ZipFile zipFile = new ZipFile(getFile());
    return zipFile.getInputStream(zipFile.getEntry(getName()));
  }
}
