package components;

public class CompletedTest extends Test {

    private String message;
    private boolean passed;

    public CompletedTest(String title, Category category, int firstInput, int secondInput, int thirdInput, int[] expected, boolean exceptionExpected, String exceptionText) {
        super(title, category, firstInput, secondInput, thirdInput, expected, exceptionExpected, exceptionText);
    }

    public CompletedTest(Test test, boolean passed, String message) {
        super(test.getTitle(), test.getCategory(), test.getFirstInput(), test.getSecondInput(), test.getThirdInput(), test.getExpected(), test.isExceptionExpected(), test.getExceptionText());
        this.passed = passed;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPassed() {
        return passed;
    }
}
