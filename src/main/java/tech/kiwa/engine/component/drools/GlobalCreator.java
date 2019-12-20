package tech.kiwa.engine.component.drools;

import com.alibaba.druid.util.StringUtils;

public class GlobalCreator implements DroolsPartsCreator {
    private String name = null;
    private String reference = null;
    private Object value = null;

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

    @Override
    public String toJavaString() {
        StringBuffer sbf = new StringBuffer();
        sbf.append("public static ");
        sbf.append(reference);
        sbf.append(" ");
        sbf.append(name);
        sbf.append(" ;\n");

        return sbf.toString();
    }

    private GlobalCreator() {

    }

    @SuppressWarnings("unused")
    private DroolsBuilder builder = null;

    public static GlobalCreator create(String content, DroolsBuilder builder) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (!content.startsWith("global")) {
            return null;
        }
        content = content.trim();
        GlobalCreator creator = new GlobalCreator();
        creator.builder = builder;
        String[] sections = content.split("\\s+|\\t|\\n|\\r");
        if (sections.length >= 3) {
            creator.name = sections[2].trim();
            creator.reference = sections[1].trim();
        }
        return creator;
    }
}
