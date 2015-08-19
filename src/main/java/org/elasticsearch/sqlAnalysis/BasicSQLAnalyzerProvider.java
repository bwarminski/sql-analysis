package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * Created by bwarminski on 8/19/15.
 */
public class BasicSQLAnalyzerProvider extends AbstractIndexAnalyzerProvider<BasicSQLAnalyzer> {

    @Inject
    public BasicSQLAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index,indexSettings,name,settings);

    }

    private final BasicSQLAnalyzer analyzer = new BasicSQLAnalyzer();

    public BasicSQLAnalyzer get() {
        return analyzer;
    }

    public static final String NAME = "basic_sql";
}
