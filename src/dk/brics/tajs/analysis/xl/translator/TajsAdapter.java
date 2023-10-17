package dk.brics.tajs.analysis.xl.translator;

import dk.brics.tajs.lattice.Value;
import dk.brics.tajs.util.CrossLanguageAnalysisException;

public interface TajsAdapter {

     Value queryObject(Value v) throws CrossLanguageAnalysisException;

     Value queryField(Value v, String fieldName) throws CrossLanguageAnalysisException;

     Value queryMethod(Value v, String methodName) throws CrossLanguageAnalysisException;
}
