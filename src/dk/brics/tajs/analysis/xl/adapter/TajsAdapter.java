package dk.brics.tajs.analysis.xl.adapter;

import dk.brics.tajs.lattice.Value;

import java.util.List;

public interface TajsAdapter {

     Value readProperty(Value v, String propertyName);

     Value callFunction(Value v, String FunctionName, List<Value> parameters);

     void setProperty(Value v, String propertyName, Value assignedValue);

     Value newObject(Integer index, String javaName);
}
