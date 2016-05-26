/**
 * 
 */
package com.research.course.debate.util ;

/**
 * @author Qiang Lu
 * 
 */
public interface Stemmer
{

    /**
     * Stem a word
     * 
     * @param word
     *            word to be stemmed
     * @return word stem or empty String if invalid input.
     */
    String stemWord( String word );
}
