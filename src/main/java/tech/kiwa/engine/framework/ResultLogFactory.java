package tech.kiwa.engine.framework;

import tech.kiwa.engine.component.AbstractResultLogRecorder;
import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;

import java.util.ArrayList;
import java.util.List;

public class ResultLogFactory implements FactoryMethod {
    private static ResultLogFactory instance = new ResultLogFactory();
    private static List<AbstractResultLogRecorder> logList = new ArrayList<>();

    private ResultLogFactory() {
    }

    public static ResultLogFactory getInstance() {
        return instance;
    }

    @Override
    public void acceptRegister(Component logger) {
        logList.add((AbstractResultLogRecorder) logger);
    }

    public boolean writeLog(Object object, RuleItem item, ItemExecutedResult result) {
        boolean bRet = true;
        for (AbstractResultLogRecorder logger : logList) {
            bRet = bRet && logger.writeLog(object, item, result);
        }
        return bRet;
    }
}
