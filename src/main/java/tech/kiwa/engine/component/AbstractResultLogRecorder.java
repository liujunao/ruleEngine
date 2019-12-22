package tech.kiwa.engine.component;

import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.framework.Component;
import tech.kiwa.engine.framework.ResultLogFactory;

public abstract class AbstractResultLogRecorder implements Component {
    @Override
    public void register() throws RuleEngineException {
        ResultLogFactory optMgr = ResultLogFactory.getInstance();
        optMgr.acceptRegister(this);
    }

    public abstract boolean writeLog(Object object, RuleItem item, ItemExecutedResult result);
}
