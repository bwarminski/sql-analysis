package org.elasticsearch.sqlAnalysis;

import com.google.common.collect.ImmutableSet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import org.elasticsearch.sqlAnalysis.BasicSQLTokenizer.TokenTypes;

import java.io.IOException;
import java.util.Set;

/**
 * Attempts to replace literals in SQL statements with with ?
 */
public class SQLLiteralFilter extends TokenFilter {
    private final CharTermAttribute term = addAttribute(CharTermAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);

    /* SELECT [ TOP term ] [ DISTINCT | ALL ] selectExpression [,...]
       FROM tableExpression [,...] [ WHERE expression ]
       [ GROUP BY expression [,...] ] [ HAVING expression ]
       [ { UNION [ ALL ] | MINUS | EXCEPT | INTERSECT } select ] [ ORDER BY order [,...] ]
       [ LIMIT expression [ OFFSET expression ] [ SAMPLE_SIZE rowCountInt ] ]
       [ FOR UPDATE ] */

    private final CharArraySet END_CLAUSE = new CharArraySet(ImmutableSet.of("where", "select", "group", "having", "union",
            "minus", "except", "intersect", "order", "limit", "offset"), false);

    private static final Set<String> LITERALS = ImmutableSet.of(TokenTypes.LITERAL, TokenTypes.NUMBER);
    private static final Set<String> KEYWORD_OR_LITERAL = ImmutableSet.of(TokenTypes.KEYWORD, TokenTypes.LITERAL);

    private boolean inFrom = false;
    private boolean inGroupBy = false;
    private boolean inOrderBy = false;
    private String lastType = "";

    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected SQLLiteralFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) return false;

        boolean clauseTerminator = END_CLAUSE.contains(term.buffer(), 0, term.length());
        if (clauseTerminator) {
            inFrom = false;
            inGroupBy = false;
            inOrderBy = false;
        }
        if (!inFrom && isKeyword() && "from".equals(term.toString())) inFrom = true;

        if (!inGroupBy && isKeyword() && "group".equals(term.toString())) inGroupBy = true;

        if (!inOrderBy && isKeyword() && "order".equals(term.toString())) inOrderBy = true;

        // Keep first literal after keyword or terminal

        // replace literals with ?
        if (!inFrom && !inGroupBy && !inOrderBy && !KEYWORD_OR_LITERAL.contains(lastType) && LITERALS.contains(type.type())) {
            term.setEmpty().append("?");
        }

        lastType = type.type();
        return true;
    }

    private boolean isKeyword() {
        return TokenTypes.KEYWORD.equals(type.type());
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        inFrom = false;
        inGroupBy = false;
        inOrderBy = false;
    }

}
