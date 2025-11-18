package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.woggioni.jwo.JWO.newThrowable;

public class SQL {

    public enum Operation {
        INSERT
    }

    @RequiredArgsConstructor
    public static class QueryBuilder {
        private final Operation operation;
        private final String tableName;

        private final Map<String, Tuple2<Object, Class<?>>> fields = new TreeMap<>();

        public QueryBuilder field(final String name, final Object value, final Class<?> cls) {
            fields.put(name, Tuple2.newInstance(value, cls));
            return this;
        }

        public QueryBuilder field(final String name, final Object value) {
            if(value == null) {
                throw newThrowable(IllegalArgumentException.class, "Class argument required for null value");
            }
            return field(name, value, value.getClass());
        }

        @SneakyThrows
        public PreparedStatement buildStatement(final Connection conn) {
            final StringBuilder sb = new StringBuilder();
            switch (operation) {
                case INSERT:
                    sb.append("INSERT INTO ");
                    sb.append(tableName);
                    sb.append(" (");
                    int i = 0;
                    final List<Map.Entry<String, Tuple2<Object, Class<?>>>> entries = new ArrayList<>(fields.entrySet());
                    for(Map.Entry<String, Tuple2<Object, Class<?>>> entry : entries) {
                        if(i++ > 0) sb.append(',');
                        sb.append(entry.getKey());
                    }
                    sb.append(") VALUES(");
                    while(i-->0) {
                        sb.append("?");
                        if(i > 0) sb.append(',');
                    }
                    sb.append(");");
                    final PreparedStatement stmt = conn.prepareStatement(sb.toString());
                    i = 1;
                    for(final Map.Entry<String, Tuple2<Object, Class<?>>> entry : entries) {
                        final Tuple2<Object, Class<?>> tuple2 = entry.getValue();
                        final Object value = tuple2.get_1();
                        final  Class<?> cls = tuple2.get_2();
                        if(cls.isAssignableFrom(String.class)) {
                            stmt.setString(i, (String) value);
                        } else if(cls.isAssignableFrom(Integer.class)) {
                            stmt.setInt(i, (Integer) value);
                        } else if(cls.isAssignableFrom(Double.class)) {
                            stmt.setDouble(i, (Double) value);
                        } else if(cls.isAssignableFrom(Float.class)) {
                            stmt.setFloat(i, (Float) value);
                        } else if(cls.isAssignableFrom(Short.class)) {
                            stmt.setShort(i, (Short) value);
                        } else if(cls.isAssignableFrom(Byte.class)) {
                            stmt.setByte(i, (Byte) value);
                        } else if(cls.isAssignableFrom(Boolean.class)) {
                            stmt.setBoolean(i, (Boolean) value);
                        } else if(cls.isAssignableFrom(Timestamp.class)) {
                            stmt.setTimestamp(i, (Timestamp) value);
                        } else {
                            throw newThrowable(IllegalArgumentException.class, "Class '%s' is not supported",
                                value.getClass());
                        }
                        ++i;
                    }
                    return stmt;
                default:
                    throw new RuntimeException("This should never happen");
            }
        }

    }
}