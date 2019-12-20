package tech.kiwa.engine.entity;

public class EngineRunResult {
    private RESULT result = RESULT.EMPTY;
    private String result_desc;
    private String sequence;

    public RESULT getResult() {
        return result;
    }

    public void setResult(RESULT result) {
        this.result = result;
    }

    public void setResult(String result) {
        boolean bRet = this.result.parse(result);
        if (!bRet) {
            try {
                this.result.setValue(Integer.parseInt(result));
            } catch (NumberFormatException e) {
            }
        }
    }

    public void setResult(int result) {
        this.result.setValue(result);
    }

    public String getResult_desc() {
        return result_desc;
    }

    public void setResult_desc(String result_desc) {
        this.result_desc = result_desc;
        this.result.parse(result_desc);
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
