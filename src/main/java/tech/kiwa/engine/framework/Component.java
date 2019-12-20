package tech.kiwa.engine.framework;

import tech.kiwa.engine.exception.RuleEngineException;

public interface Component {
    void register() throws RuleEngineException;
}
