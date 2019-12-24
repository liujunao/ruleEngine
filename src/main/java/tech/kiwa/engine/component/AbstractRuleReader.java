package tech.kiwa.engine.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kiwa.engine.entity.RESULT;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;

//规则获取的抽象类
public abstract class AbstractRuleReader {
    private static Logger log = LoggerFactory.getLogger(AbstractRuleReader.class);
    //缓存获取的规则项（系统缓存）
    protected volatile List<RuleItem> ruleItemCache = new ArrayList<>();

    public abstract List<RuleItem> readRuleItemList() throws RuleEngineException; //获取规则列表

    public abstract Long getRuleItemCount() throws RuleEngineException; //获取规则列表的数量

    public abstract RuleItem getRuleItem(String ruleId) throws RuleEngineException; //获取指定 item_no 的规则项

    /**
     * 将同层级规则列表，按优先级排序
     *
     * @param itemList   排序的对象列表
     * @param parentItem 父级规则
     * @return 排好序的规则列表
     */
    public List<RuleItem> sortItem(List<RuleItem> itemList, String parentItem) {
        List<RuleItem> tempItemList = filterItem(itemList, parentItem); //过滤同层级列表
        List<RuleItem> retItemList = new ArrayList<>();
        //按优先级大小排序，并放入 retItemList
        while (!tempItemList.isEmpty()) {
            int maxIndex = queryMaxPriority(tempItemList);
            retItemList.add(tempItemList.get(maxIndex));
            tempItemList.remove(maxIndex);
        }
        return retItemList;
    }

    /**
     * 查找同层级的规则列表
     *
     * @param itemList     要查找的对象
     * @param parentItemNo 父级规则
     * @return 同层级的规则列表
     */
    public List<RuleItem> filterItem(List<RuleItem> itemList, String parentItemNo) {
        List<RuleItem> newItemList = new ArrayList<>();
        //同一层级的 item
        for (RuleItem item : itemList) {
            if (StringUtils.isEmpty(parentItemNo)) {
                if (StringUtils.isEmpty(item.getParentItemNo())) {
                    newItemList.add(item); //父级规则为空
                }
            } else if (parentItemNo.equals(item.getParentItemNo())) {
                newItemList.add(item); //父级规则相同
            }
        }

        return newItemList;
    }

    /**
     * 检查规则的格式是否正确
     *
     * @param item 规则定义体
     * @return true -- 合法 false　-- 不合法
     */
    public boolean preCompile(RuleItem item) {
        boolean bRet = true;
        if (StringUtils.isEmpty(item.getItemNo())) {
            log.debug("ruleId cannot be empty.");
            bRet = false;
        }
        // parent 与 group 是否为空
        if (StringUtils.isEmpty(item.getParentItemNo()) && StringUtils.isEmpty(item.getGroupExpress())) {
            if (StringUtils.isEmpty(item.getPriority())) {
                log.debug("priority cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getPriority())) {
                log.debug("priority cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getContinueFlag())) {
                log.debug("continue flag cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getResult())) {
                RESULT result = RESULT.EMPTY;
                if (!result.typeFromStringToInt(item.getResult())) {
                    log.debug("result cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                    bRet = false;
                }
            }
            if (StringUtils.isEmpty(item.getExeClass()) && StringUtils.isEmpty(item.getExeSql())) {
                log.debug("either exe_class or exe_sql must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isEmpty(item.getBaseline())) {
                log.debug("baseline must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isEmpty(item.getComparisonCode())) {
                log.debug("comparison code must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isNotEmpty(item.getExeSql())) {
                String sql = item.getExeSql();
                if (sql.contains("?")) {
                    if (StringUtils.isEmpty(item.getParamName())) {
                        log.debug("param name must be inputted since your exe_sql has parameters. ruleid ={}", item.getItemNo());
                        bRet = false;
                    }
                }
            }
        }
        // 同一 group 的 item 运算表达式
        if (StringUtils.isNotEmpty(item.getGroupExpress())) {
            if (StringUtils.isEmpty(item.getPriority())) { //执行的优先级
                log.debug("priority cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getPriority())) {
                log.debug("priority cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getContinueFlag())) { //是否执行下一条：1 则继续执行
                log.debug("continue flag cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (!StringUtils.isNumeric(item.getResult())) { //状态：1 通过，2 关注，3 拒绝
                RESULT result = RESULT.EMPTY;
                if (!result.typeFromStringToInt(item.getResult())) {
                    log.debug("result cannot be empty and must be numberic if it's free-running rule. ruleid ={}", item.getItemNo());
                    bRet = false;
                }
            }
        }
        //父规则检查
        if (StringUtils.isNotEmpty(item.getParentItemNo())) {
            if (StringUtils.isEmpty(item.getExeClass()) && StringUtils.isEmpty(item.getExeSql())) {
                log.debug("either java_class or exe_sql must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isEmpty(item.getBaseline())) {
                log.debug("baseline must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isEmpty(item.getComparisonCode())) {
                log.debug("comparison code must be inputted. ruleid ={}", item.getItemNo());
                bRet = false;
            }
            if (StringUtils.isNotEmpty(item.getExeSql())) {
                String sql = item.getExeSql();
                if (sql.contains("?")) {
                    if (StringUtils.isEmpty(item.getParamName())) {

                        log.debug("param name must be inputted since your exe_sql has parameters. ruleid ={}", item.getItemNo());
                        bRet = false;
                    }
                }
            }
        }
        return bRet;
    }

    /**
     * 清除缓存
     */
    public void clearRuleItemCache() {
        synchronized (ruleItemCache) {
            ruleItemCache.clear();
        }
    }

    /**
     * 更新缓存
     *
     * @param item
     */
    public void updateRuleItem(RuleItem item) {
        for (int iLoop = 0; iLoop < ruleItemCache.size(); iLoop++) {
            RuleItem element = ruleItemCache.get(iLoop);
            if (element.getItemNo().equalsIgnoreCase(item.getItemNo())) {
                synchronized (ruleItemCache) {
                    ruleItemCache.set(iLoop, item);
                }
            }
        }
    }

    /**
     * 在规则列表中查找最小的优先级
     *
     * @param itemList 查找的对象列表
     * @return 最小的优先级
     */
    @SuppressWarnings("unused")
    private int queryMiniPriority(List<RuleItem> itemList) {
        int priority = Integer.MAX_VALUE;
        int minIndex = Integer.MAX_VALUE;
        for (int iLoop = 0; iLoop < itemList.size(); iLoop++) {
            try {
                int current = Integer.valueOf(itemList.get(iLoop).getPriority());
                if (priority > current) {
                    priority = current;
                    minIndex = iLoop;
                }
            } catch (java.lang.NumberFormatException e) {
                log.debug(e.getLocalizedMessage());
            }
        }
        return minIndex;
    }

    /**
     * 在规则列表中查找最大的优先级
     *
     * @param itemList 查找的对象列表
     * @return 最大的优先级
     */
    private int queryMaxPriority(List<RuleItem> itemList) {
        int priority = Integer.MIN_VALUE;
        int minIndex = Integer.MIN_VALUE;
        for (int iLoop = 0; iLoop < itemList.size(); iLoop++) {
            try {
                int current = Integer.valueOf(itemList.get(iLoop).getPriority());
                if (priority < current) {
                    priority = current;
                    minIndex = iLoop;
                }
            } catch (java.lang.NumberFormatException e) {
                log.debug(e.getLocalizedMessage());
            }
        }
        return minIndex;
    }
}
