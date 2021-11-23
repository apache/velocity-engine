package org.apache.velocity.spi.compilers;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.spi.Cache;
import org.apache.velocity.spi.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCompiler implements Compiler {

  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([_a-zA-Z][_a-zA-Z0-9\\.]*);");
  private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([_a-zA-Z][_a-zA-Z0-9]*)\\s+");

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected Cache<String, Object> cache;
  protected File compileDirectory;
  private volatile boolean first = true;

  @Override
  public Class<?> compile(String code) throws VelocityException {
    String className = null;
    try {
      code = code.trim();
      if (!code.endsWith("}")) {
        throw new ParseException("The java code not endsWith \"}\"", code.length() - 1);
      }
      Matcher matcher = PACKAGE_PATTERN.matcher(code);
      String pkg;
      if (matcher.find()) {
        pkg = matcher.group(1);
      } else {
        pkg = "";
      }
      matcher = CLASS_PATTERN.matcher(code);
      String classSimpleName;
      if (matcher.find()) {
        classSimpleName = matcher.group(1);
      } else {
        throw new VelocityException("No such class name in java code.");
      }
      className = StringUtils.isNotEmpty(pkg) ? pkg + "." + classSimpleName : classSimpleName;
      return doCompile(className, code);
    } catch (Throwable t) {
      if (logger != null && logger.isErrorEnabled()) {
        logger.error("Failed to compile class, cause: " + t.getMessage() + ", class: " + className
            + ", code: \n================================\n" + code + "\n================================\n", t);
      }
      if (t instanceof VelocityException) {
        throw (VelocityException) t;
      }
      throw new VelocityException(
          "Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", stack: ", t);
    }
  }

  protected abstract Class<?> doCompile(String name, String source) throws Exception;

  public void setCache(Cache<String, Object> cache) {
    this.cache = cache;
  }

  protected void saveBytecode(String name, byte[] bytecode) {
    if (compileDirectory != null) {
      try {
        File file = new File(compileDirectory, name.replace('.', '/') + ".class");
        FileOutputStream out = new FileOutputStream(file);
        try {
          out.write(bytecode);
          out.flush();
        } finally {
          out.close();
        }
        if (first) {
          first = false;
          if (logger != null && logger.isInfoEnabled()) {
            logger.info("Compile template classes to directory " + compileDirectory.getAbsolutePath());
          }
        }
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }

}
