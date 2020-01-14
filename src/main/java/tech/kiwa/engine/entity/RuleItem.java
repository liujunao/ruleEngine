package tech.kiwa.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import tech.kiwa.engine.framework.OperatorFactory;

import java.io.Serializable;
import java.util.Date;

//规则列表 pojo
public class RuleItem implements Serializable {
    private static final long serialVersionUID = -4129428406038157150L;

    private String itemNo;
    private String content; //内容说明
    private String exeSql; //执行检查的SQL语句
    private String exeClass; //执行检查的java类名， 与exe_sql二者只填写一项
    private String paramName; //exe_sql 或 exe_class 参数，多个参数用逗号（,）分割
    private String paramType; //exe_sql 或 exe_class 参数类型，多个类型用逗号（,）分割，与 param_name 一一对应
    private String comparisonCode; //01 == ，02 > ，03 < ，04 != ，05 >= ，06 <= ，07 include ，08 exclude ，09 included by，10 excluded by，11 equal, 12 not equal，13 equalIgnoreCase
    private String comparisonValue; //=,>,<,>=,<=, !=, include, exclude等
    private String baseline; //参数值，比较目标值
    private String result; //1 通过, 2 关注, 3 拒绝,  逻辑运算满足目标值的时候读取改内容
    private String executor; //结果执行后的被执行体，从 AbstractCommand 中继承下来
    private String priority; //执行的优先顺序，值大的优先执行
    private String continueFlag; //1: 继续执行下一条, 其他：中断执行
    private String parentItemNo; //如果是子规则，则填写父规则的 item_no
    private String groupExpress; //同一 PARENT_ITEM 的各 ITEM 运算表达式 ( A AND B OR C)
    private Object attach; //remark？？？
    private String comments;
    private String enableFlag; //是否有效，enable_flag = 1表示有效，其余: 无效
    private Date createTime;
    private Date updateTime;

    /**
     * 根据 xml 指定 name 来赋值
     *
     * @param name
     * @param value
     */
    public void setMappedValue(String name, String value) {
        switch (name.toLowerCase()) {
            case "itemno":
            case "ruleid":
            case "id":
                this.itemNo = value;
                break;
            case "auditdesc":
            case "content":
                this.content = value;
                break;
            case "exe_sql":
                this.exeSql = value;
                break;
            case "param_name":
                this.paramName = value;
                break;
            case "param_type":
                this.paramType = value;
                break;
            case "java_class":
            case "exe_class":
                this.exeClass = value;
                break;
            case "executor":
            case "command":
                this.executor = value;
                break;
            case "comments":
                this.comments = value;
                break;
            case "attach":
                this.attach = value;
                break;
            case "logic_key":
            case "comparison_code":
                this.comparisonCode = value;
                if (null != value && comparisonValue == null) {
                    this.comparisonValue = OperatorFactory.OPR_CODE.getValue(value);
                }
                break;
            case "logic_value":
            case "comparison_value":
                this.comparisonValue = value;
                if (null != value && comparisonCode == null) {
                    this.comparisonCode = OperatorFactory.OPR_CODE.getCode(value);
                }
                break;
            case "baseline":
                this.baseline = value;
                break;
            case "result_key":
            case "result":
                this.result = value;
                break;

            case "priority":
                this.priority = value;
                break;
            case "enable_flag":
                this.enableFlag = value;
                break;
            case "continue_flag":
                this.continueFlag = value;
                break;
            case "parent_item_no":
            case "parent":
                this.parentItemNo = value;
                break;
            case "group_express":
            case "parent_express":
                this.groupExpress = value;
                break;
            default:
                break;
        }
    }

    /**
     * 获取当前规则的 itemNo
     *
     * @param name
     * @return
     */
    public Object getValue(String name) {
        Object value = null;
        if (name.equals("itemno")) {
            value = this.itemNo;
        }
        return value;
    }

    //setter getter
    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = (itemNo == null ? null : itemNo.trim());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = (content == null ? null : content.trim());
    }

    public String getExeSql() {
        return exeSql;
    }

    public void setExeSql(String exeSql) {
        this.exeSql = (exeSql == null ? null : exeSql.trim());
    }

    public String getExeClass() {
        return exeClass;
    }

    public void setExeClass(String exeClass) {
        this.exeClass = (exeClass == null ? null : exeClass.trim());
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = (paramName == null ? null : paramName.trim());
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = (paramType == null ? null : paramType.trim());
    }

    public String getComparisonCode() {
        return comparisonCode;
    }

    public void setComparisonCode(String comparisonCode) {
        this.comparisonCode = (comparisonCode == null ? null : comparisonCode.trim());
    }

    public String getComparisonValue() {
        return comparisonValue;
    }

    public void setComparisonValue(String comparisonValue) {
        this.comparisonValue = (comparisonValue == null ? null : comparisonValue.trim());
    }

    public String getBaseline() {
        return baseline;
    }

    public void setBaseline(String baseline) {
        this.baseline = (baseline == null ? null : baseline.trim());
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = (result == null ? null : result.trim());
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = (executor == null ? null : executor.trim());
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = (priority == null ? null : priority.trim());
    }

    public String getContinueFlag() {
        return continueFlag;
    }

    public void setContinueFlag(String continueFlag) {
        this.continueFlag = (continueFlag == null ? null : continueFlag.trim());
    }

    public String getParentItemNo() {
        return parentItemNo;
    }

    public void setParentItemNo(String parentItemNo) {
        this.parentItemNo = (parentItemNo == null ? null : parentItemNo.trim());
    }

    public String getGroupExpress() {
        return groupExpress;
    }

    public void setGroupExpress(String groupExpress) {
        this.groupExpress = (groupExpress == null ? null : groupExpress.trim());
    }

    public Object getAttach() {
        return attach;
    }

    public void setAttach(Object attachment) {
        if (attachment instanceof String) {
            this.attach = (attachment == null ? null : ((String) attachment).trim());
        } else {
            this.attach = attachment;
        }
    }

    public void setRemark(String remark) {
        this.attach = (remark == null ? null : remark.trim());
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = (comments == null ? null : comments.trim());
    }

    public String getEnableFlag() {
        return enableFlag;
    }

    public void setEnableFlag(String enableFlag) {
        this.enableFlag = (enableFlag == null ? null : enableFlag.trim());
    }

    public void setEnableFlag(boolean enableFlag) {
        this.enableFlag = (enableFlag ? "1" : "2");
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
