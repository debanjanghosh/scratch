/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.LDA;

/**
 *
 *
 */
public class LDADataPair implements Comparable<LDADataPair>
{

    private int key;
    private float score;

    public LDADataPair(int key, float score)
    {
        this.key = key;
        this.score = score;
    }

    public int getKey()
    {
        return key;
    }

    public float getScore()
    {
        return score;
    }

    @Override
    public int compareTo(LDADataPair o)
    {
        int cmp = Float.compare(o.score, this.score);
        if (cmp == 0)
        {
            if (this.key > o.key)
            {
                return 1;
            }
            else if (this.key < o.key)
            {
                return -1;
            }
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LDADataPair that = (LDADataPair) o;

        if (key != that.key) return false;
        if (Float.compare(that.score, score) != 0) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = key;
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        return result;
    }
}
