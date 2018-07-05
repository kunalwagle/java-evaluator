package components;

public enum Category {
    BASIC("BASIC"),
    MEDIUM("MEDIUM"),
    DIFFICULT("DIFFICULT");

    private String type;

    Category(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
