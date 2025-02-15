import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connect {
    private final String serverName = "localhost";
    private final String portNumber = "1433";
    private final String dbName = "User";
    private final String userID = "sa";
    private final String password = "12345";
    private final String instance = ""; // Nếu có instance thì điền tên instance vào đây

    public Connection getConnection() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlserver://" + serverName + ":" + portNumber;
            if (instance != null && !instance.trim().isEmpty()) {
                url += "\\" + instance;
            }
            url += ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";

            // Load JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Kết nối tới SQL Server
            conn = DriverManager.getConnection(url, userID, password);
            System.out.println("Connection successful!");

        } catch (ClassNotFoundException e) {
            System.err.println("Error: JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection error:");
            e.printStackTrace();
        }
        return conn;
    }
}
