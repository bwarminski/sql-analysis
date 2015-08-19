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
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        return new TokenStreamComponents(new BasicSQLTokenizer(reader));
    }

    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new BasicSQLAnalyzer();
        TokenStream stream = analyzer.tokenStream("SomeField", "SELECT attribute_value_str AS \"profile_value_string\",COUNT(DISTINCT customer_id) AS \"profiles\" FROM (SELECT customer_id, attribute_type, attribute_key, attribute_value_str, version, FIRST_VALUE(version) OVER (PARTITION BY customer_id ORDER BY version ASC) AS latest_version FROM \"fact_customer_profiles\" WHERE ((profiledb_id = 64462) AND (attribute_key = 'Apps most used'))) AS inner_table WHERE ((attribute_key = 'Apps most used') AND (attribute_type = 's') AND (version = latest_version)) GROUP BY \"profile_value_string\" ORDER BY \"profiles\" DESC");

        while (stream.incrementToken()) {
            CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
            TypeAttribute type = stream.getAttribute(TypeAttribute.class);
            System.out.print(term.toString() + "|" + type.type() + " ");
        }
    }
}
