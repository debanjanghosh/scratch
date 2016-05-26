/*
 * Copyright 2011: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.util ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *
 */

public class DictionaryReaderFactoryBean 
{

    private List<File> headwordFiles;
    private List<File> conflationsFiles;

    public DictionaryReaderFactoryBean(List<File> headwordFiles, List<File> conflationsFiles)
    {
        this.headwordFiles = headwordFiles;
        this.conflationsFiles = conflationsFiles;
    }

    public Map<String, KrovetzStemmer.DictionaryEntry> getDictionary() throws Exception
    {

        Map<String, KrovetzStemmer.DictionaryEntry> dictionary = new HashMap<String, KrovetzStemmer.DictionaryEntry>();


        for (File headwordFile : headwordFiles)
        {
            BufferedReader br = new BufferedReader(new FileReader(headwordFile));
            try
            {
                String line = br.readLine();
                while (line != null)
                {
                    String word = line.trim();
                    dictionary.put(word, new KrovetzStemmer.DictionaryEntry("", false));
                    line = br.readLine();
                }
            }
            finally
            {
                try { br.close(); } catch (IOException ignored) { }
            }
        }

        for (File conflationsFile : conflationsFiles)
        {
            BufferedReader br = new BufferedReader(new FileReader(conflationsFile));
            try
            {
                String line = br.readLine();
                while (line != null)
                {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    if (st.countTokens() == 2)
                    {
                        String variant = st.nextToken();
                        String headword = st.nextToken();
                        dictionary.put(variant, new KrovetzStemmer.DictionaryEntry(headword, false));
                    }
                    line = br.readLine();
                }

            }
            finally
            {
                try { br.close(); } catch (IOException ignored) { }
            }
        }
        return dictionary;
    }

   
}
