package dk.brics.tajs.analysis.xl.translator;


import dk.brics.tajs.lattice.Value;


public interface JavaTranslator {
     Value queryFunctionValue(String javaFullClassName, String javaFunctionName);

     Value queryPropertyValue(String javaFullClassName, String javaPropertyName);

}
