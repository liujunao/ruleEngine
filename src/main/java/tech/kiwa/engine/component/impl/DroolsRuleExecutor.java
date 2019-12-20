package tech.kiwa.engine.component.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kiwa.engine.component.AbstractCommand;
import tech.kiwa.engine.component.AbstractRuleItem;
import tech.kiwa.engine.component.drools.DroolsBuilder;
import tech.kiwa.engine.component.drools.LocalCreator;
import tech.kiwa.engine.component.drools.RuleCreator;
import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RESULT;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;

public class DroolsRuleExecutor extends AbstractRuleItem {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ItemExecutedResult doCheck(RuleItem item) throws RuleEngineException {
        RuleCreator creator = (RuleCreator) item.getAttach();
        DroolsBuilder builder = creator.getBuilder();
        builder.compile();
        boolean bRet = creator.runCondition(this.object);
        ItemExecutedResult checkResult = new ItemExecutedResult();
        //缺省认为是 passed
        checkResult.setResult(RESULT.EMPTY);    //通过
        checkResult.setRemark(RESULT.EMPTY.getName());
        checkResult.setContinue(ItemExecutedResult.CONTINUE);
        checkResult.setReturnValue(bRet);
        if (bRet) {
            checkResult.setResult(item.getResult());
            checkResult.setRemark(checkResult.getResult().getName());
            checkResult.setContinue(Integer.parseInt(item.getContinueFlag()));
        }
        this.executeCommand(item, checkResult);
        return checkResult;
    }

    private void executeCommand(RuleItem item, ItemExecutedResult result) {
        if (!result.getReturnValue()) {
            return;
        }
        try {
            if (StringUtils.isNotEmpty(item.getExecutor())) {
                RuleCreator rule = (RuleCreator) item.getAttach();
                DroolsBuilder builder = rule.getBuilder();
                builder.compile();
                AbstractCommand command = (AbstractCommand) builder.createObject(item.getExecutor(), rule.toJavaString());
                for (LocalCreator result_value : rule.getResultList()) {
                    if (result_value != null && result_value.getValue() != null) {
                        command.SetObject(result_value.getValue());
                    }
                }
                command.execute(item, result);
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
    }
}
