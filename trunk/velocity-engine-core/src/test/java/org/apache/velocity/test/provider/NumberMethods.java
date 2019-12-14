package org.apache.velocity.test.provider;

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

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Used to check that method calls with number parameters are executed correctly.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 */
public class NumberMethods
{

  public String numMethod(byte val)
  {
      return "byte (" + val + ")";
  }

  public String numMethod(short val)
  {
      return "short (" + val + ")";
  }

  public String numMethod(int val)
  {
      return "int (" + val + ")";
  }

  public String numMethod(double val)
  {
      return "double (" + val + ")";
  }

  public String numMethod(long val)
  {
      return "long (" + val + ")";
  }

  public String numMethod(BigInteger val)
  {
      return "BigInteger (" + val + ")";
  }

  public String numMethod(BigDecimal val)
  {
      return "BigDecimal (" + val + ")";
  }


}
