package dk.brics.tajs.analysis.xl.translator;

import dk.brics.tajs.lattice.Value;
import dk.brics.tajs.util.CrossLanguageAnalysisException;

import java.util.List;

public interface TajsAdapter {

     Value readProperty(Value v, String propertyName) throws CrossLanguageAnalysisException;

     Value callFunction(Value v, String FunctionName, List<Value> parameters) throws CrossLanguageAnalysisException;

     void setProperty(Value v, String propertyName, Value assignedValue) throws CrossLanguageAnalysisException;

     Value newObject(Integer index, String javaName);
}
