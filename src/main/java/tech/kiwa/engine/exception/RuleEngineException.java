package tech.kiwa.engine.exception;

public class RuleEngineException extends Exception {
    private static final long serialVersionUID = -2312238642591378363L;

    public RuleEngineException() {
        super();
    }

    public RuleEngineException(String message) {
        super(message);
    }

    public RuleEngineException(Throwable cause) {
        super(cause);
    }

    public RuleEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleEngineException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
