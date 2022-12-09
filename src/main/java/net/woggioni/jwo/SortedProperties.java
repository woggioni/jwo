package net.woggioni.jwo;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SortedProperties extends Properties {

    @Override
    public Enumeration<Object> keys() {
        Iterator<Object> it = keySet().iterator();
        return new Enumeration<Object>() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public Object nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public Set<Object> keySet() {
        Enumeration<Object> enumeration = super.keys();

        TreeSet<String> sortedSet = new TreeSet<>();
        while(enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            sortedSet.add(key);
        }
        return (Set) sortedSet;
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Enumeration<Object> enumeration = keys();

        TreeMap<String, Object> sortedMap = new TreeMap<>();
        while(enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            sortedMap.put(key, get(key));
        }
        return (Set) sortedMap.entrySet();
    }
}