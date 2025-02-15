import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginApp extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCreateAccount;

    public LoginApp() {
        setTitle("Login System");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 5, 5));

        // Nhãn và ô nhập Username
        add(new JLabel("Username:"));
        txtUsername = new JTextField();
        add(txtUsername);

        // Nhãn và ô nhập Password
        add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        // Nút Login
        btnLogin = new JButton("Login");
        add(btnLogin);

        // Nút Create Account
        btnCreateAccount = new JButton("Create Account");
        add(btnCreateAccount);

        // Xử lý đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                        "Username and password cannot be empty.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Kiểm tra đăng nhập
                int userRole = authenticate(username, password);
                if (userRole != -1) {
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    
                    // Nếu userRole = 1 => Admin, userRole = 0 => Người dùng thường
                    boolean isAdmin = (userRole == 1);

                    // Mở giao diện nhiệm vụ
                    new MissionTaskInterface(username, isAdmin);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Invalid username or password.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // Xử lý đăng ký tài khoản mới
        btnCreateAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                        "Username and password cannot be empty.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Kiểm tra xem tài khoản đã tồn tại chưa
                if (isUsernameExists(username)) {
                    JOptionPane.showMessageDialog(null, 
                        "Username already exists. Please choose a different username.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Đăng ký tài khoản mới
                if (registerNewAccount(username, password, 0)) {
                    JOptionPane.showMessageDialog(null, "Account created successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Account creation failed. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        setVisible(true);
    }

    /**
     * Xác thực đăng nhập từ bảng Users.
     * Trả về 1 nếu là Admin, 0 nếu là User, -1 nếu thất bại.
     */
    private int authenticate(String username, String password) {
        connect dbConnect = new connect();
        String sql = "SELECT Password, CheckAccount FROM Users WHERE Username = ?";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                int checkAccount = rs.getInt("CheckAccount"); // 1 = Admin, 0 = User

                if (password.equals(storedPassword)) { // Nên sử dụng BCrypt trong thực tế
                    return checkAccount; // Trả về quyền của user
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection error: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
        return -1; // Trả về -1 nếu đăng nhập thất bại
    }

    /**
     * Kiểm tra xem tài khoản đã tồn tại hay chưa.
     */
    private boolean isUsernameExists(String username) {
        connect dbConnect = new connect();
        String sql = "SELECT * FROM Users WHERE Username = ?";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Nếu tìm thấy dòng => tài khoản đã tồn tại
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection error: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
        return false;
    }

    /**
     * Đăng ký tài khoản mới vào bảng Users.
     */
    private boolean registerNewAccount(String username, String password, int checkAccount) {
        connect dbConnect = new connect();
        String sql = "INSERT INTO Users (Username, Password, CheckAccount) VALUES (?, ?, ?)";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Nên mã hóa BCrypt
            stmt.setInt(3, checkAccount); // 0 = User, 1 = Admin

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database error: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
        return false;
    }

    
}
