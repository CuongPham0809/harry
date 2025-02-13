import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginApp extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginApp() {
        setTitle("Login System");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("username:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("password:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("Login");
        add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());

                if (authenticate(username, password)) {
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    createTaskAndMissionTable();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    private boolean authenticate(String username, String password) {
        connect dbConnect = new connect();
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void createTaskAndMissionTable() {
        connect dbConnect = new connect();
        try (Connection conn = dbConnect.getConnection();
             Statement stmt = conn.createStatement()) {
            String createTaskTableSQL = "CREATE TABLE Tasks ("
                    + "task_id INT PRIMARY KEY, "
                    + "task_name VARCHAR(255) NOT NULL, "
                    + "due_date DATE NOT NULL)";
            stmt.executeUpdate(createTaskTableSQL);

            String createMissionTableSQL = "CREATE TABLE Missions ("
                    + "mission_id INT PRIMARY KEY, "
                    + "mission_name VARCHAR(255) NOT NULL, "
                    + "description TEXT)";
            stmt.executeUpdate(createMissionTableSQL);

            JOptionPane.showMessageDialog(null, "Task and Mission tables created successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error creating tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginApp::new);
    }
}
