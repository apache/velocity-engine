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
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.api.Resource;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.spi.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseLoader implements Loader {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected RuntimeInstance engine;
  protected String encoding;
  protected Locale locale;
  protected boolean reloadable;
  protected String[] templateDirectory; // 模板目录
  protected String[] templateSuffix;    // 模板后缀

  @Override
  public boolean exists(String name, Locale locale) {
    Locale cur = locale;
    while (cur != null) {
      if (_exists(name, locale, toPath(name, cur))) {
        return true;
      }
      cur = getParentLocale(cur);
    }
    return _exists(name, locale, toPath(name, null));
  }

  @Override
  public Resource load(String name, Locale locale, String encoding) throws IOException {
    if (StringUtils.isBlank(encoding)) {
      encoding = this.encoding;
    }
    Locale cur = locale;
    String path = toPath(name, cur);
    while (cur != null && !_exists(name, locale, path)) {
      cur = getParentLocale(cur);
      path = toPath(name, cur);
    }
    return doLoad(name, locale, encoding, path);
  }

  protected abstract boolean doExists(String name, Locale locale, String path) throws IOException;

  protected abstract Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException;

  private boolean _exists(String name, Locale locale, String path) {
    try {
      return doExists(name, locale, path);
    } catch (Exception e) {
      return false;
    }
  }

  protected String toPath(String name, Locale locale) {
    if (endsWith(name, templateSuffix)) {
      name = relocate(name, locale, templateDirectory);
    }
    return appendLocale(name, locale);
  }

  protected String relocate(String name, Locale locale, String[] directories) {
    if (directories != null && directories.length > 0) {
      for (String directory : directories) {
        try {
          if (doExists(name, locale, directory + name)) {
            return directory + name;
          }
        } catch (IOException ignore) {
          continue;
        }
      }
      return directories[0] + name;
    }
    return name;
  }

  private static boolean endsWith(String value, String[] suffixes) {
    if (suffixes == null || suffixes.length == 0) {
      return false;
    }
    for (String suffix : suffixes) {
      if (StringUtils.endsWith(value, suffix)) {
        return true;
      }
    }
    return false;
  }

  private static String appendLocale(String name, Locale locale) {
    if (locale == null) {
      return name;
    }
    return locale + name;
  }

  private static Locale getParentLocale(Locale locale) {
    return null;
  }

  // region setter

  public void setEngine(RuntimeInstance engine) {
    this.engine = engine;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public void setReloadable(boolean reloadable) {
    this.reloadable = reloadable;
  }

  public void setTemplateDirectory(String[] templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  public void setTemplateSuffix(String[] templateSuffix) {
    this.templateSuffix = templateSuffix;
  }

  // endregion setter

}
