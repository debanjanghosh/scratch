package com.research.course.debate.util;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class NonAlphaStopTokenizerFactory extends ModifyTokenTokenizerFactory
{
    static final long serialVersionUID = -3401639068551227864L;
	public static TokenizerFactory INSTANCE;

    public NonAlphaStopTokenizerFactory( TokenizerFactory factory )
    {
        super( factory );
    }

    public String modifyToken( String token )
    {
        return stop( token )
            ? null
                : token;
    }

    public boolean stop( String token )
    {
        if ( token.length() < 2 )
        {
            return true;
        }

        for ( int i = 0; i < token.length(); ++i )
        {
            if ( Character.isLetter( token.charAt( i ) ) )
            {
                return false;
            }
        }
        return true;
    }

}

