package org.apache.velocity.runtime;

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

/**
 * Class gathering configured replacement characters for a specific parser class.
 * @since 2.2
 */

public class ParserConfiguration
{
    /**
     * Configured replacement character for '$'
     */
    private char dollar = '$';

    /**
     * Configured replacement character for '#'
     */
    private char hash = '#';

    /**
     * Configured replacement character for '@'
     */
    private char at = '@';

    /**
     * Configured replacement character for '*'
     */
    private char asterisk = '*';

    /**
     * Getter for '$' configured replacement character
     * @return configured replacement character for '$'
     */
    public char getDollarChar()
    {
        return dollar;
    }

    /**
     * Setter for '$' configured replacement character
     */
    void setDollarChar(char dollar)
    {
        this.dollar = dollar;
    }

    /**
     * Getter for '#' configured replacement character
     * @return configured replacement character for '#'
     */
    public char getHashChar()
    {
        return hash;
    }

    /**
     * Setter for '#' configured replacement character
     */
    void setHashChar(char hash)
    {
        this.hash = hash;
    }

    /**
     * Getter for '@' configured replacement character
     * @return configured replacement character for '@'
     */
    public char getAtChar()
    {
        return at;
    }

    /**
     * Setter for '@' configured replacement character
     */
    void setAtChar(char at)
    {
        this.at = at;
    }

    /**
     * Getter for '*' configured replacement character
     * @return configured replacement character for '*'
     */
    public char getAsteriskChar()
    {
        return asterisk;
    }

    /**
     * Setter for '*' configured replacement character
     */
    void setAsteriskChar(char asterisk)
    {
        this.asterisk = asterisk;
    }
}
