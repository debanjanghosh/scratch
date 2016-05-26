/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A PropertyType denotes a particular piece of data or metadata about an IRecDocument.
 * These pieces of data are either native to the documents themselves (e.g. body and title)
 * or are generated as part of a publishing pipeline (e.g. LDA, dedupe signatures).
 * </p>
 * <p>
 * These pieces of data are used by the online recommendation system to produce the
 * recommendations.  The publishing pathways and the online recommendation system are
 * typically completely different and separate systems so it is paramount that consistent
 * types be used across both systems, hence this enum.
 * </p>
 * <p>
 * However there may also be a need to add custom properties into the system that may be
 * business need specific.  To meet this need there are enums that can be used for custom
 * properties.  These are reserved for specific pathway logic and are not used by any of the
 * standard recommendation algorithms.
 * </p>
 * <p>
 * Their names are generic (CUSTOM_A, CUSTOM_B, etc) so a recommended way to use them would
 * be to setup your own set of constants with more meaningful names and assign these enums
 * to your values.  For example:
 * </p>
 * <pre>
 *      class MyCustomTypes
 *      {
 *          public static final PropertyType MY_MEANINGFUL_NAME = PropertyType.CUSTOM_A;
 *          public static final PropertyType ANOTHER_MEANINGFUL_NAME = PropertyType.CUSTOM_B;
 *      }
 *  </pre>
 * <p>
 * They would then be used like this:
 * </p>
 * <pre>
 *      DocumentProperty prop = new DocumentProperty("someValue", 1.0f, MyCustomTypes.MY_MEANINGFUL_NAME, CodeType.TEXT);
 *
 *      DocumentProperty customProperty = iRecDocument.getProperty( MyCustomTypes.MY_MEANINGFUL_NAME );
 *  </pre>
 * <p>
 * Note however that in memory and during persistence of the properties they will appear as the custom
 * enum values (e.g. CUSTOM_A, CUSTOM_B) and not by the name that you have given them.  It is up to the
 * implementer to manage those translations.
 * </p>
 */
public enum PropertyType
{
    /** The document body text */
    BODY(1),
    /** The title of the document.  For example a headline */
    TITLE(2),
    /** The document identifier */
    DOC_ID(3),
    /** The publish date of the document */
    DOC_DATE(4),
    /** The language the document is written in */
    LANGUAGE(5),
    /** People that appear in the document */
    PEOPLE(6),
    /** Companies that appear in the document */
    COMPANY_NAME(7),
    /** Topics that the document has been assigned to */
    TOPIC(8),
    /** Whether the document should be recommended */
    RECOMMENDABLE(9),
    /** LDA topic assignments */
    LDA(10),
    /** The dedupe signature of the document */
    DEDUPE_DOC_SIG(11),
    /** The dedupe CRC of the document */
    DEDUPE_CRC(12),
    /** The document length calculated by the dedupe algorithm */
    DEDUPE_DOC_LENGTH(13),
    /** Source of Document * */
    SOURCE(14),
    /** Synopsis of Document */
    SYNOPSIS(15),
    /** Flag indicated whether document is TopNews or not * */
    TOPNEWS(16),
    /** Original Source of article * */
    ARTICLESOURCE(17),
    /** URL link back to original NewsArticle * */
    SOURCEURL(18),

    /** The custer ID that a document belongs to* */
    CLUSTER_TAG(19),
    /** **/
    FUZZY_TAG(20),
    /** **/
    SIMPLE_CLUSTER_TAG(21),

    /** A unique ID assigned by the clustering module * */
    CLUSTER_UNIQUE_ID(22),

    /** Any organization like a sports team or a government body. Not necessarily a company */
    ORGANIZATION(23),

    /** Media and corresponding Mediatype extracted from MediaExpress documents */
    MEDIA(24),

    MEDIATYPE(25),

    SMART_TERM(26),

    /** An Org. Authority ID * */
    OAID(27),

    /** RCS codes assigned by CaRE */
    CARE_RCS(28),

    /** Smart Terms assigned by CaRE */
    CARE_SMART_TERM(29),

    /** RIC Code generated by Calais. */
    CALAIS_RIC(30),

    /** RCS Code generated by Calais. */
    CALAIS_RCS(31),

    /** Plain Text code generated by Calais. */
    CALAIS_TEXT(32),

    /** A Reuters Instrument Code */
    RIC(33),

    /** A Reuters Classification System code */
    RCS(34),

    /** */
    HIERARCHICAL_CLUSTER_TAG(35),

    /** */
    CHANNEL_CLUSTER_TAG(36),

    TOP_CLUSTERS_TAG(37),

    TOP_CLUSTER_SCORES_TAG(38),

    FUZZY_MATCH_SCORE(39),

    TOP_HIERARCHICAL_CLUSTERS_TAG(40),

    TOP_HIERARCHICAL_CLUSTER_SCORES_TAG(41),

    /** The score returned from the search engine for a candidate document */
    SEARCH_SCORE(42),

    /** Product Codes as specified in Reuters Content */
    PRODUCT_CODE(43),

    /** Original Story ID for Professional Docs (perhaps MeidaExpressDocs aswell) */
    PNAC_ID(44),

    /** Placeholder for a custom property type */
    CUSTOM_A(-1),
    /** Placeholder for a custom property type */
    CUSTOM_B(-2),
    /** Placeholder for a custom property type */
    CUSTOM_C(-3),
    /** Placeholder for a custom property type */
    CUSTOM_D(-4),
    /** Placeholder for a custom property type */
    CUSTOM_E(-5),
    /** Placeholder for a custom property type */
    CUSTOM_F(-6),
    /** Placeholder for a custom property type */
    CUSTOM_G(-7),
    /** Placeholder for a custom property type */
    CUSTOM_H(-8),
    /** Placeholder for a custom property type */
    CUSTOM_I(-9),
    /** Placeholder for a custom property type */
    CUSTOM_J(-10);

    private int id;

    PropertyType(int i)
    {
        id = i;
    }

    public int getId()
    {
        return id;
    }

    /**
     * Returns the PropertyType for the given id.
     *
     * @param id the id of the enum
     *
     * @return the FeatureType that represents the id
     *
     * @throws IllegalArgumentException if there is no enum represented by id
     */
    public static PropertyType lookupById(int id) throws IllegalArgumentException
    {
        PropertyType type = lookupMap.get(id);
        if (type == null)
        {
            throw new IllegalArgumentException("No PropertyType found for id:" + id);
        }
        return type;
    }

    private static Map<Integer, PropertyType> lookupMap;

    static
    {
        lookupMap = new HashMap<Integer, PropertyType>();
        for (PropertyType num : PropertyType.values())
        {
            PropertyType existing = lookupMap.put(num.getId(), num);
            if (existing != null)
            {
                throw new IllegalStateException("PropertyType " + num + " and " + existing + " have the same ID.  This is an error");
            }
        }
    }
}
