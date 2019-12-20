package tech.kiwa.engine;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kiwa.engine.component.AbstractRuleItem;
import tech.kiwa.engine.component.AbstractRuleReader;
import tech.kiwa.engine.component.impl.ComplexRuleExecutor;
import tech.kiwa.engine.component.impl.DBRuleReader;
import tech.kiwa.engine.component.impl.DefaultRuleExecutor;
import tech.kiwa.engine.component.impl.DroolsRuleReader;
import tech.kiwa.engine.component.impl.XMLRuleReader;
import tech.kiwa.engine.entity.EngineRunResult;
import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RESULT;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.framework.ResultLogFactory;
import tech.kiwa.engine.sample.Student;
import tech.kiwa.engine.utility.PropertyUtil;

public class EngineService {
    private Logger log = LoggerFactory.getLogger(EngineService.class);

    private AbstractRuleReader itemService = loadService(); //这个操作的目的？？？
    private String seq = null;

    //获取配置文件(xml,property,drools)的配置规则
    @SuppressWarnings("unchecked")
    private AbstractRuleReader loadService() {
        if (itemService != null) {
            return itemService;
        }
        AbstractRuleReader reader = null;

        String serviceName = PropertyUtil.getProperty("rule.reader"); //获取 rule.reader 的属性值： xml、drools、database
        switch (serviceName.toLowerCase()) {
            case "database":
                reader = new DBRuleReader();
                break;
            case "xml":
                reader = new XMLRuleReader();
                break;
            case "drools":
                reader = new DroolsRuleReader();
                break;
            default:
                Class<AbstractRuleReader> ServiceClass;
                try {
                    ServiceClass = (Class<AbstractRuleReader>) Class.forName(serviceName);
                    reader = ServiceClass.newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.debug(e.getMessage());
                }
                break;
        }
        itemService = reader;
        return reader;
    }

    public EngineRunResult start(Object object) throws RuleEngineException {
        return this.start(object, null);
    }

    public EngineRunResult start(Map<String, Object> object) throws RuleEngineException {
        return this.start((String) object.get("Id"), null);
    }

    @SuppressWarnings("unchecked")
    public EngineRunResult start(Object object, String sequence) throws RuleEngineException {
        List<RuleItem> itemList = itemService.readRuleItemList();
        itemList = itemService.sortItem(itemList, null); //过滤出同层级列表，并按优先级排序

        this.seq = sequence;
        //TODO：。。。
        EngineRunResult ret_Result = new EngineRunResult();
        ret_Result.setResult(RESULT.PASSED);
        ret_Result.setResult_desc("PASSED");

        for (RuleItem item : itemList) {
            log.debug("started to execute the rule item check. item = {}, ObjectId ={}", item.getItemNo(), object);
            if (StringUtils.isNotEmpty(item.getGroupExpress())) {
                ComplexRuleExecutor executor = new ComplexRuleExecutor();
                executor.setObject(object);
                ItemExecutedResult result = null;
                //如果是重复执行.
                do {
                    result = executor.doCheck(item);
                    if (result == null) break;

                    seq = this.writeExecutedLog(object, item, result);
                    if (result.getResult().compare(ret_Result.getResult()) > 0) {
                        ret_Result.setResult(result.getResult());
                        ret_Result.setResult_desc(result.getRemark());
                    }
                    ret_Result.setSequence(seq);
                    if (!result.canBeContinue()) {
                        break;
                    }
                } while (result != null && result.shouldLoop());
                if (!result.canBeContinue()) {
                    break;
                }
            } else {
                String className = item.getExeClass();
                Class<AbstractRuleItem> auditClass = null;
                AbstractRuleItem auditInstance = null;
                if (StringUtils.isNotEmpty(className)) {
                    try {
                        auditClass = (Class<AbstractRuleItem>) Class.forName(className);
                        if (null != auditClass) {
                            auditInstance = auditClass.newInstance();
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuleEngineException(e.getMessage());
                    }
                }
                if (null == auditInstance) {
                    auditInstance = new DefaultRuleExecutor();
                }
                //直接地调用doCheck的函数。
                auditInstance.setObject(object);
                ItemExecutedResult result = null;
                //如果是重复执行.
                do {
                    result = auditInstance.doCheck(item);
                    if (result == null) {
                        break;
                    }
                    seq = this.writeExecutedLog(object, item, result);
                    if (result.getResult().compare(ret_Result.getResult()) > 0) {
                        ret_Result.setResult(result.getResult());
                        ret_Result.setResult_desc(result.getRemark());
                    }
                    ret_Result.setSequence(seq);
                    if (!result.canBeContinue()) {
                        break;
                    }
                } while (result != null && result.shouldLoop());
                if (!result.canBeContinue()) {
                    break;
                }
            }
        }
        return ret_Result;
    }

    protected String writeExecutedLog(Object object, RuleItem item, ItemExecutedResult result) throws RuleEngineException {
        if (StringUtils.isEmpty(seq)) {
            seq = String.valueOf(System.currentTimeMillis());
            seq = seq + String.valueOf(new Random(1000).nextInt());
        }
        try {
            ResultLogFactory.getInstance().writeLog(object, item, result);
        } catch (RuleEngineException e) {
            log.debug("write log error.");
            throw e;
        }
        return seq;
    }

    public static void main(String[] args) {
        EngineService service = new EngineService();
        try {
            for (int iLoop = 0; iLoop < 1000; iLoop++) {
                Student st = new Student();
                st.setAge(5);
                st.name = "tom";
                st.sex = 1;
                EngineRunResult result = service.start(st);
                System.out.println(result.getResult().getName());
                System.out.println(st.getAge());
            }
        } catch (RuleEngineException e) {
            e.printStackTrace();
        }
    }
}