package org.apache.turbine.services.weather;

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



import org.apache.turbine.services.TurbineServices;

/**
 *
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 */
public class TurbineWeather
{
    /**
     * Utility method for accessing the service 
     * implementation
     *
     * @return a WeatherService implementation instance
     */
    protected static WeatherService getService()
    {
        return (WeatherService)TurbineServices
            .getInstance().getService(WeatherService.SERVICE_NAME);
    }
}
