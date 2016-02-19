package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes SQL input
 */
public class BasicSQLAnalyzer extends Analyzer {
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new BasicSQLTokenizer());
    }

    public static void main(String[] args) throws IOException {

        List<String> queries = ImmutableList.of( "" ); // Your sql here

        TestHarness tester = new TestHarness(new Supplier<Analyzer>() {
            @Override
            public Analyzer get() {
                return new BasicSQLAnalyzer();
            }
        }, true);
        tester.run(queries);
    }
}
