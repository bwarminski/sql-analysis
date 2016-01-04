package org.elasticsearch.sqlAnalysis;

import com.google.common.collect.ImmutableSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.TypeTokenFilter;

/**
 * Combines a SQL tokenizer, with a SQL literal filter, Vertica COPY statement filter and repeated value filter
 */
public class SQLFingerprintAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // Tokenizer -> Comment Filter -> Literal Replacement -> collapse in, values, "as ?"
        final Tokenizer src = new BasicSQLTokenizer();
        TokenStream tok = new TypeTokenFilter(src, ImmutableSet.of(BasicSQLTokenizer.TokenTypes.COMMENT));
        tok = new SQLCopyStmtFilter(tok);
        tok = new SQLAsFilter(tok);
        tok = new SQLLiteralFilter(tok);
        tok = new SQLRepeatedValueFilter(tok);
        return  new TokenStreamComponents(src, tok);
    }
}
