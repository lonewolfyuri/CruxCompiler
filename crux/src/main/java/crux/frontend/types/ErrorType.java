package crux.frontend.types;

public final class ErrorType extends Type {
    private final String message;

    public ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equivalent(Type that) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("ErrorType(%s)", message);
    }
}
