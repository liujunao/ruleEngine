package tech.kiwa.engine.component.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tech.kiwa.engine.component.AbstractRuleReader;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.utility.PropertyUtil;

//xml 读取规则
public class XMLRuleReader extends AbstractRuleReader {
    private static Logger log = LoggerFactory.getLogger(XMLRuleReader.class);

    private List<RuleItem> itemList = null; //存放 xml 中获取的规则列表

    @Override
    public List<RuleItem> readRuleItemList() throws RuleEngineException {
        if (!ruleItemCache.isEmpty()) { //缓存加载
            return ruleItemCache;
        }
        itemList = new ArrayList<>();
        try {
            // 创建 DOM 文档对象
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String configFile = PropertyUtil.getProperty("xml.rule.filename"); //xml 文件的位置属性
            //File.separator： 文件分隔符：/ 或 \
            if (!configFile.startsWith(File.separator)) { //安全校验：不以 / 开头
                File dir = new File(PropertyUtil.class.getClassLoader().getResource("").getPath()); // 路径：xxx/RuleEngine/target/classes/
                configFile = dir + File.separator + configFile; //xml 配置文件的绝对路径
            }
            Document doc = builder.parse(new File(configFile));
            // 获取包含类名的文本节点
            NodeList ruleList = doc.getElementsByTagName("rule"); //获取规则的所有 rule 标签
            for (int iLoop = 0; iLoop < ruleList.getLength(); iLoop++) { //rule 标签的循环
                RuleItem item = new RuleItem();
                Node rule = ruleList.item(iLoop);
                NamedNodeMap ruleAttr = rule.getAttributes();
                //rule 标签下的 id 属性标签
                if (null == ruleAttr || ruleAttr.getNamedItem("id") == null) {
                    log.debug("rule id must not be null. rule.context = {}", rule.getTextContent());
                    return null;
                }
                //获取 rule 标签中的 id 属性值
                String xmlRuleId = ruleAttr.getNamedItem("id").getNodeValue();
                item.setItemNo(xmlRuleId);
                //这个 for 循环没看懂？？？
//                for (int jLoop = 0; jLoop < ruleAttr.getLength(); jLoop++) {
//                    Node node = ruleAttr.item(jLoop); //????
//                    item.setMappedValue(node.getNodeName(), node.getNodeValue());
//                }
                //rule 标签下的其他属性标签
                if (ruleAttr.getNamedItem("class") != null) {
                    item.setExeClass(ruleAttr.getNamedItem("class").getNodeValue());
                }
                if (ruleAttr.getNamedItem("method") != null) {
                    item.setExecutor(rule.getAttributes().getNamedItem("method").getNodeValue());
                }
                if (ruleAttr.getNamedItem("parent") != null) {
                    item.setParentItemNo(rule.getAttributes().getNamedItem("parent").getNodeValue());
                }
                //获取子节点 property 的属性值
                if (rule.hasChildNodes()) {
                    Node child = rule.getFirstChild();
                    while (child != null) { //遍历
                        if ("property".equalsIgnoreCase(child.getNodeName())) {
                            NamedNodeMap childAttrs = child.getAttributes();
                            Node nameNode = childAttrs.getNamedItem("name");
                            Node valueNode = childAttrs.getNamedItem("value");
                            if (valueNode == null || nameNode == null) {
                                throw new RuleEngineException("rule format error, attribute value or name must existed.");
                            }
                            item.setMappedValue(nameNode.getNodeValue(), valueNode.getNodeValue());
                            //exe_sql 或 exe_class 参数与参数类型
                            Node typeNode = childAttrs.getNamedItem("type");
                            if ("param".equalsIgnoreCase(nameNode.getNodeValue())) {
                                item.setParamName(valueNode.getNodeValue());
                                item.setParamType(typeNode.getNodeValue());
                            }
                            if ("comparison".equalsIgnoreCase(nameNode.getNodeValue())) {
                                Node codeNode = childAttrs.getNamedItem("code");
                                item.setComparisonCode(codeNode.getNodeValue());
                                item.setComparisonValue(valueNode.getNodeValue());
                            }
                        }
                        child = child.getNextSibling(); //下一个儿子节点
                    }
                }
                if (!preCompile(item)) { //检查规则的格式是否正确
                    log.debug("xml rule format error.");
                    throw new RuleEngineException("rule format error.");
                }
                itemList.add(item);
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        }
        synchronized (ruleItemCache) {
            ruleItemCache.addAll(itemList);
        }
        return ruleItemCache;
    }

    @Override
    public Long getRuleItemCount() throws RuleEngineException {
        if (itemList == null) { //为空说明 readRuleItemList 未调用，无法获取规则数量
            this.readRuleItemList();
            return (long) itemList.size();
        }
        return 0L;
    }

    @Override
    public RuleItem getRuleItem(String ruleId) throws RuleEngineException {
        if (itemList == null) { //为空，说明未加载 xml，此时加载 xml
            this.readRuleItemList();
        }
        for (RuleItem rule : itemList) {
            if (rule.getItemNo().equalsIgnoreCase(ruleId)) {
                return rule;
            }
        }
        //TODO：当获取不到，是否需要再次重新读取 xml 文件
        return null;
    }
}