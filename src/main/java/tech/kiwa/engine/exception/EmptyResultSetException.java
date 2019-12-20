package tech.kiwa.engine.exception;

public class EmptyResultSetException extends RuleEngineException {
    private static final long serialVersionUID = -2247544711821509687L;

    public EmptyResultSetException() {
        super();
    }

    public EmptyResultSetException(String message) {
        super(message);
    }

    public EmptyResultSetException(Throwable cause) {
        super(cause);
    }

    public EmptyResultSetException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyResultSetException(String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
