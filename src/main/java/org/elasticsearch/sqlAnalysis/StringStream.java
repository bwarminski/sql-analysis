package org.elasticsearch.sqlAnalysis;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bwarminski on 8/19/15.
 */
public class StringStream {
    private int pos = 0;
    private int start = 0;
    private String string;
    private char lastChar = ' ';

    public StringStream(String string) {
        this.string = string;
    }

    public int getPos() {
        return pos;
    }

    public int getStart() {
        return start;
    }

    public CharSequence getString() {
        return string;
    }

    public char getChar() {
        Preconditions.checkState(pos > 0, "No previous char available");
        return lastChar;
    }

    public int setStart() {
        start = pos;
        return start;
    }

    public boolean eol() { return pos >= string.length(); }
    public boolean sol() { return pos == 0; }
    public char peek() { return this.string.charAt(pos); }
    public boolean next() {
        if (eol()) return false;
        lastChar = this.string.charAt(pos++);
        return true;
    }
    public boolean eat(Pattern match) {
        if (eol()) return false;
        CharSequence ch = string.subSequence(pos,pos+1);
        if (match.matcher(ch).find()) { ++this.pos; lastChar = ch.charAt(0); return true; }
        return false;
    }
    public boolean eat(char match) {
        if (peek() == match) {
            ++this.pos;
            lastChar = match;
            return true;
        }
        return false;
    }
    public boolean eatNot(char match) {
        if (peek() != match) {
            ++this.pos;
            lastChar = match;
            return true;
        }
        return false;
    }
    public boolean eatWhile(Pattern match) {
        int start = pos;
        while (this.eat(match)) {}
        return this.pos > start;
    }
    public boolean eatWhile(char match) {
        int start = pos;
        while (this.eat(match)) {}
        return this.pos > start;
    }

    private static final Pattern SPACE = Pattern.compile("[\\s\\u00a0]");
    public boolean eatSpace() {
        return eatWhile(SPACE);
    }
    public void skipToEnd() {this.pos = this.string.length();}
    public boolean skipTo(char ch) {
        int idx = this.string.substring(pos).indexOf(ch);
        if (idx >= 0) idx += pos;
        if (idx >= 0) {
            this.pos = idx;
            return true;
        }
        return false;
    }
    public void backUp(int n) {
        this.pos -= n;
    }

    public Optional<Matcher> match(Pattern pattern) {
        return match(pattern, true);
    }

    public Optional<Matcher> match(Pattern pattern, boolean consume) {
        Matcher matcher = pattern.matcher(string.substring(pos));
        if (matcher.find()) {
            if (matcher.start() > 0) return Optional.absent();
            if (consume) this.pos += matcher.group().length();
            return Optional.of(matcher);
        }
        return Optional.absent();
    }

    public boolean match(String pattern, boolean consume, boolean caseInsensitive) {
        if (caseInsensitive ? pattern.equalsIgnoreCase(string.substring(pos)) : pattern.equals(string.substring(pos))) {
            if (consume) this.pos += pattern.length();
            return true;
        }
        return false;
    }

    public String current() {return this.string.substring(start, pos); }

}
