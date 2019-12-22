package tech.kiwa.engine.component.drools;

import com.alibaba.druid.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCreator implements DroolsPartsCreator {
    private String functionName;
    private String reference;
    private Map<String, String> params = new HashMap<>();
    private List<String> paramTypes = new ArrayList<>();
    private List<String> paramNames = new ArrayList<>();
    private String content;
    private DroolsBuilder builder = null;

    public String getFunctionName() {
        return functionName;
    }

    public String getReference() {
        return reference;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toJavaString() {
        StringBuffer javaBuffer = new StringBuffer();
        javaBuffer.append(builder.getPackage().toJavaString());
        javaBuffer.append("\n");
        for (ImportCreator impt : builder.getImportList()) {
            javaBuffer.append(impt.toJavaString());
        }
        for (DeclareCreator decl : builder.getDeclareList()) {
            javaBuffer.append("import " + builder.getPackage().getName() + ".");
            javaBuffer.append(decl.getName());
            javaBuffer.append(";\n");
        }
        javaBuffer.append("\n");
        javaBuffer.append("public class ").append("DroolsFunctions");
        javaBuffer.append(" {").append("\n");
        javaBuffer.append("\n");
        for (FunctionCreator fun : builder.getFunctionList()) {
            javaBuffer.append("  public ");
            javaBuffer.append(fun.reference);
            javaBuffer.append(" ");
            javaBuffer.append(fun.functionName);
            javaBuffer.append(" ");
            javaBuffer.append(" (");
            for (int iLoop = 0; iLoop < fun.paramNames.size(); iLoop++) {
                String name = fun.paramNames.get(iLoop);
                String type = fun.paramTypes.get(iLoop);
                javaBuffer.append(type);
                javaBuffer.append(" ");
                javaBuffer.append(name);
                javaBuffer.append(" , ");
            }
            if (!fun.paramNames.isEmpty()) {
                int pos = javaBuffer.lastIndexOf(",");
                javaBuffer.deleteCharAt(pos);
            }
            javaBuffer.append(")");
            javaBuffer.append(fun.content);
            javaBuffer.append("\n");
        }
        javaBuffer.append("\n");
        javaBuffer.append("}");
        return javaBuffer.toString();
    }

    private FunctionCreator() {
    }

    public static FunctionCreator create(String content, DroolsBuilder builder) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        if (!content.startsWith("function")) {
            return null;
        }
        content = content.trim();
        FunctionCreator creator = new FunctionCreator();
        creator.builder = builder;

        String[] lines = content.split("\\n|\\r");
        String title = lines[0].trim();
        String[] sections = title.split("\\s+|\\t|\\(|,|\\)\\{");
        if (sections.length >= 3) {
            creator.reference = sections[1];
            creator.functionName = sections[2];
            if (title.endsWith("{")) {
                title = title.substring(0, title.length() - 1);
            }
            if (title.endsWith(")")) {
                String type = null, name = null;
                for (int iLoop = 3; iLoop < sections.length; iLoop++) {
                    if (StringUtils.isEmpty(sections[iLoop])) {
                        continue;
                    }
                    if (type == null) {
                        type = sections[iLoop];
                    } else {
                        name = sections[iLoop];
                    }
                    if (type != null && name != null) {
                        creator.params.put(name, type);
                        creator.paramTypes.add(type);
                        creator.paramNames.add(name);
                        type = null;
                        name = null;
                    }
                }
            }
            int pos = content.indexOf('{');
            creator.content = content.substring(pos);
        }
        return creator;
    }
}
