package game.util;

public enum Out {
    ANSI_RESET("\u001B[0m"),
    ANSI_RED("\u001B[31m"),
    ANSI_BLUE("\u001B[34m");

    private String color;

    Out(java.lang.String color) {
        this.color = color;
    }


    @Override
    public String toString() {
        return color;
    }
}