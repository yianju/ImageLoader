package kotlin.studio.com.myapplication.sql.update;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Copyright  : Copyright (c) 2016
 * Company    : Android
 * Author     : 关羽
 * Date       : 2018-07-24 16:22
 */
public class CreateDb {

    /**
     * 数据库表名
     */
    private String name;

    /**
     * 创建表的sql语句集合
     */
    private List<String> sqlCreates;

    public CreateDb(Element ele) {
        name = ele.getAttribute("name");

        {
            sqlCreates = new ArrayList<String>();
            NodeList sqls = ele.getElementsByTagName("sql_createTable");
            for (int i = 0; i < sqls.getLength(); i++) {
                String sqlCreate = sqls.item(i).getTextContent();
                this.sqlCreates.add(sqlCreate);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSqlCreates() {
        return sqlCreates;
    }

    public void setSqlCreates(List<String> sqlCreates) {
        this.sqlCreates = sqlCreates;
    }

}
