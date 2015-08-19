package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.common.base.Optional;
import org.elasticsearch.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bwarminski on 8/18/15.
 */
public class BasicSQLTokenizer extends Tokenizer {
    private static final Pattern NUMBER = Pattern.compile("^[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private static final Pattern TERMINAL_CHARS = Pattern.compile("^[\\(\\),\\;\\[\\]]");
    private static final Pattern IDENTIFIERS = Pattern.compile("^[a-zA-Z_]+");
    private static final Pattern OPERATOR_CHARS = Pattern.compile("^[*+\\-%<>!=&|^]");
    private static final Pattern WORD_CHARS = Pattern.compile("^[_\\w\\d]");
    private static final Pattern SURROUNDING_1 = Pattern.compile("^( )+'[^']*'");
    private static final Pattern SURROUNDING_2 = Pattern.compile("^( )+\"[^\"]*\"");

    private static final Set<String> DATE_SQL = ImmutableSet.of("date", "time", "timestamp");
    private static final Set<String> KEYWORDS = ImmutableSet.of("accessible", "action", "add", "after", "algorithm", "all", "analyze", "asensitive", "at", "authors", "auto_increment", "autocommit", "avg", "avg_row_length", "before", "binary", "binlog", "both", "btree", "cache", "call", "cascade", "cascaded", "case", "catalog_name", "chain", "change", "changed", "character", "check", "checkpoint", "checksum", "class_origin", "client_statistics", "close", "coalesce", "code", "collate", "collation", "collations", "column", "columns", "comment", "commit", "committed", "completion", "concurrent", "condition", "connection", "consistent", "constraint", "contains", "continue", "contributors", "convert", "cross", "current", "current_date", "current_time", "current_timestamp", "current_user", "cursor", "data", "database", "databases", "day_hour", "day_microsecond", "day_minute", "day_second", "deallocate", "dec", "declare", "default", "delay_key_write", "delayed", "delimiter", "des_key_file", "describe", "deterministic", "dev_pop", "dev_samp", "deviance", "diagnostics", "directory", "disable", "discard", "distinctrow", "div", "dual", "dumpfile", "each", "elseif", "enable", "enclosed", "end", "ends", "engine", "engines", "enum", "errors", "escape", "escaped", "even", "event", "events", "every", "execute", "exists", "exit", "explain", "extended", "fast", "fetch", "field", "fields", "first", "flush", "for", "force", "foreign", "found_rows", "full", "fulltext", "function", "general", "get", "global", "grant", "grants", "group", "group_concat", "handler", "hash", "help", "high_priority", "hosts", "hour_microsecond", "hour_minute", "hour_second", "if", "ignore", "ignore_server_ids", "import", "index", "index_statistics", "infile", "inner", "innodb", "inout", "insensitive", "insert_method", "install", "interval", "invoker", "isolation", "iterate", "key", "keys", "kill", "language", "last", "leading", "leave", "left", "level", "limit", "linear", "lines", "list", "load", "local", "localtime", "localtimestamp", "lock", "logs", "low_priority", "master", "master_heartbeat_period", "master_ssl_verify_server_cert", "masters", "match", "max", "max_rows", "maxvalue", "message_text", "middleint", "migrate", "min", "min_rows", "minute_microsecond", "minute_second", "mod", "mode", "modifies", "modify", "mutex", "mysql_errno", "natural", "next", "no", "no_write_to_binlog", "offline", "offset", "one", "online", "open", "optimize", "option", "optionally", "out", "outer", "outfile", "pack_keys", "parser", "partition", "partitions", "password", "phase", "plugin", "plugins", "prepare", "preserve", "prev", "primary", "privileges", "procedure", "processlist", "profile", "profiles", "purge", "query", "quick", "range", "read", "read_write", "reads", "real", "rebuild", "recover", "references", "regexp", "relaylog", "release", "remove", "rename", "reorganize", "repair", "repeatable", "replace", "require", "resignal", "restrict", "resume", "return", "returns", "revoke", "right", "rlike", "rollback", "rollup", "row", "row_format", "rtree", "savepoint", "schedule", "schema", "schema_name", "schemas", "second_microsecond", "security", "sensitive", "separator", "serializable", "server", "session", "share", "show", "signal", "slave", "slow", "smallint", "snapshot", "soname", "spatial", "specific", "sql", "sql_big_result", "sql_buffer_result", "sql_cache", "sql_calc_found_rows", "sql_no_cache", "sql_small_result", "sqlexception", "sqlstate", "sqlwarning", "ssl", "start", "starting", "starts", "status", "std", "stddev", "stddev_pop", "stddev_samp", "storage", "straight_join", "subclass_origin", "sum", "suspend", "table_name", "table_statistics", "tables", "tablespace", "temporary", "terminated", "to", "trailing", "transaction", "trigger", "triggers", "truncate", "uncommitted", "undo", "uninstall", "unique", "unlock", "upgrade", "usage", "use", "use_frm", "user", "user_resources", "user_statistics", "using", "utc_date", "utc_time", "utc_timestamp", "value", "variables", "varying", "view", "views", "warnings", "when", "while", "with", "work", "write", "xa", "xor", "year_month", "zerofill", "begin", "do", "then", "else", "loop", "repeat", "alter", "and", "as", "asc", "between", "by", "count", "create", "delete", "desc", "distinct", "drop", "from", "group", "having", "in", "insert", "into", "is", "join", "like", "not", "on", "or", "order", "select", "set", "table", "union", "update", "values", "where");
    private static final Set<String> ATOMS = ImmutableSet.of("false", "true", "null");
    private static final Set<String> BUILTINS = ImmutableSet.of("bool", "boolean", "bit", "blob", "decimal", "double", "first_value", "float", "long", "longblob", "longtext", "medium", "mediumblob", "mediumint", "mediumtext", "over", "time", "timestamp", "tinyblob", "tinyint", "tinytext", "text", "bigint", "int", "int1", "int2", "int3", "int4", "int8", "integer", "float", "float4", "float8", "double", "char", "varbinary", "varchar", "varcharacter", "precision", "date", "datetime", "year", "unsigned", "signed", "numeric");

    private final InnerTokenizer BASE_TOKENIZER = new BaseTokenizer();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private StringStream stream = new StringStream("");

    private InnerTokenizer tokenize = BASE_TOKENIZER;


    public BasicSQLTokenizer(Reader input) {
        super(input);
        try {
            stream = new StringStream(fillBuffer(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        stream.eatSpace();
        return tokenize.incrementToken();
    }

    @Override
    public void end() throws IOException {
        super.end();
        final int ofs = correctOffset(stream.getString().length());
        offsetAtt.setOffset(ofs, ofs);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        stream = new StringStream(fillBuffer(input));
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

    private void markTokenFromMatch(Matcher match, String type) {
        termAtt.setEmpty().append(match.group().toLowerCase());
        offsetAtt.setOffset(correctOffset(stream.getPos()-match.group().length()), correctOffset(stream.getPos()));
        typeAtt.setType(type);
        keywordAtt.setKeyword(false);
    }

    private void markToken(String type) {
        termAtt.setEmpty().append(stream.current().toLowerCase());
        offsetAtt.setOffset(correctOffset(stream.getStart()), correctOffset(stream.getPos()));
        typeAtt.setType(type);
        keywordAtt.setKeyword("keyword".equals(type));
    }

    private interface InnerTokenizer {
        boolean incrementToken() throws IOException;
    }

    private class BaseTokenizer implements InnerTokenizer {
        public boolean incrementToken() throws IOException {
            stream.eatSpace();
            stream.setStart();
            if (!stream.next()) {
                return false;
            }


            clearAttributes();

            char ch = stream.getChar();
            int charCode = (int) ch;

            if (charCode > 47 && charCode < 58) {
                stream.backUp(1);
                Matcher match = stream.match(NUMBER).get();
                markTokenFromMatch(match, "number");
                return true;
            } else if (ch == '\'' || ch == '"') {
                tokenize = new TokenLiteralTokenizer(ch);
                return tokenize.incrementToken();
            } else {
                String input = "" + ch;
                if (TERMINAL_CHARS.matcher(input).matches()) {
                    return tokenize.incrementToken();
                } else if (ch == '.') {
                    Optional<Matcher> match = stream.match(IDENTIFIERS);
                    if (match.isPresent()) {
                        markTokenFromMatch(match.get(), "word");
                        return true;
                    }
                    return tokenize.incrementToken();
                } else if (OPERATOR_CHARS.matcher(input).matches()) {
                    stream.eatWhile(OPERATOR_CHARS);
                    return tokenize.incrementToken();
                } else {
                    stream.eatWhile(WORD_CHARS);
                    String word = stream.current().toLowerCase();
                    if (DATE_SQL.contains(word) && (stream.match(SURROUNDING_1).isPresent() || stream.match(SURROUNDING_2).isPresent())) markToken("number");
                    else if (ATOMS.contains(word) || BUILTINS.contains(word) || KEYWORDS.contains(word)) markToken("keyword");
                    else markToken("word");
                    return true;
                }
            }
        }
    }


    private class TokenLiteralTokenizer implements InnerTokenizer {
        private final char quote;
        private boolean escaped = false;

        TokenLiteralTokenizer(char quote) {
            this.quote = quote;
        }

        public boolean incrementToken() throws IOException {
            stream.setStart();
            escaped = false;
            if (stream.eol()) return false;
            clearAttributes();
            while (stream.next()) {
                char ch = stream.getChar();
                if (ch == quote && !escaped) {
                    tokenize = BASE_TOKENIZER;
                    stream.backUp(1);
                    markToken("word");
                    stream.next();
                    return true;
                } else if (Character.isWhitespace(ch)) {
                    stream.backUp(1);
                    markToken("word");
                    stream.eatSpace();
                    return true;
                }
                escaped = !escaped && ch == '\\';
            }

            return false;
        }

    }
}
