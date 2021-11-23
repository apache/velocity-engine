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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import org.apache.velocity.api.Resource;
import org.apache.velocity.spi.Loader;

public class MultiLoader implements Loader {

  private Loader[] loaders;

  @Override
  public boolean exists(String name, Locale locale) {
    if (loaders.length == 1) {
      return loaders[0].exists(name, locale);
    }
    for (Loader loader : loaders) {
      try {
        if (loader.exists(name, locale)) {
          return true;
        }
      } catch (Exception ignore) {
      }
    }
    return false;
  }

  @Override
  public Resource load(String name, Locale locale, String encoding) throws IOException {
    for (Loader loader : loaders) {
      try {
        if (loader.exists(name, locale)) {
          return loader.load(name, locale, encoding);
        }
      } catch (Exception ignore) {
      }
    }
    throw new FileNotFoundException("No such template file: " + name);
  }

  public void setLoaders(Loader[] loaders) {
    this.loaders = loaders;
  }

}
