package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Optional;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;

/**
 * Created by bwarminski on 9/6/15.
 */
public abstract class AbstractStringStreamTokenizer extends Tokenizer {
    protected Optional<StringStream> stream = Optional.absent();
    protected InnerTokenizer tokenize;

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    protected final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private Reader lastReaderSeen = null;

    protected interface InnerTokenizer {
        boolean incrementToken() throws IOException;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (lastReaderSeen != input) {
            stream = Optional.of(new StringStream(fillBuffer(input)));
            lastReaderSeen = input;
        }
        stream.get().eatSpace();
        return tokenize.incrementToken();
    }

    @Override
    public void end() throws IOException {
        super.end();
        final int ofs = stream.isPresent() ? correctOffset(stream.get().getString().length()) : 0;
        offsetAtt.setOffset(ofs, ofs);
    }

    protected void markTokenFromMatch(Matcher match, String type) {
        termAtt.setEmpty().append(match.group().toLowerCase());
        offsetAtt.setOffset(correctOffset(stream.get().getPos()-match.group().length()), correctOffset(stream.get().getPos()));
        typeAtt.setType(type);
        keywordAtt.setKeyword(false);
    }

    protected void markToken(String type) {
        markToken(type, false);
    }

    private void markToken(String type, boolean keyword) {
        termAtt.setEmpty().append(stream.get().current().toLowerCase());
        offsetAtt.setOffset(correctOffset(stream.get().getStart()), correctOffset(stream.get().getPos()));
        typeAtt.setType(type);
        keywordAtt.setKeyword(keyword);
    }

    protected void markKeywordToken(String type) {
        markToken(type, true);
    }

    final char[] buffer = new char[8192];
    private String fillBuffer(Reader input) throws IOException {
        int len;
        StringBuilder sb = new StringBuilder();

        while ((len = input.read(buffer)) > 0) {
            sb.append(buffer, 0, len);
        }
        return sb.toString();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        stream = Optional.absent();
    }
}
