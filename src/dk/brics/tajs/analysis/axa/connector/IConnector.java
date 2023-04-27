package dk.brics.tajs.analysis.axa.connector;


import dk.brics.tajs.lattice.Value;


public interface IConnector {
     Value queryFunctionValue(String javaFullClassName, String javaFunctionName);

     Value queryPropertyValue(String javaFullClassName, String javaPropertyName);

     void resume(String code);
}
