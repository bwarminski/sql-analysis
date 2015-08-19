package org.elasticsearch.sqlAnalysis;

import org.elasticsearch.index.analysis.AnalysisModule;

/**
 * Created by bwarminski on 8/19/15.
 */
public class SQLAnalyzerBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {
    @Override
    public void processAnalyzers(AnalyzersBindings bindings) {
        bindings.processAnalyzer(BasicSQLAnalyzerProvider.NAME, BasicSQLAnalyzerProvider.class);
    }
}
