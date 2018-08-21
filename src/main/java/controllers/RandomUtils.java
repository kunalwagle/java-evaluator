package controllers;

import java.util.*;

class RandomUtils {

    protected String getRandomString(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException();
        }
        return String.valueOf(Math.random()).substring(2, maxLength - 1)
                + String.valueOf(Math.random()).substring(2, maxLength - 1);
    }

    protected int getRandomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    protected char getRandomChar() {
        int upperCase = getRandomInt(1, 2);
        char result = (char) (new Random().nextInt(26) + 'a');
        return (upperCase % 2 == 0) ? result : Character.toUpperCase(result);
    }

    private String[] removeStringFromArray(String[] array, String toBeRemoved) {
        List<String> asList = Arrays.asList(array);
        asList.remove(toBeRemoved);
        return (String[]) asList.toArray();
    }

    private Integer[] randomizeSequence(Integer[] array) {
        List<Integer> asList = Arrays.asList(array);
        Collections.shuffle(asList);
        return (Integer[]) asList.toArray();
    }

    private DataPoint generateDataPoints() {
        return new DataPoint(getRandomInt(500, 1000) / 10, new Date());
    }

    private String[] randomlyRemoveField(String[][] input, String field) {
        int idx = getRandomInt(0, input.length);
        List<String[]> inputAsList = Arrays.asList(input);
        inputAsList.set(idx, removeStringFromArray(inputAsList.get(idx), field));
        return (String[]) inputAsList.toArray();
    }
}
