package com.darklanders.bitcards.common.script.rhino;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 28/04/2016.
 */
public class RhinoHelper {

    public static final String PARAM_OUT = "out";

    public static final String[] EXTERNS = {PARAM_OUT};

    private Context context;
    private Scriptable scope;
    private ObjectMapper objectMapper;
    private Map<String, Object> properties;

    public RhinoHelper() {
        this(Context.enter());
    }

    public RhinoHelper(Context context) {
        this(context, context.initSafeStandardObjects());
    }

    public RhinoHelper(Context context, Scriptable scope) {
        // disable optimisation
        context.setOptimizationLevel(-1);

        this.context = context;
        this.scope = scope;
        this.objectMapper = new ObjectMapper();
        this.properties = new HashMap<String, Object>();

        addQuietProperty(System.out, PARAM_OUT);
    }

    private String addQuietProperty(Object o, String ...keys) {
        // convert to string
        Object wrappedObject;
        String result;
        try {
            String s = this.objectMapper.writeValueAsString(o);
            wrappedObject = NativeJSON.parse(this.context, this.scope, s, new Callable() {
                @Override
                public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                    return objects[1];
                }
            });
            result = s;
        } catch( Exception ex ) {
            //throw new RuntimeException("unable to convert "+o, ex);
            System.err.println("unable to serialize value for "+keys[0]+", will embed directly instead: "+o);
            ex.printStackTrace();
            wrappedObject = Context.javaToJS(o, this.scope);
            result = null;
        }
        for(String key : keys ) {
            ScriptableObject.putProperty(scope, key, wrappedObject);
        }
        return result;
    }

    public void addProperty(Object o, String ...keys) {
        String result = addQuietProperty(o, keys);
        Object reported;
        if( result != null ) {
            reported = result;
        } else {
            reported = o;
        }
        this.properties.put(keys[0], reported);
    }

    public <T> T evaluate(String script, Class<T> resultType, String sourceName) throws IOException {

        for(Map.Entry<String, Object> property : this.properties.entrySet() ) {
            System.out.print(property.getKey()+"=");
            try {
                System.out.println(property.getValue());
            } catch( Exception ex ) {
                System.out.println(property.getValue());
            }
        }
        System.out.println(script);

        context.evaluateString(scope, script, sourceName, 1, null);

        Object jsObject = this.scope.get("r", this.scope);
        String json = (String)NativeJSON.stringify(this.context, this.scope, jsObject, null, null);

        System.out.println(json);

        T result = this.objectMapper.readValue(json, resultType);

        System.out.println(result);

        return result;
    }

    public void exit() {
        // thread local?
        Context.exit();
    }

}
