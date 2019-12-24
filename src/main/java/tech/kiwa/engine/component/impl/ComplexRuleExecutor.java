package tech.kiwa.engine.component.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kiwa.engine.component.AbstractCommand;
import tech.kiwa.engine.component.AbstractRuleItem;
import tech.kiwa.engine.component.AbstractRuleReader;
import tech.kiwa.engine.entity.ItemExecutedResult;
import tech.kiwa.engine.entity.RESULT;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.utility.PropertyUtil;

import java.util.ArrayList;
import java.util.List;

//
public class ComplexRuleExecutor extends AbstractRuleItem {
    private Logger log = LoggerFactory.getLogger(DefaultRuleExecutor.class);

    // 1 = "运算变量"  2 = 双目运算符号   3 = 单目运算符号  4 = 左括号  5 = 右括号
    private enum TYPE {
        VARIABLE, BINOCULAR, MONOCULAR, LEFT_BRACKET, RIGHT_BRACKET
    }

    //
    class OperationUnit {
        public TYPE type; // 1 = "运算变量"  2 = 双目运算符号   3 = 单目运算符号  4 = 左括号  5 = 右括号
        public String element; //
        public int level = 0; //

        @Override
        public String toString() {
            return "[type=" + String.valueOf(type) + " element = '" + element + "' level = " + String.valueOf(level) + "]";
        }
    }

    class ExpressionUnit {
        private ExpressionUnit left = null;
        private ExpressionUnit right = null;
        private String operator = null; //！ 或 || 或 &&
        private boolean value = false;

        //TODO: ...
        public boolean calculate() {
            if ("!".equals(operator)) {
                value = !right.calculate(); //TODO：？？？
            } else if ("&&".equals(operator)) {
                value = left.calculate() && right.calculate();
            } else if ("||".equals(operator)) {
                value = left.calculate() || right.calculate();
            }
            return value;
        }

        public List<OperationUnit> leftSubList = null;
        public List<OperationUnit> rightSubList = null;
        private String name = null;

        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer();
            if (leftSubList != null) {
                ret.append("[");
                ret.append(leftSubList.toString());
                ret.append("] ");
            }
            ret.append(name);
            ret.append(":");
            ret.append(operator);

