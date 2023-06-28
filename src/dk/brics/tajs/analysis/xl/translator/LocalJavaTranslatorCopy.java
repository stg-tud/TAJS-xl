package dk.brics.tajs.analysis.xl.translator;

public class LocalJavaTranslatorCopy {

    private static JavaTranslator javaTranslator;

        public static void setLocalJavaTranslatorCopy(JavaTranslator javaTranslator){
            LocalJavaTranslatorCopy.javaTranslator = javaTranslator;
        }

        public static JavaTranslator getLocalJavaTranslatorCopy(){return javaTranslator;}

}
