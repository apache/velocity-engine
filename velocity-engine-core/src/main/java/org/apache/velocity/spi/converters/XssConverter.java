package org.apache.velocity.spi.converters;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.spi.Converter;

public class XssConverter implements Converter {

  @Override
  public String convert(String key, String value) {
    if (StringUtils.isBlank(value)) {
      return value;
    }
    char[] array = value.toCharArray();
    int length = array.length;
    StringBuilder out = new StringBuilder(length);
    int i = 0;
    while (i < length) {
      char ch = array[i++];
      switch (ch) {
        case '>': {
          out.append("&gt;");
          break;
        }
        case '<': {
          out.append("&lt;");
          break;
        }
        case '\"': {
          out.append("&quot;");
          break;
        }
        case '\'': {
          out.append("&apos;");
          break;
        }
        case '&': {
          out.append("&amp;");
          break;
        }
        default: {
          out.append(ch);
          break;
        }
      }
    }
    return out.toString();
  }

}
