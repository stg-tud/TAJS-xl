package dk.brics.tajs.analysis.xl.translator;

public class LocalTAJSAdapter {

    private static TajsAdapter tajsAdapter;

        public static void setLocalTAJSAdapter(TajsAdapter tajsAdapter){
            LocalTAJSAdapter.tajsAdapter = tajsAdapter;
        }

        public static TajsAdapter getLocalTajsAdapter(){return tajsAdapter;}
}