            if (rightSubList != null) {
                ret.append(" [");
                ret.append(rightSubList.toString());
                ret.append("]");
            }
            return ret.toString();
        }
    }

    /**
     * 解析出各个独立的单元
     *
     * @param express
     * @return
     */
    private List<OperationUnit> formatExpress(String express) {
        List<OperationUnit> theStack = new ArrayList<>();
        StringBuffer element = new StringBuffer();
        int level = 0;
        OperationUnit unit;
        for (int iLoop = 0; iLoop < express.length(); iLoop++) {
            char alphabet = express.charAt(iLoop);
            switch (alphabet) {
                case '(': //单字节运算符
                    if (element.length() > 0) {
                        unit = new OperationUnit();
                        unit.element = element.toString();
                        unit.type = TYPE.VARIABLE;
                        unit.level = level;
                        theStack.add(unit);
                    }
                    unit = new OperationUnit();
                    unit.element = String.valueOf(alphabet);
                    unit.type = TYPE.LEFT_BRACKET;
                    level++; // ( 本身也属于下个level
                    unit.level = level;
                    theStack.add(unit);
                    element = new StringBuffer();
                    break;
                case ')':
                    if (element.length() > 0) {
                        unit = new OperationUnit();
                        unit.element = element.toString();
                        unit.type = TYPE.VARIABLE;
                        unit.level = level;
                        theStack.add(unit);
                    }
                    unit = new OperationUnit();
                    unit.element = String.valueOf(alphabet);
                    unit.type = TYPE.RIGHT_BRACKET;
                    unit.level = level;
                    theStack.add(unit);
                    level--;                    // ) 本身也属于上一个level.
                    element = new StringBuffer();
                    break;
                case '!':
                    if (element.length() > 0) {
                        unit = new OperationUnit();
                        unit.element = element.toString();
                        unit.type = TYPE.VARIABLE;
                        unit.level = level;
                        theStack.add(unit);
                    }
                    unit = new OperationUnit();
                    unit.element = String.valueOf(alphabet);
                    unit.type = TYPE.MONOCULAR;
                    unit.level = level;
                    theStack.add(unit);
                    element = new StringBuffer();
                    break;
                case '&':
                case '|':
                    //如果是2个||或者是2个&&，那么就是逻辑运算符
                    if (express.length() >= iLoop + 1 && express.charAt(iLoop + 1) == alphabet) {
                        if (element.length() > 0) {
                            unit = new OperationUnit();
                            unit.element = element.toString();
                            unit.type = TYPE.VARIABLE;
                            unit.level = level;
                            theStack.add(unit);
                        }
                        unit = new OperationUnit();
                        unit.element = String.valueOf(alphabet) + String.valueOf(alphabet);
                        unit.type = TYPE.BINOCULAR;
                        unit.level = level;
                        theStack.add(unit);
                        iLoop++;
                        element = new StringBuffer();
                    } else {
                        element.append(alphabet);
                    }
                    break;
                case ' ':        //去除空格
                case '\n':        //去除换行
                case '\r':        //去除回车
                    break;
                default:
                    element.append(alphabet);
                    break;
            }
        }
        //扫尾的字符串也要添加进去
        if (element.length() > 0) {
            String end = element.toString();
            if (")".equals(end)) {
                unit = new OperationUnit();
                unit.element = end;
                unit.type = TYPE.RIGHT_BRACKET;
                unit.level = 1; //必须是1
                theStack.add(unit);
            } else if ("!".equals(end) || "(".equals(end) || "|".equals(end) || "&".equals(end)) {
                System.err.println("结尾的字符不能是关键字。");
            } else {
                //变量
                unit = new OperationUnit();
                unit.element = end;
                unit.type = TYPE.VARIABLE;
                unit.level = 0; //必须是 0
                theStack.add(unit);
            }
        }
        return theStack;
    }

    /**
     * 去除前后端无用的括号
     *
     * @param list
     * @param minLevel -- 如果是-1，那么需要重新累计level值
     * @return
     */
    private List<OperationUnit> trimExpress(List<OperationUnit> list, int minLevel) {
        if (list.size() <= 1) { //无括号表达式
            return list;
        }
        if (list.get(0).type != TYPE.LEFT_BRACKET) { //开头不是左括号
            return list;
        }
        if (list.get(0).type == TYPE.LEFT_BRACKET && list.get(list.size() - 1).type != TYPE.RIGHT_BRACKET) {  //开头左括号，但结尾不是右括号
            return list;
        }
        if (minLevel == 0) { //最少括号数目是0，就是中间有非括号的情况 () + ()
            return list;
        }
        if (minLevel < 0) { //未知的最小括号数目，重新取得
            int tempLevel = Integer.MAX_VALUE;
            for (OperationUnit unit : list) {
                if (tempLevel > unit.level) {
                    tempLevel = unit.level;
                }
            }
            minLevel = tempLevel;
        }
        if (minLevel <= 0) { //最少括号数目是0，就是中间有非括号的情况 () + ()
            return list;
        }
        List<OperationUnit> newList = new ArrayList<>(); //依次拷贝到去除多余括号的数组中去
        for (int iLoop = 0; iLoop < list.size(); iLoop++) {
            OperationUnit unit = list.get(iLoop);
            if (iLoop < minLevel) {
                if (unit.type != TYPE.LEFT_BRACKET) { //非标准括号，可能是表达式不合格
                    System.err.println("括号个数不匹配");
                }
            } else if (iLoop >= list.size() - minLevel) {
                if (unit.type != TYPE.RIGHT_BRACKET) {
                    System.err.println("括号个数不匹配");
                }
            } else {
                unit.level = unit.level - minLevel;
                newList.add(unit);
            }
        }
        return newList;
    }

    private ExpressionUnit breakExpress(List<OperationUnit> list, ExpressionUnit current) throws RuleEngineException {
        list = trimExpress(list, -1);
        ExpressionUnit root = current;
        if (root == null) {
            root = new ExpressionUnit();
        }
        int firstOr = -1, firstAnd = -1, firstNot = -1;
        for (int iLoop = 0; iLoop < list.size(); iLoop++) {
            OperationUnit unit = list.get(iLoop);
            //取得括号外的内容（括号中的内容不作为划分的信息）
            if (unit.level == 0) {
                //双目运算符
                if (unit.type == TYPE.BINOCULAR) {
                    if (firstOr == -1 && "||".equals(unit.element)) {
                        firstOr = iLoop;
                        break;
                    }
                    if (firstAnd == -1 && "&&".equals(unit.element)) {
                        firstAnd = iLoop;
                    }
                } else if (unit.type == TYPE.MONOCULAR) {
                    if (firstNot == -1 && "!".equals(unit.element)) {
                        firstNot = iLoop;
                    }
                }
            }
        }
        if (firstOr > 0) {
            root.operator = list.get(firstOr).element;
            root.name = "OR";
            root.leftSubList = this.trimExpress(list.subList(0, firstOr), -1);
            root.left = breakExpress(root.leftSubList, root.left);
            root.rightSubList = this.trimExpress(list.subList(firstOr + 1, list.size()), -1);
            root.right = breakExpress(root.rightSubList, root.right);
            root.calculate();
        } else if (firstAnd > 0) {
            root.operator = list.get(firstAnd).element;
            root.name = "AND";
            root.leftSubList = this.trimExpress(list.subList(0, firstAnd), -1);
            root.left = breakExpress(root.leftSubList, root.left);
            root.rightSubList = this.trimExpress(list.subList(firstAnd + 1, list.size()), -1);
            root.right = breakExpress(root.rightSubList, root.right);
            root.calculate();
        } else if (firstNot >= 0) {
            root.operator = list.get(firstNot).element;
            root.name = "NOT";
            root.leftSubList = null;
            root.rightSubList = this.trimExpress(list.subList(firstNot + 1, list.size()), -1);
            root.right = breakExpress(root.rightSubList, root.right);

            root.calculate();
            //不带运算符的情况
        } else if (current == null && list.size() > 0) {
            if (list.get(0).type == TYPE.VARIABLE) {
                root.operator = list.get(0).element;
                root.name = "VARIABLE";
                AbstractRuleReader reader = this.getReaderService();
                RuleItem item = reader.getRuleItem(root.operator);
                try {
                    root.value = calculate(item, this.object);
                } catch (RuleEngineException e) {
                    log.debug(e.getLocalizedMessage());
                    throw e;
                }
            }
        }
        return root;
    }

    @Override
    public ItemExecutedResult doCheck(RuleItem item) throws RuleEngineException {
        ItemExecutedResult result = new ItemExecutedResult();
        if (StringUtils.isNotEmpty(item.getGroupExpress())) {
            List<OperationUnit> stack = formatExpress(item.getGroupExpress());
            ExpressionUnit root = breakExpress(stack, null);
            boolean bRet = root.calculate();
            //缺省认为是 passed
            result.setResult(RESULT.PASSED);    //通过
            result.setRemark(RESULT.PASSED.getName());
            result.setContinue(ItemExecutedResult.CONTINUE);
            result.setReturnValue(bRet);
            if (bRet) {
                result.setResult(item.getResult());
                result.setRemark(result.getResult().getName());
                result.setContinue(Integer.parseInt(item.getContinueFlag()));
            }
            this.executeCommand(item, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void executeCommand(RuleItem item, ItemExecutedResult result) throws RuleEngineException {
        Class<AbstractCommand> commandClass;
        try {
            if (StringUtils.isNotEmpty(item.getExecutor())) {
                commandClass = (Class<AbstractCommand>) Class.forName(item.getExecutor());
                AbstractCommand command = commandClass.newInstance();
                command.execute(item, result);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private AbstractRuleReader getReaderService() throws RuleEngineException {
        AbstractRuleReader reader;
        String serviceName = PropertyUtil.getProperty("rule.reader");
        if (StringUtils.isEmpty(serviceName)) {
            throw new RuleEngineException("rule.reader property must be set value.");
        }
        serviceName = serviceName.trim();
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
                    throw new RuleEngineException(e.getCause());
                }
                break;
        }
        return reader;
    }

    @SuppressWarnings("unchecked")
    private static boolean calculate(RuleItem item, Object object) throws RuleEngineException {
        String className = item.getExeClass();
        Class<AbstractRuleItem> auditClass;
        AbstractRuleItem auditInstance = null;
        if (StringUtils.isNotEmpty(className)) {
            try {
                auditClass = (Class<AbstractRuleItem>) Class.forName(className);
                if (null != auditClass) {
                    auditInstance = auditClass.newInstance();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuleEngineException(e.getCause());
            }
        }
        if (null == auditInstance) {
            auditInstance = new DefaultRuleExecutor();
        }
        //直接地调用doCheck的函数
        auditInstance.setObject(object);
        ItemExecutedResult result = auditInstance.doCheck(item);
        if (null != result) {
            return (result.getResult() == RESULT.PASSED);
        } else {
            throw new RuleEngineException("do check returns null pointer.");
        }
    }
}
