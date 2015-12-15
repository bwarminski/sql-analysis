package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by bwarminski on 8/19/15.
 */
public class BasicSQLAnalyzer extends Analyzer {
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new BasicSQLTokenizer());
    }

    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new BasicSQLAnalyzer();
        TokenStream stream = analyzer.tokenStream("SomeField", "SELECT a_1 AS \"alias_ME5EGYLSMQQGGYLUMVTW64TZ\",COUNT(*) AS \"occurrences_metric\" FROM \"fact_events\" WHERE ((name = 'Card opened') AND (client_date >= '2015-08-03' AND client_date <= '2015-09-02') AND (app_id = '56981')) GROUP BY \"alias_ME5EGYLSMQQGGYLUMVTW64TZ\" ORDER BY \"occurrences_metric\" DESC");
        stream.reset();
        while (stream.incrementToken()) {
            CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
            TypeAttribute type = stream.getAttribute(TypeAttribute.class);
            System.out.print(term.toString() + "|" + type.type() + " ");
        }
    }
}
