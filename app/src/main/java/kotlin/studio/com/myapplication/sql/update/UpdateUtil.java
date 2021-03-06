package kotlin.studio.com.myapplication.sql.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kotlin.studio.com.myapplication.app.App;
import kotlin.studio.com.myapplication.sql.bean.User;
import kotlin.studio.com.myapplication.sql.dao.UserDao;
import kotlin.studio.com.myapplication.sql.factory.DaoManagerFactory;
import kotlin.studio.com.myapplication.utils.FileUtil;

/**
 * Description:
 * Copyright  : Copyright (c) 2016
 * Company    : Android
 * Author     : 关羽
 * Date       : 2018/7/29 15:42
 */
public class UpdateUtil {
    private static final String INFO_FILE_DIV = "/";
    private List<User> userList;
    private File parentFile = new File(App.getInstance().getDataBasePath(), "user");
    private File bakFile    = new File(parentFile, "backUpDb");

    public UpdateUtil() {
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!bakFile.exists()) {
            bakFile.mkdirs();
        }
    }

    /**
     * 检查数据库变化
     * @param context
     */
    public void checkThisVersionTable(Context context) {
        UserDao userDao = DaoManagerFactory.getInstance().getDataHelper(UserDao.class, User.class);

        userList = userDao.query(new User());

//        //读物升级xml脚本信息
//        UpdateDbXml xml = readDbXml(context);
//
//        String thisVersion = this.getVersionName(context);
//        //获取对应版本的建表脚本
//        CreateVersion thisCreateVersion = analyseCreateVersion(xml, thisVersion);
//        try {
//            //根据建表脚本,核实一遍应该存在的表
//            executeCreateVersion(thisCreateVersion, true);
//        } catch (Exception e) {
//        }
    }


    /**
     * 读取升级xml
     * @param context
     * @return
     */
    private UpdateDbXml readDbXml(Context context) {
        InputStream is = null;
        Document document = null;
        try {
            is = context.getAssets().open("updateXml.xml");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (document == null) {
            return null;
        }

        UpdateDbXml xml = new UpdateDbXml(document);

        return xml;
    }

    /**
     * 解析出对应版本的建表脚本
     * @return
     */
    private CreateVersion analyseCreateVersion(UpdateDbXml xml, String version) {
        CreateVersion cv = null;
        if (xml == null || version == null) {
            return cv;
        }

        List<CreateVersion> createVersions = xml.getCreateVersions();
        if (createVersions != null) {
            for (CreateVersion item : createVersions) {
                // 如果表相同则要支持xml中逗号分隔
                String[] createVersion = item.getVersion().trim().split(",");

                for (int i = 0; i < createVersion.length; i++) {
                    if (createVersion[i].trim().equalsIgnoreCase(version)) {
                        cv = item;
                        break;
                    }
                }
            }
        }

        return cv;
    }

    /**
     * 根据建表脚本,核实一遍应该存在的表
     * @param createVersion
     * @throws Exception
     */
    private void executeCreateVersion(CreateVersion createVersion, boolean isLogic) throws Exception {
        if (createVersion == null || createVersion.getCreateDbs() == null) {
            throw new Exception("createVersion or createDbs is null;");
        }

        for (CreateDb cd : createVersion.getCreateDbs()) {
            if (cd == null || cd.getName() == null) {
                throw new Exception("db or dbName is null when createVersion;");
            }

            // 创建数据库表sql
            List<String> sqls = cd.getSqlCreates();

            SQLiteDatabase sqlitedb = null;
            // 逻辑层数据库要做多用户升级
            if (userList != null && !userList.isEmpty()) {
                // 多用户建新表
                for (int i = 0; i < userList.size(); i++) {
                    // 获取db
                    sqlitedb = getDb(cd, userList.get(i).getPhoneNumber());
                    executeSql(sqlitedb, sqls);
                    sqlitedb.close();
                }
            }
        }
    }


    /**
     * 执行sql语句
     * @param sqlitedb
     *         SQLiteDatabase
     * @param sqls
     *         sql语句集合
     * @throws Exception
     *         异常
     * @throws throws
     *         [违例类型] [违例说明]
     * @see
     */
    private void executeSql(SQLiteDatabase sqlitedb, List<String> sqls) throws Exception {
        // 检查参数
        if (sqls == null || sqls.size() == 0) {
            return;
        }

        // 事务
        sqlitedb.beginTransaction();

        for (String sql : sqls) {
            sql = sql.replaceAll("\r\n", " ");
            sql = sql.replaceAll("\n", " ");
            if (!"".equals(sql.trim())) {
                sqlitedb.execSQL(sql);
            }
        }
        sqlitedb.setTransactionSuccessful();
        sqlitedb.endTransaction();
    }

    /**
     * 创建数据库,获取数据库对应的SQLiteDatabase
     * @param dbname
     * @return 设定文件
     * @throws throws
     *         [违例类型] [违例说明]sta
     * @see
     */
    private SQLiteDatabase getDb(String dbname, String userId) {
        String dbfilepath = null;
        SQLiteDatabase sqlitedb = null;
        File file = new File(parentFile, userId);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (dbname.equalsIgnoreCase("login")) {
            dbfilepath = file.getAbsolutePath() + "/login.db";// logic对应的数据库路径

        } else if (dbname.equalsIgnoreCase("yianju")) {
            dbfilepath = App.getInstance().getDataBasePath() + "/yianju.db";// service对应的数据库
        }

        if (dbfilepath != null) {
            File f = new File(dbfilepath);
            f.mkdirs();
            if (f.isDirectory()) {
                f.delete();
            }
            sqlitedb = SQLiteDatabase.openOrCreateDatabase(dbfilepath, null);
        }

        return sqlitedb;
    }

    /**
     * 根据xml对象获取对应要修改的db文件
     * @param db
     * @return
     */
    private SQLiteDatabase getDb(UpdateDb db, String userId) {
        return getDb(db.getDbName(), userId);
    }

    private SQLiteDatabase getDb(CreateDb db, String userId) {
        return getDb(db.getName(), userId);
    }

    /**
     * 获取APK版本号
     * @param context
     *         上下文
     * @return 版本号
     * @throws throws
     *         [违例类型] [违例说明]
     * @see
     */
    public String getVersionName(Context context) {
        String versionName = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return versionName;
    }

    /*
     * 保存下载APK版本信息
     * @param context
     * @return 保存成功返回true，否则返回false
     * @throws throws
     *         [违例类型] [违例说明]
     * @see
     */
    public boolean saveVersionInfo(Context context, String newVersion) {
        boolean ret = false;

        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(parentFile, "update.txt"), false);
            writer.write(newVersion + INFO_FILE_DIV + getVersionName(context));
            writer.flush();
            ret = true;
        } catch (IOException e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /*
     * 获取本地版本相关信息
     * @see
     */
    private String existVersion;
    private String lastBackupVersion;

    private boolean getLocalVersionInfo() {
        boolean ret = false;

        File file = new File(parentFile, "update.txt");

        if (file.exists()) {
            int byteread = 0;
            byte[] tempbytes = new byte[100];
            StringBuilder stringBuilder = new StringBuilder();
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                while ((byteread = in.read(tempbytes)) != -1) {
                    stringBuilder.append(new String(tempbytes, 0, byteread));
                }
                String[] infos = stringBuilder.toString().split(INFO_FILE_DIV);
                if (infos.length == 2) {
                    existVersion = infos[0];
                    lastBackupVersion = infos[1];
                    ret = true;
                }
            } catch (Exception e) {

            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    in = null;
                }
            }
        }

        return ret;
    }

    /*
     * 开始升级
     * @param context
     */
    public void startUpdateDb(Context context) {
        UpdateDbXml updateDbxml = readDbXml(context);
        try {
            //第一步：备份数据库  更新每个用户的数据库
            for (User user : userList) {
                String logicDbDir = parentFile.getAbsolutePath() + "/" + user.getPhoneNumber() + "/login.db";

                String logicCopy = bakFile.getAbsolutePath() + "/" + user.getPhoneNumber() + "/login.db";
                FileUtil.CopySingleFile(logicDbDir, logicCopy);
            }

            //第二步：备份总数据库表
            String yianju = App.getInstance().getDataBasePath() + "/yianju.db";

            String yianjuBack = bakFile.getAbsolutePath() + "/yianju.db";
            FileUtil.CopySingleFile(yianju, yianjuBack);

            //获取版本检查更新数据，如有有版本更新，就会在数据库存放地址生成update.txt文件，文件内存放新版和旧版版本号 ，判断是否需要升级数据库
            if (getLocalVersionInfo()) {

                //第三步：获取当前版本信息 拿到当前版本
                String thisVersion = getVersionName(context);
                //拿到上一个版本
                String lastVersion = lastBackupVersion;

                //插入数据
                UpdateStep updateStep = analyseUpdateStep(updateDbxml, lastVersion, "2.0");

                if (updateStep == null) {
                    return;
                }

                //获取更新数据库脚本集合
                List<UpdateDb> updateDbs = updateStep.getUpdateDbs();

                //解析出对应版本的建表脚本
                CreateVersion createVersion = analyseCreateVersion(updateDbxml, "2.0");

                // 第四步:执行sql_before语句，删除以及备份相关旧表
                executeDb(updateDbs, -1);


                // 第五步:检查新表，创建新表
                executeCreateVersion(createVersion, false);

                // 第六步:从备份表中恢复数据，恢复后删除备份表
                executeDb(updateDbs, 1);

                // 第五步:升级成功，删除备份数据库
                if (userList != null && !userList.isEmpty()) {
                    FileUtil.deleteFile(bakFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            //升级发生异常，恢复备份的数据库

            try {
                for (User user : userList) {
//                    FileUtil.deleteFile(parentFile.getAbsolutePath() + "/" + user.getPhoneNumber());
//                    FileUtil.deleteFile(App.getInstance().getDataBasePath() + "/yianju.db");


                    String logicDbDir = parentFile.getAbsolutePath() + "/" + user.getPhoneNumber() + "/login.db";

                    String logicCopy = bakFile.getAbsolutePath() + "/" + user.getPhoneNumber() + "/login.db";

                    FileUtil.CopySingleFile(logicCopy, logicDbDir);

                    String yianju = App.getInstance().getDataBasePath() + "/yianju.db";

                    String yianjuBack = bakFile.getAbsolutePath() + "/yianju.db";
                    FileUtil.CopySingleFile(yianjuBack, yianju);
                }
            } catch (Exception i) {
                FileUtil.deleteFile(App.getInstance().getDataBasePath());
                DaoManagerFactory.getInstance().getDataHelper(UserDao.class, User.class);
            }

        }
    }


    /**
     * 执行针对db升级的sql集合
     * @param updateDbs
     *         数据库操作脚本集合
     * @param type
     *         小于0为建表前，大于0为建表后
     * @throws Exception
     * @throws throws
     *         [违例类型] [违例说明]
     * @see
     */
    private void executeDb(List<UpdateDb> updateDbs, int type) throws Exception {
        if (updateDbs == null) {
            throw new Exception("updateDbs is null;");
        }
        for (UpdateDb db : updateDbs) {
            if (db == null || db.getDbName() == null) {
                throw new Exception("db or dbName is null;");
            }

            List<String> sqls = null;
            //更改表
            if (type < 0) {
                sqls = db.getSqlBefores();
            } else if (type > 0) {
                sqls = db.getSqlAfters();
            }

            SQLiteDatabase sqlitedb = null;

            // 逻辑层数据库要做多用户升级
            if (userList != null && !userList.isEmpty()) {
                // 多用户表升级
                for (int i = 0; i < userList.size(); i++) {
                    String phoneNumber = userList.get(i).getPhoneNumber();
                    sqlitedb = getDb(db, phoneNumber);

                    executeSql(sqlitedb, sqls);

                    sqlitedb.close();
                }
            }
        }
    }

    /**
     * 新表插入数据
     * @param xml
     * @param lastVersion
     *         上个版本
     * @param thisVersion
     *         当前版本
     * @return
     */
    private UpdateStep analyseUpdateStep(UpdateDbXml xml, String lastVersion, String thisVersion) {
        if (lastVersion == null || thisVersion == null) {
            return null;
        }

        // 更新脚本
        UpdateStep thisStep = null;
        if (xml == null) {
            return null;
        }
        List<UpdateStep> steps = xml.getUpdateSteps();
        if (steps == null || steps.size() == 0) {
            return null;
        }

        for (UpdateStep step : steps) {
            if (step.getVersionFrom() == null || step.getVersionTo() == null) {
            } else {
                // 升级来源以逗号分隔
                String[] lastVersionArray = step.getVersionFrom().split(",");

                if (lastVersionArray != null && lastVersionArray.length > 0) {
                    for (int i = 0; i < lastVersionArray.length; i++) {
                        // 有一个配到update节点即升级数据

                        boolean b = lastVersion.equalsIgnoreCase(lastVersionArray[i]);
                        boolean b1 = step.getVersionTo().equalsIgnoreCase(thisVersion);

                        if (b && b1) {
                            thisStep = step;

                            break;
                        }
                    }
                }
            }
        }

        return thisStep;
    }
}
