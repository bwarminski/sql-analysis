package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.common.base.Optional;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bwarminski on 8/18/15.
 */
public class BasicSQLTokenizer extends Tokenizer {
    private static final Pattern NUMBER = Pattern.compile("^[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");

    private final InnerTokenizer BASE_TOKENIZER = new BaseTokenizer();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final StringBuilder str = new StringBuilder();
    private int index;
    private InnerTokenizer tokenizer = BASE_TOKENIZER;

    public BasicSQLTokenizer(AttributeFactory factory, Reader input) {
        super(factory, input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        return tokenizer.incrementToken();
    }

    @Override
    public void end() throws IOException {
        super.end();
        final int ofs = correctOffset(str.length());
        offsetAtt.setOffset(ofs, ofs);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        fillBuffer(str, input);
        index = 0;
    }

    final char[] buffer = new char[8192];
    private void fillBuffer(StringBuilder sb, Reader input) throws IOException {
        int len;
        sb.setLength(0);
        while ((len = input.read(buffer)) > 0) {
            sb.append(buffer, 0, len);
        }
    }

    private Optional<Matcher> match(Pattern pattern) {
        return match(pattern, true);
    }

    private Optional<Matcher> match(Pattern pattern, boolean consume) {
        Matcher match = pattern.matcher(str.substring(index));
        if (!match.matches() || (match.matches() && match.start() > 0)) return Optional.absent();
        if (match.matches() && consume) index += match.end() - match.start();
        return Optional.of(match);
    }

    private interface InnerTokenizer {
        boolean incrementToken() throws IOException;
    }

    private class BaseTokenizer implements InnerTokenizer {
        public boolean incrementToken() throws IOException {
            if (index >= str.length()) return false;
            clearAttributes();

            char ch = str.charAt(index);
            int charCode = (int) ch;

            if (charCode > 47 && charCode < 58) {
                int startIndex = index;
                match(NUMBER);
                termAtt.setEmpty().append(str, startIndex, index);
                offsetAtt.setOffset(correctOffset(startIndex), correctOffset(index));
                typeAtt.setType("number");
                keywordAtt.setKeyword(false);
                return true;
            } else if (ch == '\'' || ch == '"') {
                index++;

            }

            return false;
        }
    }

    private class TokenLiteralTokenizer implements InnerTokenizer {
        private final char quote;
        private boolean escaped = false;

        TokenLiteralTokenizer(char quote) {
            this.quote = quote;
        }

        public boolean incrementToken() throws IOException {
            if (index >= str.length()) return false;
            clearAttributes();

            int startIndex = index;
            while (index >= str.length()) {
                char ch = str.charAt(index);
                if (ch == quote && !escaped) {
                    tokenizer = BASE_TOKENIZER;
                    if (index > startIndex) {
                        dumpBuffer(startIndex);
                        return true;
                    }
                    index++;
                    return tokenizer.incrementToken();
                }
                escaped = !escaped && ch == '\\';
                if (Character.isWhitespace(ch)) {
                    if (index > startIndex) {
                        dumpBuffer(startIndex);
                        return true;
                    }
                }
                index++;
            }

            if (index > startIndex) {
                dumpBuffer(startIndex);
                return true;
            }
            return false;
        }

        private void dumpBuffer(int startIndex) {
            termAtt.setEmpty().append(str, startIndex, index);
            offsetAtt.setOffset(correctOffset(startIndex), correctOffset(index));
            typeAtt.setType("string");
            keywordAtt.setKeyword(false);
        }
    }
}
