
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class connect {
    private final String serverName = "localhost";
    private final String portNumber = "1433";
    private final String dbName = "User";
    private final String userID = "sa";
    private final String password = "12345";
    private final String instance = ""; // Nếu có instance thì điền vào đây

    public Connection getConnection() {
        Connection conn = null;
        try {
            // Chuỗi kết nối với encrypt=true và trustServerCertificate=true
            String url = "jdbc:sqlserver://" + serverName + ":" + portNumber;
            if (instance != null && !instance.trim().isEmpty()) {
                url += "\\" + instance;
            }
            url += ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";

            // Load JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Kết nối tới SQL Server
            conn = DriverManager.getConnection(url, userID, password);
            System.out.println("Kết nối thành công!");

        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy driver JDBC.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu:");
            e.printStackTrace();
        }
        return conn;
    }
}