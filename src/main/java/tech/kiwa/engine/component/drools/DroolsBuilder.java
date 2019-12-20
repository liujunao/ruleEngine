package tech.kiwa.engine.component.drools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.utility.JavaStringCompiler;

public class DroolsBuilder {
    private Logger log = LoggerFactory.getLogger(DroolsBuilder.class);

    private List<FunctionCreator> functionList = new ArrayList<>();
    private List<GlobalCreator> globalList = new ArrayList<>();
    private List<ImportCreator> importList = new ArrayList<>();
    private List<QueryCreator> queryList = new ArrayList<>();
    private List<RuleCreator> ruleList = new ArrayList<>();
    private List<DeclareCreator> declareList = new ArrayList<>();
    private JavaStringCompiler compiler = new JavaStringCompiler();
    private PackageCreator pack = null;
    private boolean compiled = false;

    public void build(List<String> phases) {
        for (String item : phases) {
            item = item.trim();
            if (item.startsWith("rule")) {
                ruleList.add(RuleCreator.create(item, this));
            } else if (item.startsWith("function")) {
                functionList.add(FunctionCreator.create(item, this));
            } else if (item.startsWith("global")) {
                this.globalList.add(GlobalCreator.create(item, this));
            } else if (item.startsWith("import")) {
                this.importList.add(ImportCreator.create(item, this));
            } else if (item.startsWith("query")) {
                this.queryList.add(QueryCreator.create(item, this));
            } else if (item.startsWith("package")) {
                this.pack = PackageCreator.create(item, this);
            } else if (item.startsWith("declare")) {
                this.declareList.add(DeclareCreator.create(item, this));
            }
        }
    }

    public List<RuleItem> getRuleItemList() {
        List<RuleItem> retList = new ArrayList<RuleItem>();
        for (RuleCreator creator : ruleList) {
            retList.add(creator.getItem());
        }
        return retList;
    }

    public boolean compile() {
        if (compiled) {
            return compiled;
        }
        for (DeclareCreator declare : declareList) {
            try {
                String fileName = declare.getName() + ".java";
                compiler.compile(fileName, declare.toJavaString());
            } catch (IOException e) {

            }
        }
        if (!functionList.isEmpty()) {
            String fileName = "DroolsFunctions.java";
            try {
                compiler.compile(fileName, functionList.get(0).toJavaString());
            } catch (IOException e) {
                log.debug(e.getMessage());
                e.printStackTrace();
            }
        }
        return compiled;
    }

    @SuppressWarnings({"unchecked"})
    public DroolsPartsObject createObject(String className, String javaContent) {
        try {
            String fileName = "";
            int pos = className.lastIndexOf('.');
            if (pos > 0) {
                fileName = className.substring(pos + 1) + ".java";
            } else {
                fileName = className + ".java";
            }
            Map<String, byte[]> clsMap = compiler.compile(fileName, javaContent);
            Class<DroolsPartsObject> cls = (Class<DroolsPartsObject>) compiler.loadClass(className, clsMap);
            DroolsPartsObject obj = (DroolsPartsObject) cls.newInstance();

            return obj;
        } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<FunctionCreator> getFunctionList() {
        return functionList;
    }

    public List<GlobalCreator> getGlobalList() {
        return globalList;
    }

    public List<ImportCreator> getImportList() {
        return importList;
    }

    public List<QueryCreator> getQueryList() {
        return queryList;
    }

    public List<RuleCreator> getRuleList() {
        return ruleList;
    }

    public List<DeclareCreator> getDeclareList() {
        return declareList;
    }

    public PackageCreator getPackage() {
        return pack;
    }
}
