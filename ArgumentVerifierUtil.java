/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util ;

/**
 *
 *
 */
public class ArgumentVerifierUtil
{
    private ArgumentVerifierUtil() {}


    /**
     * Checks if the given Object value is null.  If the object is null an IllegalArgumentException
     * is thrown.
     *
     * @param value     the value to check
     * @param paramName the name of the parameter being checked which is included in the exception message
     *
     * @throws IllegalArgumentException if value is null
     */
    public static void checkForNull(Object value, String paramName) throws IllegalArgumentException
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Param [" + paramName + "] can not be null");
        }
    }

    /**
     * Checks if the given String is null, empty or contains all whitespace.  If the value is null, empty
     * or all whitespace an IllegalArgumentException is thrown.
     *
     * @param value     the value to check
     * @param paramName the name of the parameter being checked which is included in the exception message
     *
     * @throws IllegalArgumentException if value is null, empty or all whitespace
     */
    public static void checkForNullOrEmpty(String value, String paramName) throws IllegalArgumentException
    {

        if (isBlank(value))
        {
            throw new IllegalArgumentException("Param [" + paramName + "] can not be null or empty.  It is [" + value + ']');
        }
    }

    private static boolean isBlank(String value)
    {
        if (value == null || value.isEmpty())
        {
            return true;
        }

        int length = value.length();
        for (int i = 0; i < length; i++)
        {
            if (!Character.isWhitespace(value.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
}