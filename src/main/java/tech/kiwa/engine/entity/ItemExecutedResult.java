package tech.kiwa.engine.entity;

public class ItemExecutedResult {
    //TODO: 这三个参数的作用？？？
    public static final int CONTINUE = 1;
    public static final int LOOP = 2;
    public static final int BROKEN = 3;

    private RESULT result = RESULT.EMPTY;
    private String remark;
    private boolean returnValue;
    private int continueFlag = CONTINUE;  //默认可以继续

    public boolean getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

    public RESULT getResult() {
        return result;
    }

    public void setResult(RESULT result) {
        this.result = result;
        if (this.result == RESULT.WAIT) {
            continueFlag = BROKEN;
        }
    }

    //TODO：多此一举？？？
    public void setResult(String result) {
        int iResult = Integer.parseInt(result);
        this.setResult(iResult);
    }

    public void setResult(int result) {
        this.result.typeFromIntToString(result);
        if (this.result == RESULT.WAIT) {
            continueFlag = BROKEN;
        }
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean canBeContinue() {
        return continueFlag == CONTINUE;
    }

    public void setContinue(int contin) {
        this.continueFlag = contin;
    }

    public boolean shouldLoop() {
        return continueFlag == LOOP;
    }
}