package com.research.course.debate.util;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class StemTokenizerFactory extends ModifyTokenTokenizerFactory
{

    static final long serialVersionUID = -6045422132691926248L;
    private KrovetzStemmer krovetzStemmer;

    public StemTokenizerFactory( TokenizerFactory factory, KrovetzStemmer krovetzStemmer )
    {
        super( factory );
        this.krovetzStemmer = krovetzStemmer;
    }

    public String modifyToken( String token )
    {
        String stem = krovetzStemmer.stemWord( token );
        return stem;
    }

}
