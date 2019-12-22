package tech.kiwa.engine.component.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kiwa.engine.component.AbstractRuleReader;
import tech.kiwa.engine.entity.RuleItem;
import tech.kiwa.engine.exception.RuleEngineException;
import tech.kiwa.engine.framework.DBAccesser;
import tech.kiwa.engine.utility.PropertyUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//处理 database 规则
public class DBRuleReader extends AbstractRuleReader {
    private static Logger log = LoggerFactory.getLogger(DBRuleReader.class);

    private static volatile DBAccesser accesser = null; //数据库访问类

    /**
     * 初始化 accesser（单例模式）
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private DBAccesser loadDBAccesser() throws Exception {
        if (accesser == null) {
            String className = PropertyUtil.getProperty("db.accesser"); //db.accesser 配置数据连接方式
            Class<DBAccesser> dbClass = (Class<DBAccesser>) Class.forName(className);
            synchronized (DBAccesser.class) { //单例模式
                if (accesser == null) {
                    accesser = dbClass.newInstance(); //弱 new，只能调用无参构造器
                }
            }
        }
        return accesser;
    }

    /**
     * 读取规则列表并放入缓存
     *
     * @return
     * @throws RuleEngineException
     */
    @Override
    public List<RuleItem> readRuleItemList() throws RuleEngineException {
        Statement stmt = null;
        ResultSet res = null;
        if (!ruleItemCache.isEmpty()) { //缓存若已加载，则直接返回
            return ruleItemCache;
        }
        List<RuleItem> retList = new ArrayList<>();
        //db.rule.table 为设置的表名；ENABLE_FLAG 为 1 表示有效；PRIORITY 表示执行的优先顺序，值大的优先执行
        String sqlStr = " select * from " + PropertyUtil.getProperty("db.rule.table") + " where ENABLE_FLAG = 1 order by PRIORITY desc ";
        try {
            loadDBAccesser(); //初始化 accesser，用于访问数据库
            Connection conn = accesser.getConnection(); //获取数据库连接
            stmt = conn.prepareStatement(sqlStr);
            res = stmt.executeQuery(sqlStr);
            while (res.next()) {
                RuleItem rule = new RuleItem();
                setterRuleItem(rule, res); //setter 赋值
                if (!preCompile(rule)) { //检查规则的格式是否正确
                    log.debug("database rule format error.");
                    throw new RuleEngineException("rule format error.");
                }
                retList.add(rule);
            }
            res.close();
            stmt.close();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        }
        //添加到缓存
        synchronized (ruleItemCache) {
            ruleItemCache.addAll(retList);
        }
        return ruleItemCache;
    }

    /**
     * 获取规则的数量
     *
     * @return
     * @throws RuleEngineException
     */
    @Override
    public Long getRuleItemCount() throws RuleEngineException {
        Statement stmt = null;
        ResultSet res = null;
        long count = 0;
        String sqlStr = " select count(*) from " + PropertyUtil.getProperty("db.rule.table") + " where ENABLE_FLAG = 1 ";
        try {
            loadDBAccesser();
            Connection conn = accesser.getConnection();
            stmt = conn.prepareStatement(sqlStr);
            res = stmt.executeQuery(sqlStr);
            res.next();
            count = res.getLong(1);
            res.close();
            stmt.close();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        }
        return count;
    }

    /**
     * 获取指定 item_no 的规则项
     *
     * @param ruleId
     * @return
     * @throws RuleEngineException
     */
    @Override
    public RuleItem getRuleItem(String ruleId) throws RuleEngineException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<RuleItem> retList = new ArrayList<RuleItem>();
        String sqlStr = " select * from " + PropertyUtil.getProperty("db.rule.table") + " where ENABLE_FLAG = 1  and ITEM_NO = ? ";
        try {
            loadDBAccesser();
            Connection conn = accesser.getConnection();
            stmt = conn.prepareStatement(sqlStr);
            stmt.setString(1, ruleId);
            res = stmt.executeQuery();
            while (res.next()) {
                RuleItem rule = new RuleItem();
                setterRuleItem(rule, res); //setter 赋值
                if (!preCompile(rule)) { //检查规则的格式是否正确
                    log.debug("database rule format error.");
                    throw new RuleEngineException("rule format error.");
                }
                retList.add(rule);
                // 只读一条记录？？？
                // item_no 不应该限制了只存在一条记录吗。。。
                break;
            }
            res.close();
            stmt.close();
        } catch (SQLException e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new RuleEngineException(e.getCause());
        }
        if (retList.isEmpty()) {
            return null;
        }
        return retList.get(0);
    }

    //RuleItem 的 setter 赋值
    private RuleItem setterRuleItem(RuleItem rule, ResultSet res) throws SQLException {
        rule.setItemNo(res.getString("ITEM_NO"));
        rule.setContent(res.getString("content"));
        rule.setExeSql(res.getString("EXE_SQL"));
        rule.setExeClass(res.getString("exe_class"));
        rule.setParamType(res.getString("param_type"));
        rule.setParamName(res.getString("PARAM_NAME"));
        rule.setComparisonCode(res.getString("comparison_code"));
        rule.setComparisonValue(res.getString("comparison_value"));
        rule.setBaseline(res.getString("BASELINE"));
        rule.setResult(res.getString("result"));
        rule.setExecutor(res.getString("EXECUTOR"));
        rule.setPriority(res.getString("PRIORITY"));
        rule.setParentItemNo(res.getString("PARENT_ITEM_NO"));
        rule.setGroupExpress(res.getString("group_express"));
        rule.setContinueFlag(res.getString("CONTINUE_FLAG"));
        rule.setRemark(res.getString("REMARK"));
        rule.setComments(res.getString("COMMENTS"));
        rule.setEnableFlag(res.getString("ENABLE_FLAG"));
        rule.setCreateTime(res.getDate("CREATE_TIME"));
        rule.setUpdateTime(res.getDate("UPDATE_TIME"));
        return rule;
    }
}