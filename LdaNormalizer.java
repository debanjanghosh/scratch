/*
 * Copyright 2012: Thomson Reuters Global Resources. All Rights Reserved.
 *
 * Proprietary and Confidential information of TRGR. Disclosure, Use or
 * Reproduction without the written authorization of TRGR is prohibited
 */

package com.research.course.debate.LDA;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 *
 */

public class LdaNormalizer
{
    private static Set<String> stopWords = null;

  //  private static Logger logger = LoggerFactory.getLogger(LdaNormalizer.class);

    private LdaNormalizer()
    {

    }

    static
    {
        try
        {
            stopWords = loadDictionary("./config/lda/stopWords.lst");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param body
     *
     * @return
     */
    public static String normalizeWhiteSpace(String body)
    {
        String ret = "";

        for (String tok : body.split("\\s+"))
        {
            ret += tok + " ";
        }

        return ret.trim();
    }

    private static Set<String> loadDictionary(String stopFile) throws IOException 
    {
		// TODO Auto-generated method stub
    	Set<String> stops = new HashSet<String>() ;
    	BufferedReader reader = new BufferedReader ( new FileReader ( stopFile)) ;
    	
    	while ( true )
    	{
    		String line = reader.readLine();
    		if ( null == line )
    		{
    			break ;
    		}
    		stops.add(line.trim());
    		
    	}
    	reader.close();
		return stops;
	}

	/**
     * @param body
     * @param words
     *
     * @return
     */
    public static String cleanUpBodyText(String body, List<String> words)
    {
        String body2 = body.toLowerCase();

        body2 = body2.replaceAll("\r\n", " ");
        body2 = body2.replaceAll("\n", " ");
        body2 = body2.replaceAll("]", " ");
        body2 = body2.replaceAll("\\-\\-+", " ");
        body2 = body2.replaceAll("cdata", " ");
        body2 = body2.replaceAll("\\.\\.", " ");
        body2 = body2.replaceAll("\\&gt;", " ");
        body2 = body2.replaceAll("\\&lt;", " ");
        body2 = body2.replaceAll("\\>", " ");
        body2 = body2.replaceAll("\\<", " ");
        body2 = body2.replaceAll("\\&amp;", " ");
        body2 = body2.replaceAll("lt;", " ");

        StringBuilder newText = new StringBuilder();
        for (String tok : body2.split("\\s+|:|;|,|\\&|\\#|\\?"))
        {

            String tok2 = tok.replaceAll("^\\W+", "");
            tok2 = tok2.replaceAll("\\W+$", "");
            tok2 = tok2.replaceAll("'s$", "");
            tok2 = tok2.replaceAll("`s$", "");
            tok2 = tok2.trim();

            if (tok2.length() < 2 || tok2.matches(".*\\d+.*")
                || tok2.matches("\\W+") || tok2.length() > 15
                || tok2.startsWith("www") || tok2.startsWith("http")
                || tok2.contains("@") || tok2.contains("?")
                || tok2.endsWith(".com") || stopWords.contains(tok2))
            {
                continue;
            }

            newText.append(tok2).append(" ");
            words.add(tok2);
        }
        return newText.toString();
    }

    /**
     * @param body
     *
     * @return
     */
    public static String cleanUpBodyTextLW(String body)
    {

        String body2 = body.toLowerCase();

        body2 = body2.replaceAll("\r\n", " ");
        body2 = body2.replaceAll("\n", " ");
        body2 = body2.replaceAll("]", " ");
        body2 = body2.replaceAll("\\-\\-+", " ");
        body2 = body2.replaceAll("cdata", " ");
        body2 = body2.replaceAll("\\.\\.+", " ");
        body2 = body2.replaceAll("\\&lt;", " ");
        body2 = body2.replaceAll("\\>", " ");
        body2 = body2.replaceAll("\\<", " ");
        body2 = body2.replaceAll("\\&gt;", " ");
        body2 = body2.replaceAll("\\&amp;", " ");
        body2 = body2.replaceAll("lt;", " ");

        StringBuilder newText = new StringBuilder();
        for (String tok : body2.split("\\s+|:|;|,|\\&|\\#|\\?"))
        {

            String tok2 = tok.replaceAll("^\\W+", "");
            tok2 = tok2.replaceAll("\\W+$", "");
            tok2 = tok2.replaceAll("'s$", "");
            tok2 = tok2.replaceAll("`s$", "");
            tok2 = tok2.trim();

            if (tok2.length() < 2 || tok2.matches("\\d+")
                || tok2.matches("^\\d.*\\d$") || tok2.matches("\\W+")
                || tok2.length() > 15 || tok2.endsWith(".com")
                || stopWords.contains(tok2))
            {
                continue;
            }

            newText.append(tok2).append(" ");
            if (tok2.contains("-"))
            {
                newText.append(tok2.replaceAll("-", " ")).append(" ");
            }

        }
        body2 = newText.toString();
        return body2;
    }

    /**
     * @param in
     *
     * @return
     */
    public static String unEscapeHTML(String in)
    {
        String out = StringEscapeUtils.unescapeHtml3(in);
        out = out.replaceAll("<[\\s]*[^\\s]*[\\s]*>", "");
        out = out.replaceAll("[<\\[][^\\s]+", "");
        out = out.replaceAll("[^\\s]+[>\\]]", "");
        out = out.replaceAll("/$", "");
        return out;
    }

    /**
     * @param headline
     *
     * @return
     */
    public static String cleanHeadline(String headline)
    {
        String headline2 = headline.toLowerCase();
        headline2 = headline2.trim();

        String newText = "";

        if (headline2.startsWith("dtn") || headline2.startsWith("rtp")
            || headline2.startsWith("eu research summary")
            || headline2.contains("daily earnings")
            || headline2.contains("gmt"))
        {
            return newText;
        }

        String[] fields = headline2.split("-");

        if (fields.length == 0)
        {
            newText = headline2;
        }
        else if (fields.length == 1
                 || !(fields[0].contains("update") || fields[0].contains("brief")
                      || fields[0].contains("text")
                      || fields[0].contains("refile")
                      || fields[0].contains("interview")
                      || fields[0].contains("research alert")
                      || fields[0].contains("auto alert")
                      || fields[0].contains("instant view")
                      || fields[0].contains("top news")
                      || fields[0].contains("factbox") || fields[0]
                .contains("table")))
        {
            newText = headline2;
        }
        else if (fields.length > 1)
        {
            if (fields[1].contains("research alert") && fields.length == 3)
            {
                newText = fields[2];
            }
            else
            {
                for (int i = 1; i < fields.length; i++)
                {
                    newText += fields[i] + " ";
                }
            }
        }

        return cleanUpBodyTextLW(newText);
    }


    public static String normalizeString(String instr)
    {
        String convert;
        try
        {
            convert = new String(instr.getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (Exception ignored)
        {
            convert = instr;
        }

        char[] charsText = convert.toCharArray();

        for (int i = 0; i < charsText.length; i++)
        {
            if (!Character.isLetterOrDigit(charsText[i]))
            {
                charsText[i] = ' ';
            }
        }
        return new String(charsText);
    }
}
