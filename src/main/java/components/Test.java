package components;

public class Test {

    private String title;
    private Category category;
    private int firstInput;
    private int secondInput;
    private int thirdInput;
    private int[] expected;
    private boolean exceptionExpected;
    private String exceptionText;

    public Test(String title, Category category, int firstInput, int secondInput, int thirdInput, int[] expected, boolean exceptionExpected, String exceptionText) {
        this.title = title;
        this.category = category;
        this.firstInput = firstInput;
        this.secondInput = secondInput;
        this.thirdInput = thirdInput;
        this.expected = expected;
        this.exceptionExpected = exceptionExpected;
        this.exceptionText = exceptionText;
    }

    public int getFirstInput() {
        return firstInput;
    }

    public int getSecondInput() {
        return secondInput;
    }

    public int getThirdInput() {
        return thirdInput;
    }

    public int[] getExpected() {
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
