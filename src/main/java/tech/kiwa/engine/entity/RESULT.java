package tech.kiwa.engine.entity;

//枚举运行结果
public enum RESULT {
    EMPTY(0), PASSED(1), CONCERNED(2), REJECTED(3), WAIT(4), SELFDEFINE(5);

    RESULT() {
    }

    RESULT(int value) {
        this.typeFromIntToString(value);
    }

    //结果类型
    private int value = 0; //数字表示
    private String defaultDesc = ""; //字符串表示

    /**
     * 将 int 表示的类型转换为字符串表示的类型
     *
     * @param value
     */
    public void typeFromIntToString(int value) {
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

    /**
     * 将字符串表示的结果类型转换为 int 表示的结果类型
     *
     * @param value
     * @return false：不存在该结果类型
     */
    public boolean typeFromStringToInt(String value) {
        boolean bRet = true;
        value = value.toUpperCase();
        switch (value) {
            case "PASSED":
                this.value = 1;
                break;
            case "RESULT.PASSED":
                this.value = 1;
                this.defaultDesc = "PASSED";
                break;
            case "CONCERNED":
                this.value = 2;
                break;
            case "RESULT.CONCERNED":
                this.value = 2;
                this.defaultDesc = "CONCERNED";
                break;
            case "REJECTED":
                this.value = 3;
                break;
            case "RESULT.REJECTED":
                this.value = 3;
                this.defaultDesc = "REJECTED";
                break;
            case "WAIT":
                this.value = 4;
                break;
            case "RESULT.WAIT":
                this.value = 4;
                this.defaultDesc = "WAIT";
                break;
            case "SELFDEFINE":
                this.value = 5;
                break;
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
        result.typeFromIntToString(value);
        return result;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return defaultDesc;
    }

    public void setName(String defaultDesc) {
        this.typeFromStringToInt(defaultDesc);
    }
}