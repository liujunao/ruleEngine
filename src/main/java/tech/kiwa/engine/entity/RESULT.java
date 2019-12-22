package tech.kiwa.engine.entity;

//这个枚举类没看懂
public enum RESULT {
    EMPTY(0), PASSED(1), CONCERNED(2), REJECTED(3), WAIT(4), SELFDEFINE(5);

    RESULT() {
    }

    RESULT(int value) {
        this.setValue(value);
    }

    //结果类型
    private int value = 0; //数字表示 == 字符串表示
    private String defaultDesc = ""; //字符串表示 == 数字表示

    public void setValue(int value) {
        switch (value) {
            case 1:
                defaultDesc = "PASSED";
                break;
            case 2:
                defaultDesc = "CONCERNED";
                break;
            case 3:
                defaultDesc = "REJECTED";
                break;
            case 4:
                defaultDesc = "WAIT";
                break;
            case 5:
                defaultDesc = "SELFDEFINE";
                break;
            default:
                break;
        }
        this.value = value;
    }

    public boolean parse(String value) {
        boolean bRet = true;
        value = value.toUpperCase();
        switch (value) {
            case "PASSED":
            case "RESULT.PASSED":
                this.value = 1;
                this.defaultDesc = "PASSED";
                break;
            case "CONCERNED":
            case "RESULT.CONCERNED":
                this.value = 2;
                this.defaultDesc = "CONCERNED";
                break;
            case "REJECTED":
            case "RESULT.REJECTED":
                this.value = 3;
                this.defaultDesc = "REJECTED";
                break;
            case "WAIT":
            case "RESULT.WAIT":
                this.value = 4;
                this.defaultDesc = "WAIT";
                break;
            case "SELFDEFINE":
            case "RESULT.SELFDEFINE":
                this.value = 5;
                this.defaultDesc = "SELFDEFINE";
                break;
            default:
                bRet = false;
                break;
        }
        return bRet;
    }

    public int compare(RESULT target) {
        return this.value - target.value;
    }

    public static RESULT valueOf(int value) {
        RESULT result = RESULT.EMPTY;
        result.setValue(value);
        return result;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return defaultDesc;
    }

    public void setName(String defaultDesc) {
        this.parse(defaultDesc);
    }
}