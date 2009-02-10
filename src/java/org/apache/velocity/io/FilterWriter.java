package org.apache.velocity.io;

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
import java.io.Writer;

/**
 * FilterWriter provides a means to intercept template rendering to
 * a Writer.  The important function of this writer is to distinguish 
 * the writing of template references to the rendering of static content.
 * Velocity will use the method writeReference(String) for rendering references
 * , such as $foo, to the writer.  Other template content will be rendered
 * with the standard write methods.
 */

public class FilterWriter extends Writer implements Filter
{
  protected Writer writer = null;
  public FilterWriter(Writer w)
  {
      writer = w;
  }
  
  protected FilterWriter()
  {
  }
    
  public void close() throws IOException
  {
      writer.close();
  }

  public void flush() throws IOException
  {
      writer.flush();      
  }

  public void write(char[] cbuf, int off, int len) throws IOException
  {
      writer.write(cbuf, off, len);
  }

  /**
   * Send the content of a reference, e.g.; $foo, to the writer.
   * The default implementation is to call the wrapped Writer's
   * write(String) method. 
   */
  public void writeReference(String ref) throws IOException
  {
      writer.write(ref);
  }
}
