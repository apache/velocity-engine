package org.apache.velocity.spi.loaders;

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
import java.util.Locale;
import org.apache.velocity.api.Resource;
import org.apache.velocity.spi.loaders.resources.ClasspathResource;

public class ClasspathLoader extends BaseLoader {

  @Override
  protected boolean doExists(String name, Locale locale, String path) throws IOException {
    return Thread.currentThread().getContextClassLoader().getResource(path) != null;
  }

  @Override
  protected Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException {
    return new ClasspathResource(engine, name, locale, encoding, path);
  }

}
