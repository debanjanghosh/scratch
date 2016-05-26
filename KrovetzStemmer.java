/*
 * Copyright 2011: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util ;

import java.util.Map;

/** @author  */
public class KrovetzStemmer implements Stemmer
{
    private final static int MAX_WORD_LENGTH = 25;

    private final static int sInc = 50;

    private Map<String, DictionaryEntry> dictionary;

    private char[] mB; // buffer

    private int mI; // offset into m_b
    private int mJ; // pointer
    private int mK; // pointer

    static class DictionaryEntry
    {
        public DictionaryEntry(String root, boolean exception)
        {
            this.root = root;
            this.exception = exception;
        }

        boolean exception;

        String root;
    }

    public KrovetzStemmer()
    {
        mB = new char[sInc];
        mI = 0;
        mJ = 0;
    }

    public String stemWord(String word)
    {
    //	word = "talking" ;
        String stem = "";
        String localWord = word;
        if (localWord == null)
        {
            return stem;
        }
        localWord = localWord.trim();
        if (localWord.length() == 0)
        {
            return stem;
        }
        boolean needToStem = true;
        mB = new char[sInc];
        mI = 0;
        mJ = 0;

        char[] term = localWord.toCharArray();
        mK = localWord.length() - 1;
        /*
         * if the word is too long or too short, or not entirely alphabetic,
         * just lowercase copy it into stem and return
         */
        if ((mK <= 2 - 1) || (mK >= MAX_WORD_LENGTH - 1))
        {
            needToStem = false;
        }
        else
        {
            for (int i = 0; i < term.length; i++)
            {
                // 8 bit characters can be a problem on windows
                if (!Character.isLetter(term[i]))
                {
                    needToStem = false;
                    break;
                }
            }
        }

        if (!needToStem)
        {
            return localWord.toLowerCase();
        }

        DictionaryEntry dep = null;

        // skip direct mapping

        for (int i = 0; i < term.length; i++)
        {
            add(Character.toLowerCase(term[i]));
        }

        while (true)
        {
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            plural();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            past_tense();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            aspect();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ity_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ness_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ion_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            er_and_or_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ly_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            al_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ive_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ize_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ment_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ble_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ism_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ic_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            ncy_endings();
            dep = getdep(mB);
            if (dep != null)
            {
                break;
            }
            nce_endings();
            dep = getdep(mB);
            break;
        }

        if (dep != null)
        {
            if (dep.root.length() > 0)
            {
                stem = dep.root;
            }
            else
            {
                stem = arrayToString(mB);
            }
        }
        else
        {
            stem = localWord.toLowerCase();
        }

        return stem;
    }

    /**
     * Add a character to the word being stemmed. When you are finished adding
     * characters, you can call stem(void) to stem the word.
     *
     * @param ch character to be added
     */
    private void add(char ch)
    {
        if (mI == mB.length)
        {
            char[] newB = new char[mI + sInc];
            for (int c = 0; c < mI; c++)
            {
                newB[c] = mB[c];
            }
            mB = newB;
        }
        mB[mI++] = ch;
    }

    /**
     * Adds wLen characters to the word being stemmed contained in a portion of
     * a char[] array. This is like repeated calls of add(char ch), but faster.
     *
     * @param w    character array of characters to add
     * @param wLen integer number of characters to add
     */
    private void add(char[] w, int wLen)
    {
        if (mI + wLen >= mB.length)
        {
            char[] newB = new char[mI + wLen + sInc];
            for (int c = 0; c < mI; c++)
            {
                newB[c] = mB[c];
            }
            mB = newB;
        }

        for (int c = 0; c < wLen; c++)
        {
            mB[mI++] = w[c];
        }
    }

