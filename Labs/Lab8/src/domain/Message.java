package domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

    private final Map<String, Object> fields = new HashMap<>();

    public Message(Type type) {
        setField(Fields.TYPE, type);
    }
    public void setField(String key, Object value) {
        fields.put(key, value);
    }

    public Object getField(final String key) {
        return fields.get(key);
    }

    public enum Type {
        SUBSCRIBE, UPDATE, QUIT
    }

    public static class Fields {
        public static final String VARIABLE = "variable";
        public static final String VALUE = "value";
        public static final String RANK = "rank";
        public static final String TYPE = "type";
    }
}
