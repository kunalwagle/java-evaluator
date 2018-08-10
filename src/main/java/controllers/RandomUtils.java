package controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUtils {

    public Integer[] randomizeSequence(Integer[] array, int lower, int upper, int length) {
        List<Integer> asList = Arrays.asList(array);
        Collections.shuffle(asList);
        return (Integer[]) asList.toArray();
    }

    public String getRandomString(int maxLength) {
        return String.valueOf(Math.random()).substring(2, maxLength - 1)
                + String.valueOf(Math.random()).substring(2, maxLength - 1);
    }

    private int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (Math.floor(max)
                - Math.ceil(min) + 1) + Math.ceil(min) + 1);
    }
//    This is a mess, ive no idea what input type is
    public String[] randomlyRemoveField(String[][] input, String field) {
        int idx = getRandomInt(0, input.length);
        List<String[]> inputAsList = Arrays.asList(input);
        inputAsList.set(idx, removeStringFromArray(inputAsList.get(idx), field));
        return (String[]) inputAsList.toArray();
    }

    public String getSorterString() {
        return null;
    }

    public char getRandomChar() {
        int upperCase = (int) Math.random();
        return (char) (new Random().nextInt(26) + 'a');
    }

    private String[] removeStringFromArray(String[] array, String toBeRemoved) {
        List<String> asList = Arrays.asList(array);
        asList.remove(toBeRemoved);
        return (String[]) asList.toArray();
    }
}
