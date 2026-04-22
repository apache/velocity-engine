package org.apache.velocity.test.sql;

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

import org.apache.velocity.runtime.resource.loader.DefaultPreparedStatementsFactory;
import org.apache.velocity.runtime.resource.loader.PreparedStatementsFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/** Test sentinel that records prepare() calls; used to verify which factory the loader picked up. */
public class SentinelPreparedStatementsFactory extends DefaultPreparedStatementsFactory
{
    public static final AtomicInteger calls = new AtomicInteger();

    public static void reset()
    {
        calls.set(0);
    }

    @Override
    public PreparedStatementsFactory.StatementHolder prepare(String sql) throws SQLException
    {
        calls.incrementAndGet();
        return super.prepare(sql);
    }
}