    private String arrayToString(char[] array)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] != '\0')
            {
                sb.append(array[i]);
            }
            else
            {
                break;
            }
        }
        return new String(sb);
    }

    private boolean lookup(char[] key)
    {
        return (getdep(key) != null);
    }

    private final char final_c()
    {
        return mB[mK];
    }

    private final char penult_c()
    {
        return mB[mK - 1];
    }

    private final int wordlength()
    {
        return mK + 1;
    }

    private final int stemlength()
    {
        return mJ + 1;
    }

    private final boolean cons(int i)
    {
        switch (mB[i])
        {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            case 'y':
                return (i == 0)
                       ? true
                       : !cons(i - 1);
            default:
                return true;
        }
    }

    /* vowelinstem() is true <=> 0,...m_j contains a vowel */

    private final boolean vowelinstem()
    {
        int i;
        for (i = 0; i < stemlength(); i++)
        {
            if (!cons(i))
            {
                return true;
            }
        }
        return false;
    }

    /* doublec(m_j) is true <=> m_j,(m_j-1) contain a double consonant. */

    private final boolean doublec(int j)
    {
        if (j < 1)
        {
            return false;
        }
        if (mB[mJ] != mB[mJ - 1])
        {
            return false;
        }
        return cons(mJ);
    }

    /*
     * Passing the length of str is awkward, but important for performance.
     * Since str is always a string constant, we can define a macro ends_in (see
     * the macro section of this module) which takes str and determines its
     * length at compile time. Note that str must therefore no longer be padded
     * with spaces in the calls to ends_in (as it was in the original version of
     * this code).
     */
    // modified by HMS 08-29-03
    private final boolean ends(String s)
    {
        int l = s.length();
        int o = mK - l + 1;
        // if (o < 0) return false;
        if (l > mK)
        {
            return false;
        }
        for (int i = 0; i < l; i++)
        {
            if (mB[o + i] != s.charAt(i))
            {
                mJ = mK;
                return false;
            }
        }
        mJ = mK - l;
        return true;
    }

    /*
     * setto(s) sets (m_j+1),...m_k to the characters in the string s,
     * readjusting m_k.
     */

    private final void setto(String s)
    {
        int l = s.length();
        int o = mJ + 1;
        for (int i = 0; i < l; i++)
        {
            mB[o + i] = s.charAt(i);
        }
        mK = mJ + l;
        mB[mK + 1] = '\0';
    }

    private DictionaryEntry getdep(char[] word)
    {
        String str = arrayToString(word);
        if (str.length() <= 0)
        {
            return null;
        }
        else
        {
            return (DictionaryEntry) dictionary.get(str);
        }
    }

    /* convert plurals to singular form, and `-ies' to `y' */

    private void plural()
    {
        if (final_c() == 's')
        {
            if (ends("ies"))
            {
                mB[mJ + 3] = '\0';
                mK--;
                if (lookup(mB)) /* ensure calories -> calorie */
                {
                    return;
                }
                mK++;
                mB[mJ + 3] = 's';
                setto("y");
            }
            else if (ends("es"))
            {
                /* try just removing the "s" */
                mB[mJ + 2] = '\0';
                mK--;

                /*
                 * note: don't check for exceptions here. So, `aides' -> `aide',
                 * but `aided' -> `aid'. The exception for double s is used to
                 * prevent crosses -> crosse. This is actually correct if
                 * crosses is a plural noun (a type of racket used in lacrosse),
                 * but the verb is much more common
                 */

                if ((lookup(mB))
                    && !((mB[mJ] == 's') && (mB[mJ - 1] == 's')))
                {
                    return;
                }

                /* try removing the "es" */

                mB[mJ + 1] = '\0';
                mK--;
                if (lookup(mB))
                {
                    return;
                }

                /* the default is to retain the "e" */
                mB[mJ + 1] = 'e';
                mB[mJ + 2] = '\0';
                mK++;
                return;
            }
            else
            {
                if (wordlength() > 3 && penult_c() != 's' && !ends("ous"))
                {
                    /*
                     * unless the word ends in "ous" or a double "s", remove the
                     * final "s"
                     */
                    mB[mK] = '\0';
                    mK--;
                }
            }
        }
    }

    /* convert past tense (-ed) to present, and `-ied' to `y' */

    private void past_tense()
    {
        /*
         * Handle words less than 5 letters with a direct mapping This prevents
         * (fled -> fl).
         */

        if (wordlength() <= 4)
        {
            return;
        }

        if (ends("ied"))
        {
            mB[mJ + 3] = '\0';
            mK--;
            if (lookup(mB)) /*
                                  * we almost always want to convert -ied to -y,
                                  * but
                                  */
            {
                return; /* this isn't true for short words (died->die) */
            }
            mK++; /* I don't know any long words that this applies to, */
            mB[mJ + 3] = 'd'; /* but just in case... */
            setto("y");
            return;
        }

        /* the vowelinstem() is necessary so we don't stem acronyms */
        if (ends("ed") && vowelinstem())
        {
            /* see if the root ends in `e' */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;

            DictionaryEntry dep = getdep(mB);
            if (dep != null)
            {
                if (!(dep.exception)) /*
                                         * if it's in the dictionary and not an
                                         * exception
                                         */
                {
                    return;
                }
            }

            /* try removing the "ed" */
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }

            /*
             * try removing a doubled consonant. if the root isn't found in the
             * dictionary, the default is to leave it doubled. This will
             * correctly capture `backfilled' -> `backfill' instead of
             * `backfill' -> `backfille', and seems correct most of the time
             */

            if (doublec(mK))
            {
                mB[mK] = '\0';
                mK--;
                if (lookup(mB))
                {
                    return;
                }
                mB[mK + 1] = mB[mK];
                mK++;
                return;
            }

            /* if we have a `un-' prefix, then leave the word alone */
            /* (this will sometimes screw up with `under-', but we */
            /* will take care of that later) */

            if ((mB[0] == 'u') && (mB[1] == 'n'))
            {
                mB[mK + 1] = 'e';
                mB[mK + 2] = 'd';
                mK = mK + 2;
                return;
            }

            /*
             * it wasn't found by just removing the `d' or the `ed', so prefer
             * to end with an `e' (e.g., `microcoded' -> `microcode').
             */

            mB[mJ + 1] = 'e';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            return;
        }
    }

    /* handle `-ing' endings */

    private void aspect()
    {
        /*
         * handle short words (aging -> age) via a direct mapping. This prevents
         * (thing -> the) in the version of this routine that ignores
         * inflectional variants that are mentioned in the dictionary (when the
         * root is also present)
         */

        if (wordlength() <= 5)
        {
            return;
        }

        /* the vowelinstem() is necessary so we don't stem acronyms */
        if (ends("ing") && vowelinstem())
        {

            /* try adding an `e' to the stem and check against the dictionary */
            mB[mJ + 1] = 'e';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;

            DictionaryEntry dep = getdep(mB);
            if (dep != null)
            {
                if (!(dep.exception)) /*
                                         * if it's in the dictionary and not an
                                         * exception
                                         */
                {
                    return;
                }
            }

            /* adding on the `e' didn't work, so remove it */
            mB[mK] = '\0';
            mK--; /* note that `ing' has also been removed */

            if (lookup(mB))
            {
                return;
            }

            /* if I can remove a doubled consonant and get a word, then do so */
            if (doublec(mK))
            {
                mK--;
                mB[mK + 1] = '\0';
                if (lookup(mB))
                {
                    return;
                }
                mB[mK + 1] = mB[mK]; /* restore the doubled consonant */

                /* the default is to leave the consonant doubled */
                /* (e.g.,`fingerspelling' -> `fingerspell'). Unfortunately */
                /*
                 * `bookselling' -> `booksell' and `mislabelling' ->
                 * `mislabell').
                 */
                /*
                 * Without making the algorithm significantly more complicated,
                 * this
                 */
                /* is the best I can do */
                mK++;
                return;
            }

            /*
             * the word wasn't in the dictionary after removing the stem, and
             * then checking with and without a final `e'. The default is to add
             * an `e' unless the word ends in two consonants, so `microcoding'
             * -> `microcode'. The two consonants restriction wouldn't normally
             * be necessary, but is needed because we don't try to deal with
             * prefixes and compounds, and most of the time it is correct (e.g.,
             * footstamping -> footstamp, not footstampe; however, decoupled ->
             * decoupl). We can prevent almost all of the incorrect stems if we
             * try to do some prefix analysis first
             */

            if (cons(mJ) && cons(mJ - 1))
            {
                mK = mJ;
                mB[mK + 1] = '\0';
                return;
            }

            mB[mJ + 1] = 'e';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            return;
        }
    }

    /* handle some derivational endings */

    /*
     * this routine deals with -ion, -ition, -ation, -ization, and -ication. The
     * -ization ending is always converted to -ize
     */

    private void ion_endings()
    {
        int old_k = mK;

        if (ends("ization"))
        { /*
           * the -ize ending is very productive, so simply accept it as the root
           */
            mB[mJ + 3] = 'e';
            mB[mJ + 4] = '\0';
            mK = mJ + 3;
            return;
        }

        if (ends("ition"))
        {
            mB[mJ + 1] = 'e';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB)) /*
                                  * remove -ition and add `e', and check against
                                  * the dictionary
                                  */
            {
                return; /* (e.g., definition->define, opposition->oppose) */
            }

            /* restore original values */
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 't';
            mK = old_k;
        }

        if (ends("ation"))
        {
            mB[mJ + 3] = 'e';
            mB[mJ + 4] = '\0';
            mK = mJ + 3;
            if (lookup(mB)) /*
                                  * remove -ion and add `e', and check against
                                  * the dictionary
                                  */
            {
                return; /* (elmination -> eliminate) */
            }

            mB[mJ + 1] = 'e'; /*
                                 * remove -ation and add `e', and check against
                                 * the dictionary
                                 */
            mB[mJ + 2] = '\0'; /* (allegation -> allege) */
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = '\0'; /*
                                  * just remove -ation (resignation->resign) and
                                  * check dictionary
                                  */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }

            /* restore original values */
            mB[mJ + 1] = 'a';
            mB[mJ + 2] = 't';
            mB[mJ + 3] = 'i';
            mB[mJ + 4] = 'o'; /*
                                 * no need to restore m_b[m_j+5] (n); it was
                                 * never changed
                                 */
            mK = old_k;
        }

        /*
         * test -ication after -ation is attempted (e.g.,
         * `complication->complicate' rather than `complication->comply')
         */

        if (ends("ication"))
        {
            mB[mJ + 1] = 'y';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB)) /*
                                  * remove -ication and add `y', and check
                                  * against the dictionary
                                  */
            {
                return; /* (e.g., amplification -> amplify) */
            }

            /* restore original values */
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 'c';
            mK = old_k;
        }

        if (ends("ion"))
        {
            mB[mJ + 1] = 'e';
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB)) /*
                                  * remove -ion and add `e', and check against
                                  * the dictionary
                                  */
            {
                return;
            }

            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB)) /*
                                  * remove -ion, and if it's found, treat that
                                  * as the root
                                  */
            {
                return;
            }

            /* restore original values */
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 'o';
            mK = old_k;
        }

        return;
    }

    /*
     * this routine deals with -er, -or, -ier, and -eer. The -izer ending is
     * always converted to -ize
     */

    private void er_and_or_endings()
    {
        int old_k = mK;

        char word_char; /* so we can remember if it was -er or -or */

        if (ends("izer"))
        { /* -ize is very productive, so accept it as the root */
            mB[mJ + 4] = '\0';
            mK = mJ + 3;
            return;
        }

        if (ends("er") || ends("or"))
        {
            word_char = mB[mJ + 1];
            if (doublec(mJ))
            {
                mB[mJ] = '\0';
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = mB[mJ - 1]; /* restore the doubled consonant */
            }

            if (mB[mJ] == 'i')
            { /* do we have a -ier ending? */
                mB[mJ] = 'y';
                mB[mJ + 1] = '\0';
                mK = mJ;
                if (lookup(mB)) /* yes, so check against the dictionary */
                {
                    return;
                }
                mB[mJ] = 'i'; /* restore the endings */
                mB[mJ + 1] = 'e';
            }

            if (mB[mJ] == 'e')
            { /* handle -eer */
                mB[mJ] = '\0';
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = 'e';
            }

            mB[mJ + 2] = '\0'; /* remove the -r ending */
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = '\0'; /* try removing -er/-or */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'e'; /* try removing -or and adding -e */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = word_char; /* restore the word to the way it was */
            mB[mJ + 2] = 'r';
            mK = old_k;
        }

    }

    /*
     * this routine deals with -ly endings. The -ally ending is always converted
     * to -al Sometimes this will temporarily leave us with a non-word (e.g.,
     * heuristically maps to heuristical), but then the -al is removed in the
     * next step.
     */

    private void ly_endings()
    {
        int old_k = mK;

        if (ends("ly"))
        {
            mB[mJ + 2] = 'e'; /* try converting -ly to -le */
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 2] = 'y';

            mB[mJ + 1] = '\0'; /* try just removing the -ly */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            if ((mB[mJ - 1] == 'a') && (mB[mJ] == 'l')) /*
                                                               * always convert
                                                               * -ally to -al
                                                               */
            {
                return;
            }
            mB[mJ + 1] = 'l';
            mK = old_k;

            if ((mB[mJ - 1] == 'a') && (mB[mJ] == 'b'))
            { /*
               * always convert -ably to -able
               */
                mB[mJ + 2] = 'e';
                mK = mJ + 2;
                return;
            }

            if (mB[mJ] == 'i')
            { /* e.g., militarily -> military */
                mB[mJ] = 'y';
                mB[mJ + 1] = '\0';
                mK = mJ;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = 'i';
                mB[mJ + 1] = 'l';
                mK = old_k;
            }

            mB[mJ + 1] = '\0'; /* the default is to remove -ly */
            mK = mJ;
        }
        return;
    }

    /*
     * this routine deals with -al endings. Some of the endings from the
     * previous routine are finished up here.
     */

    private void al_endings()
    {
        int old_k = mK;

        if (ends("al"))
        {
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB)) /* try just removing the -al */
            {
                return;
            }

            if (doublec(mJ))
            { /* allow for a doubled consonant */
                mB[mJ] = '\0';
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = mB[mJ - 1];
            }

            mB[mJ + 1] = 'e'; /* try removing the -al and adding -e */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'u'; /* try converting -al to -um */
            mB[mJ + 2] = 'm'; /* (e.g., optimal - > optimum ) */
            mK = mJ + 2;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'a'; /* restore the ending to the way it was */
            mB[mJ + 2] = 'l';
            mB[mJ + 3] = '\0';
            mK = old_k;

            if ((mB[mJ - 1] == 'i') && (mB[mJ] == 'c'))
            {
                mB[mJ - 1] = '\0'; /* try removing -ical */
                mK = mJ - 2;
                if (lookup(mB))
                {
                    return;
                }

                mB[mJ - 1] = 'y'; /*
                                     * try turning -ical to -y (e.g.,
                                     * bibliographical)
                                     */
                mB[mJ] = '\0';
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }

                mB[mJ - 1] = 'i';
                mB[mJ] = 'c';
                mB[mJ + 1] = '\0'; /*
                                      * the default is to convert -ical to -ic
                                      */
                mK = mJ;
                return;
            }

            if (mB[mJ] == 'i')
            { /* sometimes -ial endings should be removed */
                mB[mJ] = '\0'; /* (sometimes it gets turned into -y, but we */
                mK = mJ - 1; /* aren't dealing with that case for now) */
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = 'i';
                mK = old_k;
            }

        }
        return;
    }

    /*
     * this routine deals with -ive endings. It normalizes some of the -ative
     * endings directly, and also maps some -ive endings to -ion.
     */

    private void ive_endings()
    {
        int old_k = mK;

        if (ends("ive"))
        {
            mB[mJ + 1] = '\0'; /* try removing -ive entirely */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'e'; /* try removing -ive and adding -e */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 'v';

            if ((mB[mJ - 1] == 'a') && (mB[mJ] == 't'))
            {
                mB[mJ - 1] = 'e'; /* try removing -ative and adding -e */
                mB[mJ] = '\0'; /* (e.g., determinative -> determine) */
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ - 1] = '\0'; /* try just removing -ative */
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ - 1] = 'a';
                mB[mJ] = 't';
                mK = old_k;
            }

            /* try mapping -ive to -ion (e.g., injunctive/injunction) */
            mB[mJ + 2] = 'o';
            mB[mJ + 3] = 'n';
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 2] = 'v'; /* restore the original values */
            mB[mJ + 3] = 'e';
            mK = old_k;
        }
        return;
    }

    /* this routine deals with -ize endings. */

    private void ize_endings()
    {
        int old_k = mK;

        if (ends("ize"))
        {
            mB[mJ + 1] = '\0'; /* try removing -ize entirely */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'i';

            if (doublec(mJ))
            { /* allow for a doubled consonant */
                mB[mJ] = '\0';
                mK = mJ - 1;
                if (lookup(mB))
                {
                    return;
                }
                mB[mJ] = mB[mJ - 1];
            }

            mB[mJ + 1] = 'e'; /* try removing -ize and adding -e */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 'z';
            mK = old_k;
        }
        return;
    }

    /* this routine deals with -ment endings. */

    private void ment_endings()
    {
        int old_k = mK;

        if (ends("ment"))
        {
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'm';
            mK = old_k;
        }
        return;
    }

    /*
     * this routine deals with -ity endings. It accepts -ability, -ibility, and
     * -ality, even without checking the dictionary because they are so
     * productive. The first two are mapped to -ble, and the -ity is remove for
     * the latter
     */

    private void ity_endings()
    {
        int old_k = mK;

        if (ends("ity"))
        {
            mB[mJ + 1] = '\0'; /* try just removing -ity */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'e'; /* try removing -ity and adding -e */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ + 1] = 'i';
            mB[mJ + 2] = 't';
            mK = old_k;

            /*
             * the -ability and -ibility endings are highly productive, so just
             * accept them
             */
            if ((mB[mJ - 1] == 'i') && (mB[mJ] == 'l'))
            {
                mB[mJ - 1] = 'l'; /* convert to -ble */
                mB[mJ] = 'e';
                mB[mJ + 1] = '\0';
                mK = mJ;
                return;
            }

            /* ditto for -ivity */
            if ((mB[mJ - 1] == 'i') && (mB[mJ] == 'v'))
            {
                mB[mJ + 1] = 'e'; /* convert to -ive */
                mB[mJ + 2] = '\0';
                mK = mJ + 1;
                return;
            }

            /* ditto for -ality */
            if ((mB[mJ - 1] == 'a') && (mB[mJ] == 'l'))
            {
                mB[mJ + 1] = '\0';
                mK = mJ;
                return;
            }

            /*******************************************************************
             * if the root isn't in the dictionary, and the variant *is there,
             * then use the variant. This allows `immunity'->`immune', but
             * prevents `capacity'->`capac'. If neither the variant nor the root
             * form are in the dictionary, then remove the ending as a default
             */

            if (lookup(mB))
            {
                return;
            }

            /* the default is to remove -ity altogether */
            mB[mJ + 1] = '\0';
            mK = mJ;
            return;
        }
    }

    /* handle -able and -ible */

    private void ble_endings()
    {
        int old_k = mK;
        char word_char;

        if (ends("ble"))
        {
            if (!((mB[mJ] == 'a') || (mB[mJ] == 'i')))
            {
                return;
            }
            word_char = mB[mJ];
            mB[mJ] = '\0'; /* try just removing the ending */
            mK = mJ - 1;
            if (lookup(mB))
            {
                return;
            }
            if (doublec(mK))
            { /* allow for a doubled consonant */
                mB[mK] = '\0';
                mK--;
                if (lookup(mB))
                {
                    return;
                }
                mK++;
                mB[mK] = mB[mK - 1];
            }
            mB[mJ] = 'e'; /* try removing -a/ible and adding -e */
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB))
            { return; }

            mB[mJ] = 'a'; /* try removing -able and adding -ate */
            mB[mJ + 1] = 't'; /* (e.g., compensable/compensate) */
            mB[mJ + 2] = 'e';
            mB[mJ + 3] = '\0';
            mK = mJ + 2;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ] = word_char; /* restore the original values */
            mB[mJ + 1] = 'b';
            mB[mJ + 2] = 'l';
            mB[mJ + 3] = 'e';
            mK = old_k;
        }
        return;
    }

    /* handle -ness */

    private void ness_endings()
    {
        if (ends("ness"))
        { /*
           * this is a very productive endings, so just accept it
           */
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (mB[mJ] == 'i')
            {
                mB[mJ] = 'y';
            }
        }
        return;
    }

    /* handle -ism */

    private void ism_endings()
    {
        if (ends("ism"))
        { /*
           * this is a very productive ending, so just accept it
           */
            mB[mJ + 1] = '\0';
            mK = mJ;
        }
        return;
    }

    /*
     * handle -ic endings. This is fairly straightforward, but this is also the
     * only place we try *expanding* an ending, -ic -> -ical. This is to handle
     * cases like `canonic' -> `canonical'
     */

    private void ic_endings()
    {
        if (ends("ic"))
        {
            mB[mJ + 3] = 'a'; /* try converting -ic to -ical */
            mB[mJ + 4] = 'l';
            mB[mJ + 5] = '\0';
            mK = mJ + 4;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'y'; /* try converting -ic to -y */
            mB[mJ + 2] = '\0';
            mK = mJ + 1;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'e'; /* try converting -ic to -e */
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = '\0'; /* try removing -ic altogether */
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 1] = 'i'; /* restore the original ending */
            mB[mJ + 2] = 'c';
            mB[mJ + 3] = '\0';
            mK = mJ + 2;
        }
        return;
    }

    /* handle -ency and -ancy */

    private void ncy_endings()
    {
        if (ends("ncy"))
        {
            if (!((mB[mJ] == 'e') || (mB[mJ] == 'a')))
            {
                return;
            }
            mB[mJ + 2] = 't'; /* try converting -ncy to -nt */
            mB[mJ + 3] = '\0'; /* (e.g., constituency -> constituent) */
            mK = mJ + 2;

            if (lookup(mB))
            {
                return;
            }

            mB[mJ + 2] = 'c'; /* the default is to convert it to -nce */
            mB[mJ + 3] = 'e';
            mK = mJ + 3;
        }
        return;
    }

    /* handle -ence and -ance */

    private void nce_endings()
    {
        int old_k = mK;

        char word_char;

        if (ends("nce"))
        {
            if (!((mB[mJ] == 'e') || (mB[mJ] == 'a')))
            {
                return;
            }
            word_char = mB[mJ];
            mB[mJ] = 'e'; /* try converting -e/ance to -e (adherance/adhere) */
            mB[mJ + 1] = '\0';
            mK = mJ;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ] = '\0'; /*
                              * try removing -e/ance altogether
                              * (disappearance/disappear)
                              */
            mK = mJ - 1;
            if (lookup(mB))
            {
                return;
            }
            mB[mJ] = word_char; /* restore the original ending */
            mB[mJ + 1] = 'n';
            mK = old_k;
        }
        return;
    }

    public void setDictionary(Map<String, DictionaryEntry> dictionary)
    {
        this.dictionary = dictionary;
    }
}
