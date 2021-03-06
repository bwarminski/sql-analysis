package org.elasticsearch.sqlAnalysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;

/**
 * Created by bwarminski on 8/19/15.
 */
public class SQLAnalysisPlugin extends Plugin {

    public String name() {
        return "plugin-sql-analysis";
    }

    public String description() {
        return "Very basic SQL tokenization";
    }

    public void onModule(AnalysisModule analysisModule) {
        analysisModule.addProcessor(new SQLAnalyzerBinderProcessor());
    }
}
