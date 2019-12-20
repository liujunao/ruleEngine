package tech.kiwa.engine.component;

import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.framework.Component;
import tech.kiwa.engine.framework.OperatorFactory;

public abstract class AbstractComparisonOperator implements Component {
    private String comparisonCode;

    public AbstractComparisonOperator(String comparison_code) throws Exception {
        comparisonCode = comparison_code;
        this.register();
    }

    public void register() throws RuleEngineException {
        OperatorFactory optMgr = OperatorFactory.getInstance();
        if (OperatorFactory.OPR_CODE.isReserved(comparisonCode)) {
            throw new RuleEngineException("cannot use reserved logic key.");
        }
        optMgr.acceptRegister(this);
    }

    public abstract boolean run(String subject, String baseline);

    public String getComparisonCode() {
        return comparisonCode;
    }
}
