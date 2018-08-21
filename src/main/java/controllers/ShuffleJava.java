package controllers;

import java.util.Optional;
import java.util.stream.Collectors;

public class ShuffleJava {
    private TestServiceJava utils = new TestServiceJava();
//  Simple and blunt implementation, to be improved
    public String shuffleSolution(String str, String argv, Optional<String> optionalArgs) {
        Integer command = Integer.valueOf(argv);
        switch (command) {
            case 1:
                return utils.moveToBeginning(str, String.valueOf(optionalArgs));
            case 2:
                return utils.reverseString(str);
            case 3:
                return ((Character) utils.getMaxOccurence(utils.
                        groupBy((str.chars().mapToObj(i -> (char) i)).
                                collect(Collectors.toList())))).toString();
            case 4:
                return utils.sortString(str, String.valueOf(optionalArgs));
            default:
                throw new IllegalArgumentException("Wrong command entered");
        }
    }
}
