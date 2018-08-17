package components;

public class Test {

    private String title;
    private Category category;
    private String firstInput;
    private String secondInput;
    private String thirdInput;
    private String expected;
    private boolean exceptionExpected;
    private String exceptionText;

    public Test(String title, Category category, String firstInput, String secondInput, String thirdInput, String expected, boolean exceptionExpected, String exceptionText) {
        this.title = title;
        this.category = category;
        this.firstInput = firstInput;
        this.secondInput = secondInput;
        this.thirdInput = thirdInput;
        this.expected = expected;
        this.exceptionExpected = exceptionExpected;
        this.exceptionText = exceptionText;
    }

    public String getFirstInput() {
        return firstInput;
    }

    public String getSecondInput() {
        return secondInput;
    }

    public String getThirdInput() {
        return thirdInput;
    }

    public String getExpected() {
        return expected;
    }

    public boolean isExceptionExpected() {
        return exceptionExpected;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public String getTitle() {
        return title;
    }

    public Category getCategory() {
        return category;
    }
}
