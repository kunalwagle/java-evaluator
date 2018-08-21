package controllers;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShuffleJava {
    private TestServiceJava utils = new TestServiceJava();
//  Simple and blunt implementation, to be improved
    public String shuffleSolution(String str, String argv, Optional<String> optionalArgs) {
        Integer command = Integer.valueOf(argv);
        switch (command) {
            case 1:
                str = utils.moveToBeginning(str, String.valueOf(optionalArgs));
                break;
            case 2:
                str = utils.reverseString(str);
                break;
            case 3:
                str = ((Character) utils.getMaxOccurence(utils.
                        groupBy((str.chars().mapToObj(i -> (char) i)).
                                collect(Collectors.toList())))).toString();
                break;
            case 4:
                str = utils.sortString(str, String.valueOf(optionalArgs));
                break;
            default:
                throw new IllegalArgumentException("Wrong command entered");
        }
        return str;
    }
}
