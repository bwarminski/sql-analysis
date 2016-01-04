package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Joiner;
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

        for (String query : queries) {
            System.out.println(query);
            Analyzer analyzer = new BasicSQLAnalyzer();
            TokenStream stream = analyzer.tokenStream("SomeField", query);
            stream.reset();
            List<String> terms = new ArrayList<>();
            while (stream.incrementToken()) {
                CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
                TypeAttribute type = stream.getAttribute(TypeAttribute.class);
// Uncomment this and out.prin below to output token types
//                System.out.print(term.toString() + "|" + type.type() + " ");
                terms.add(term.toString());
            }
//            System.out.print("\n");
            System.out.println(Joiner.on(" ").join(terms));
            System.out.println();
        }

    }
}
