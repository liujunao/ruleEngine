package tech.kiwa.engine.component.drools;

import com.alibaba.druid.util.StringUtils;

public class PackageCreator implements DroolsPartsCreator {
    private String packageName = null;

    @Override
    public String toJavaString() {
        StringBuffer javaBuffer = new StringBuffer();
        javaBuffer.append("package ");
        javaBuffer.append(packageName);
        javaBuffer.append(";\n");

        return javaBuffer.toString();
    }

    public String getName() {
        return packageName;
    }

    private PackageCreator() {

    }

    @SuppressWarnings("unused")
    private DroolsBuilder builder = null;

    public static PackageCreator create(String content, DroolsBuilder builder) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (!content.startsWith("package")) {
            return null;
        }
        content = content.trim();
        PackageCreator creator = new PackageCreator();
        creator.builder = builder;
        String[] sections = content.split("\\s+|\\t|\\n|\\r");
        if (sections.length >= 2) {
            if (sections[1].endsWith(";")) {
                sections[1] = sections[1].substring(0, sections[1].length() - 1);
            }
            creator.packageName = sections[1].trim();
        } else {
            creator.packageName = null;
        }
        return creator;
    }
}
