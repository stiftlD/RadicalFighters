package data;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteHelper {
    private static Connection c = null;

    public static Connection getConn() throws SQLException {
        //System.out.println("Connection opened");
        String rootDir = System.getProperty("user.dir"); //TODO config file for this
        String kanjiDBURL = "jdbc:sqlite:" + Path.of(rootDir + "/database/kanji.db").toString();
        //Class.forName("org.sqlite.JDBC");
        c = DriverManager.getConnection(kanjiDBURL);

        //System.out.println("Providing connection");
        return c;
    }
}