package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Collapses repeated instances of ?, into a single ? (assuming it's at the end of a literal filter chain)
 */
public class SQLRepeatedValueFilter extends FilteringTokenFilter {
    /**
     * Create a new {@link FilteringTokenFilter}.
     *
     * @param in the {@link TokenStream} to consume
     */
    public SQLRepeatedValueFilter(TokenStream in) {
        super(in);
    }

    private final LinkedList<String> termWindow = new LinkedList<>();
    private final CharTermAttribute term = addAttribute(CharTermAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);


    @Override
    protected boolean accept() throws IOException {
        boolean accept = true;
        if (termWindow.size() < 2) {
            accept = true;
        } else if ("?".equals(termWindow.getFirst()) && ",".equals(termWindow.getLast()) && "?".equals(term.toString()) ) {
            accept = false;
        } else if (",".equals(termWindow.getFirst()) && "?".equals(termWindow.getLast()) && ",".equals(term.toString())) {
            accept = false;
        }

        termWindow.add(term.toString());
        while (termWindow.size() > 2) termWindow.removeFirst();
        return accept;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        termWindow.clear();
    }
}
