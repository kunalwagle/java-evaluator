package controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUtils {

    public Integer[] randomizeSequence(Integer[] array) {
        List<Integer> asList = Arrays.asList(array);
        Collections.shuffle(asList);
        return (Integer[]) asList.toArray();
    }

    public String getRandomString(int maxLength) {
        return String.valueOf(Math.random()).substring(2, maxLength - 1)
                + String.valueOf(Math.random()).substring(2, maxLength - 1);
    }

    private int getRandomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
//    This is a mess, ive no idea what input type is
    public String[] randomlyRemoveField(String[][] input, String field) {
        int idx = getRandomInt(0, input.length);
        List<String[]> inputAsList = Arrays.asList(input);
        inputAsList.set(idx, removeStringFromArray(inputAsList.get(idx), field));
        return (String[]) inputAsList.toArray();
    }

    public char getRandomChar() {
        int upperCase = getRandomInt(1, 2);
        char result = (char) (new Random().nextInt(26) + 'a');
        return (upperCase % 2 == 0) ? result : Character.toUpperCase(result);
    }

    private String[] removeStringFromArray(String[] array, String toBeRemoved) {
        List<String> asList = Arrays.asList(array);
        asList.remove(toBeRemoved);
        return (String[]) asList.toArray();
    }
}
