package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Common logic for developer main testing
 */
public class TestHarness {
    private final Supplier<Analyzer> analyzer;
    private final boolean outputTypes;

    public TestHarness(Supplier<Analyzer> analyzer) {
        this(analyzer, false);
    }

    public TestHarness(Supplier<Analyzer> analyzer, boolean outputTypes) {
        this.analyzer = Preconditions.checkNotNull(analyzer);
        this.outputTypes = outputTypes;
    }

    public void run(List<String> queries) throws IOException {
        for (String query : queries) {
            System.out.println(query);
            Analyzer analyzer = this.analyzer.get();
            TokenStream stream = analyzer.tokenStream("SomeField", query);
            stream.reset();
            List<String> terms = new ArrayList<>();
            while (stream.incrementToken()) {
                CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
                TypeAttribute type = stream.getAttribute(TypeAttribute.class);
                if (outputTypes) System.out.print(term.toString() + "|" + type.type() + " ");
                terms.add(term.toString());
            }
            if (outputTypes) System.out.print("\n");
            System.out.println(Joiner.on(" ").join(terms));
            System.out.println();
        }
    }
}
