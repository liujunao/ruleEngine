package tech.kiwa.engine.component.drools;

public class LocalCreator implements DroolsPartsCreator {
    private String name = null;
    private String reference = null;
    private Object value = null;

    public LocalCreator(String name, String reference) {
        if (null != name) {
            this.name = name.trim();
        }
        if (null != reference) {
            this.reference = reference.trim();
        }
    }

    public LocalCreator() {
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public void setName(String name) {
        if (null != name) {
            this.name = name.trim();
        }
    }

    public void setReference(String reference) {
        if (null != reference) {
            this.reference = reference.trim();
        }
    }

    @Override
    public String toJavaString() {
        StringBuffer sbf = new StringBuffer();
        sbf.append("private ");
        sbf.append(reference);
        sbf.append(" ");
        sbf.append(name);
        sbf.append(" ;\n");

        return sbf.toString();
    }
}
