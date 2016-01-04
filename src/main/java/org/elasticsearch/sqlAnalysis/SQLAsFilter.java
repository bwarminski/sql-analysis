package org.elasticsearch.sqlAnalysis;

import com.google.common.collect.ImmutableSet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filters out literals following the SQL "AS" keyword including the keyword itself
 */
public class SQLAsFilter extends FilteringTokenFilter {
    private final CharTermAttribute term = addAttribute(CharTermAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);
    private static final Set<String> LITERAL_OR_WORD = ImmutableSet.of(BasicSQLTokenizer.TokenTypes.LITERAL, BasicSQLTokenizer.TokenTypes.WORD);

    private boolean inAs = false;

    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected SQLAsFilter(TokenStream input) {
        super(input);
    }

    @Override
    protected boolean accept() throws IOException {
        boolean asKeyword = BasicSQLTokenizer.TokenTypes.KEYWORD.equals(type.type()) && "as".equals(term.toString());
        if (!inAs && asKeyword) {
            inAs = true;
            return false;
        }

        if (!inAs) return true;

        inAs = false;
        return !LITERAL_OR_WORD.contains(type.type());
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        inAs = false;
    }
}
