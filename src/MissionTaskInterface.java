import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;

public class MissionTaskInterface extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable tasksTable;
    private JTable missionsTable;
    private String username; // Tên người dùng đã đăng nhập
    private boolean isAdmin; // Kiểm tra xem người dùng có phải là admin không

    public MissionTaskInterface(String username, boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;
        setTitle("Giao diện Nhiệm vụ và Công việc - Người dùng: " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // ---------------- Panel cho bảng Công việc (Tasks) ----------------
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksTable = new JTable();
        JScrollPane tasksScrollPane = new JScrollPane(tasksTable);
        tasksPanel.add(tasksScrollPane, BorderLayout.CENTER);

        // Nút Refresh Tasks
        JButton btnRefreshTasks = new JButton("Refresh Tasks");
        btnRefreshTasks.addActionListener(e -> loadTasksData());

        // Nút Tạo Công Việc
        JButton btnCreateTask = new JButton("Tạo Công Việc");
        btnCreateTask.addActionListener(e -> createTaskDialog());

        // Nút Xóa Công Việc
        JButton btnDeleteTask = new JButton("Xóa Công Việc");
        btnDeleteTask.addActionListener(e -> deleteTask());

        // Panel chứa nút cho Tasks
        JPanel tasksBottomPanel = new JPanel();
        tasksBottomPanel.add(btnRefreshTasks);
        tasksBottomPanel.add(btnCreateTask);
        tasksBottomPanel.add(btnDeleteTask);
        tasksPanel.add(tasksBottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Công việc", tasksPanel);

        // ---------------- Panel cho bảng Nhiệm vụ (Missions) ----------------
        JPanel missionsPanel = new JPanel(new BorderLayout());
        missionsTable = new JTable();
        JScrollPane missionsScrollPane = new JScrollPane(missionsTable);
        missionsPanel.add(missionsScrollPane, BorderLayout.CENTER);

        // Nút Refresh Missions
        JButton btnRefreshMissions = new JButton("Refresh Missions");
        btnRefreshMissions.addActionListener(e -> loadMissionsData());

        // Nút Tạo Nhiệm Vụ (chỉ admin có thể tạo)
        JButton btnCreateMission = new JButton("Tạo Nhiệm Vụ");
        btnCreateMission.setEnabled(isAdmin);
        btnCreateMission.addActionListener(e -> createMissionDialog());

        // Nút Xóa Nhiệm Vụ (chỉ admin có thể xóa)
        JButton btnDeleteMission = new JButton("Xóa Nhiệm Vụ");
        btnDeleteMission.setEnabled(isAdmin);
        btnDeleteMission.addActionListener(e -> deleteMission());

        // Panel chứa nút cho Missions
        JPanel missionsBottomPanel = new JPanel();
        missionsBottomPanel.add(btnRefreshMissions);
        missionsBottomPanel.add(btnCreateMission);
        missionsBottomPanel.add(btnDeleteMission);
        missionsPanel.add(missionsBottomPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Nhiệm vụ", missionsPanel);

        add(tabbedPane);

        // Tải dữ liệu ban đầu
        loadTasksData();
        loadMissionsData();

        setVisible(true);
    }

    /**
     * Tải danh sách công việc được tạo bởi người dùng hiện tại.
     */
    private void loadTasksData() {
        connect dbConnect = new connect();
        String sql = "SELECT task_id, task_name, due_date FROM Tasks WHERE created_by = ?";
        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Tên công việc", "Ngày hết hạn"}, 0
            );
            boolean hasData = false;
            while (rs.next()) {
                int id = rs.getInt("task_id");
                String name = rs.getString("task_name");
                Date dueDate = rs.getDate("due_date");
                model.addRow(new Object[]{id, name, dueDate});
                hasData = true;
            }
            tasksTable.setModel(model);

            if (!hasData) {
                JOptionPane.showMessageDialog(null,
                    "Không có công việc nào được tìm thấy.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Lỗi tải dữ liệu công việc: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Tải danh sách nhiệm vụ (Missions).
     */
    private void loadMissionsData() {
        connect dbConnect = new connect();
        // Make sure your table is named "Missions" with columns: mission_id, mission_name, description
        String sql = "SELECT mission_id, mission_name, description FROM Missions";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Tên nhiệm vụ", "Mô tả"}, 0
            );
            boolean hasData = false;
            while (rs.next()) {
                int id = rs.getInt("mission_id");
                String name = rs.getString("mission_name");
                String description = rs.getString("description");
                model.addRow(new Object[]{id, name, description});
                hasData = true;
            }
            missionsTable.setModel(model);

            if (!hasData) {
                JOptionPane.showMessageDialog(null,
                    "Không có nhiệm vụ nào được tìm thấy.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Lỗi tải dữ liệu nhiệm vụ: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Hiển thị dialog nhập thông tin để tạo công việc mới.
     */
    private void createTaskDialog() {
        JTextField taskNameField = new JTextField(20);
        JTextField dueDateField = new JTextField(10); // Định dạng yyyy-MM-dd

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Tên công việc:"));
        panel.add(taskNameField);
        panel.add(new JLabel("Ngày hết hạn (yyyy-MM-dd):"));
        panel.add(dueDateField);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Tạo Công Việc Mới",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            String taskName = taskNameField.getText().trim();
            String dueDateStr = dueDateField.getText().trim();
            if (taskName.isEmpty() || dueDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Các trường không được để trống!",
                    "Error", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            try {
                java.sql.Date dueDate = java.sql.Date.valueOf(dueDateStr);
                if (createTask(username, taskName, dueDate)) {
                    JOptionPane.showMessageDialog(null,
                        "Tạo công việc thành công!"
                    );
                    loadTasksData();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Tạo công việc thất bại!",
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null,
                    "Định dạng ngày không hợp lệ. Vui lòng nhập theo định dạng yyyy-MM-dd.",
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Hiển thị dialog nhập thông tin để tạo nhiệm vụ mới.
     */
    private void createMissionDialog() {
        JTextField missionNameField = new JTextField(20);
        JTextField descriptionField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Tên nhiệm vụ:"));
        panel.add(missionNameField);
        panel.add(new JLabel("Mô tả:"));
        panel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Tạo Nhiệm Vụ Mới",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            String missionName = missionNameField.getText().trim();
            String description = descriptionField.getText().trim();
            if (missionName.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "Các trường không được để trống!",
                    "Error", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            if (createMission(missionName, description)) {
                JOptionPane.showMessageDialog(null,
                    "Tạo nhiệm vụ thành công!"
                );
                loadMissionsData();
            } else {
                JOptionPane.showMessageDialog(null,
                    "Tạo nhiệm vụ thất bại!",
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Chèn một công việc mới vào bảng Tasks, lưu thông tin người tạo.
     */
    private boolean createTask(String username, String taskName, java.sql.Date dueDate) {
        connect dbConnect = new connect();
        String sql = "INSERT INTO Tasks (task_name, due_date, created_by) VALUES (?, ?, ?)";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, taskName);
            stmt.setDate(2, dueDate);
            stmt.setString(3, username);

            int rowsInserted = stmt.executeUpdate();
            return (rowsInserted > 0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Lỗi khi tạo công việc: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Chèn một nhiệm vụ mới vào bảng Missions.
     */
    private boolean createMission(String missionName, String description) {
        connect dbConnect = new connect();
        String sql = "INSERT INTO Missions (mission_name, description) VALUES (?, ?)";

        try (Connection conn = dbConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, missionName);
            stmt.setString(2, description);

            int rowsInserted = stmt.executeUpdate();
            return (rowsInserted > 0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Lỗi khi tạo nhiệm vụ: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Xóa công việc được chọn từ bảng Tasks.
     */
    private void deleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null,
                "Vui lòng chọn một công việc để xóa.",
                "Error", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int taskId = (int) tasksTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
            null, "Bạn có chắc chắn muốn xóa công việc này?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            connect dbConnect = new connect();
            String sql = "DELETE FROM Tasks WHERE task_id = ?";
            try (Connection conn = dbConnect.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, taskId);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null,
                        "Xóa công việc thành công!"
                    );
                    loadTasksData();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Xóa công việc thất bại!",
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                    "Lỗi khi xóa công việc: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Xóa nhiệm vụ được chọn từ bảng Missions.
     */
    private void deleteMission() {
        int selectedRow = missionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null,
                "Vui lòng chọn một nhiệm vụ để xóa.",
                "Error", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int missionId = (int) missionsTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
            null, "Bạn có chắc chắn muốn xóa nhiệm vụ này?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            connect dbConnect = new connect();
            String sql = "DELETE FROM Missions WHERE mission_id = ?";
            try (Connection conn = dbConnect.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, missionId);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null,
                        "Xóa nhiệm vụ thành công!"
                    );
                    loadMissionsData();
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Xóa nhiệm vụ thất bại!",
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                    "Lỗi khi xóa nhiệm vụ: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
