package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestServiceJava {

    List<DataPoint> datapoints = new ArrayList<>();

    private String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public Map<String, Integer> groupBy(List<String> list) {
        Map<String, Integer> map = new HashMap<>();
        for (String a : list) {
            if (!map.containsKey(a)) {
                map.put(a, 1);
            }
            else {
                map.put(a, map.get(a) + 1);
            }
        }
        return map;
    }

    public char getMaxOccurence(Map<Character, Integer> map) {
        char result = map.keySet().stream().findFirst().get();
        int max = map.get(result);
        for (Map.Entry<Character, Integer> a : map.entrySet()) {
            if (a.getValue() > max) {
                max = a.getValue();
                result = a.getKey();
            }
        }
        return result;
    }

    public String sortString(String str) {
        return str.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
