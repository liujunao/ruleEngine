package tech.kiwa.engine.component.drools;

import com.alibaba.druid.util.StringUtils;

public class ImportCreator implements DroolsPartsCreator {
    String fullName;
    String simpleName;
    @SuppressWarnings("unused")
    private DroolsBuilder builder = null;

    public String getFullName() {
        return fullName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String toString() {
        return "import " + fullName + ";\n";
    }

    @Override
    public String toJavaString() {
        return "import " + fullName + ";\n";
    }

    private ImportCreator() {
    }

    public static ImportCreator create(String content, DroolsBuilder builder) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (!content.startsWith("import")) {
            return null;
        }
        content = content.trim();
        ImportCreator creator = new ImportCreator();
        creator.builder = builder;
        String[] sections = content.split("\\s+|\\t|\\n|\\r");
        if (sections.length >= 2) {
            creator.fullName = sections[1].trim();
            if (creator.fullName.endsWith(";")) {
                creator.fullName = creator.fullName.substring(0, creator.fullName.length() - 1);
            }
            int pos = creator.fullName.lastIndexOf('.');
            if (pos > 0) {
                creator.simpleName = creator.fullName.substring(pos + 1);
            }
        }
        return creator;
    }
}
