package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

/**
 * Removes the FROM clause in vertica COPY statements
 */
public class SQLCopyStmtFilter extends TokenFilter {
    private final CharTermAttribute term = addAttribute(CharTermAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);

    private final PositionIncrementAttribute increment = addAttribute(PositionIncrementAttribute.class);

    private int skippedPositions;
    private boolean isCopy = false;
    private boolean inFrom = false;

    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected SQLCopyStmtFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        skippedPositions = 0;
        if (!input.incrementToken()) return false;

        if (!isCopy && BasicSQLTokenizer.TokenTypes.KEYWORD.equals(type.type()) && "copy".equals(term.toString())) isCopy = true;

        if (!isCopy) return true;

        if (!inFrom) {
            if ("from".equals(term.toString())) inFrom = true;
            return true;
        }

        do { skippedPositions += increment.getPositionIncrement();} while (input.incrementToken());
        return false;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        inFrom = false;
        isCopy = false;
    }

    @Override
    public void end() throws IOException {
        super.end();
        increment.setPositionIncrement(increment.getPositionIncrement() + skippedPositions);
    }
}
