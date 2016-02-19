package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.TypeTokenFilter;

import java.io.IOException;
import java.util.List;

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

    public static void main(String[] args) throws IOException {
        List<String> queries = ImmutableList.of( "" ); // Your sql here

        TestHarness tester = new TestHarness(new Supplier<Analyzer>() {
            @Override
            public Analyzer get() {
                return new SQLFingerprintAnalyzer();
            }
        });
        tester.run(queries);
    }
}
