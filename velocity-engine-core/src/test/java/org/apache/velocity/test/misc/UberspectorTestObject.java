package org.apache.velocity.test.misc;

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

import java.util.Map;

public class UberspectorTestObject
{
    private String regular;
    private String premium;

    private boolean regularBool;
    private boolean premiumBool;

    private String ambigous;

    private String unambiguous;
    
    /**
     * @return the premium
     */
    public String getpremium()
    {
        return premium;
    }

    /**
     * @param premium the premium to set
     */
    public void setpremium(String premium)
    {
        this.premium = premium;
    }

    /**
     * @return the premiumBool
     */
    public boolean ispremiumBool()
    {
        return premiumBool;
    }

    /**
     * @param premiumBool the premiumBool to set
     */
    public void setpremiumBool(boolean premiumBool)
    {
        this.premiumBool = premiumBool;
    }

    /**
     * @return the regular
     */
    public String getRegular()
    {
        return regular;
    }

    /**
     * @param regular the regular to set
     */
    public void setRegular(String regular)
    {
        this.regular = regular;
    }

    /**
     * @return the regularBool
     */
    public boolean isRegularBool()
    {
        return regularBool;
    }

    /**
     * @param regularBool the regularBool to set
     */
    public void setRegularBool(boolean regularBool)
    {
        this.regularBool = regularBool;
    }

    /**
     * @return the ambigous
     */
    public String getAmbigous()
    {
        return ambigous;
    }

    /**
     * @param ambigous the ambigous to set
     */
    public void setAmbigous(String ambigous)
    {
        this.ambigous = ambigous;
    }

    /**
     * @param ambigous the ambigous to set
     */
    public void setAmbigous(StringBuffer ambigous)
    {
        this.ambigous = ambigous.toString();
    }

    public void setUnambiguous(String unambiguous)
    {
        this.unambiguous = unambiguous;
    }

    public void setUnambiguous(Map unambiguous)
    {
        this.unambiguous = unambiguous.toString();
    }
}
