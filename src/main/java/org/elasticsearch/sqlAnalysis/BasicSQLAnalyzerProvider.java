package org.elasticsearch.sqlAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettingsService;

/**
 * Created by bwarminski on 8/19/15.
 */
public class BasicSQLAnalyzerProvider extends AbstractIndexAnalyzerProvider<BasicSQLAnalyzer> {

    @Inject
    public BasicSQLAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index,indexSettingsService.getSettings(),name,settings);

    }

    private final BasicSQLAnalyzer analyzer = new BasicSQLAnalyzer();

    public BasicSQLAnalyzer get() {
        return analyzer;
    }

    public static final String NAME = "basic_sql";
}
