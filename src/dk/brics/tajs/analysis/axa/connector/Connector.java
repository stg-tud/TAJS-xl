package dk.brics.tajs.analysis.axa.connector;


import dk.brics.tajs.lattice.Value;

public class Connector {

    private static IConnector connector;

        public static void setConnector(IConnector coordinator){
            Connector.connector = coordinator;
        }


    public static Value queryFunctionValue(String javaFullClassName, String javaFunctionName) {
        return connector.queryFunctionValue(javaFullClassName, javaFunctionName);
    }


    public static Value queryPropertyValue(String javaFullClassName, String javaPropertyName) {
        return connector.queryPropertyValue(javaFullClassName, javaPropertyName);
    }


    public static void resume(String code) {
        connector.resume(code);
    }
}
