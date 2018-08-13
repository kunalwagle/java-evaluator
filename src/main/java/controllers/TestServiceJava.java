package controllers;

import java.util.*;

public class TestServiceJava {

    List<DataPoint> datapoints = new ArrayList<>();
    RandomUtils utils = new RandomUtils();
//  This is cancer, I shall get rid of this asap
    private final String[] alphabet = "abcdefgijklmnopqrstuxyz1234567890".split("");

    public String reverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public Map<Character, Integer> groupBy(List<Character> list) {
        Map<Character, Integer> map = new HashMap<>();
        for (Character a : list) {
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
        assert !map.isEmpty();
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

//  This is a complete bs why are we even doing this
    public String getSorterString() {
        String[] copy = alphabet;
        int currentIndex = alphabet.length;
        String tmp;
        while (currentIndex != 0) {
            int randomIndex = (int) Math.floor(Math.random() * currentIndex);
            currentIndex--;
            tmp = copy[currentIndex];
            copy[currentIndex] = copy[randomIndex];
            copy[randomIndex] = tmp;
        }
        return String.join("", copy);
    }
}
