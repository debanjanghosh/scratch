/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util;

import java.io.Serializable;
import java.util.Comparator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 *
 */
@XmlRootElement
@XmlType(propOrder = {"propertyType", "value", "score"})
public class DocumentProperty implements Serializable
{


    public static final Comparator<DocumentProperty> SORT_BY_SCORE_COMPARATOR = new SortByScoreComparator();
    public static final Comparator<DocumentProperty> SORT_BY_VALUE_COMPARATOR = new SortByValueComparator();

    private static final long serialVersionUID = 1L;

    //per Alex, we want to set properties that do no specify a score to -1
    public final static float UNSPECIFIED_SCORE = -1.0F;

    private String value;

    private float score = UNSPECIFIED_SCORE;

    private PropertyType propertyType;

    public DocumentProperty()
    {
    }

    public DocumentProperty(String value, float score, PropertyType propertyType)
    {
        this.value = value;
        this.score = score;
        this.propertyType = propertyType;
    }

    @XmlElement(name = "v")
    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @XmlElement(name = "t")
    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    @XmlElement(name = "s")
    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentProperty that = (DocumentProperty) o;

        if (Float.compare(that.score, score) != 0) return false;
        if (propertyType != null ? !propertyType.equals(that.propertyType) : that.propertyType != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        result = 31 * result + (propertyType != null ? propertyType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return value + "; " + score + "; " + propertyType;
    }


    /**
     * Compares two document properties by their score and places the properties
     * with the higher scores *before* the properties with the lowest scores.
     * <p>
     * In other words sorting a list of document properties using this comparator
     * will result in a list sorted by descending order of scores.
     * <p>
     * Properties whose scores are tied are then sorted lexicographically by their value
     * </p>
     */
    public static class SortByScoreComparator implements Comparator<DocumentProperty>, Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DocumentProperty o1, DocumentProperty o2)
        {
            int compare = Float.compare(o2.getScore(), o1.getScore());
            if (compare == 0)
            {
                compare = o1.getValue().compareTo(o2.getValue());
            }
            return compare;
        }
    }

    /** Lexicographically compares two document properties by their values. */
    public static class SortByValueComparator implements Comparator<DocumentProperty>, Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DocumentProperty o1, DocumentProperty o2)
        {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}
