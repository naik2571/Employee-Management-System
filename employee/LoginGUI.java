package employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginGUI extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private EmployeeManager manager;

    public LoginGUI(EmployeeManager manager) {
        this.manager = manager;
        
        setTitle("System Login");
        setSize(550, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);
        
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        // Setup Left Panel (Logo)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(10, 25, 49)); // Dark Blue
        leftPanel.setPreferredSize(new Dimension(220, 320));
        
        try {
            ImageIcon icon = new ImageIcon("employee/logo.jpg");
            if (icon.getIconWidth() > 0) {
                setIconImage(icon.getImage());
                Image scaledImage = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
                lblLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lblLogo.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        JOptionPane.showMessageDialog(LoginGUI.this, 
                            "Employee Management System v2.0\nDeveloped with AI", 
                            "About", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                leftPanel.add(lblLogo);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Setup Right Panel (Login Form)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(45, 50, 56)); // Dark Gray
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel lblTitle = new JLabel("System Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(220, 220, 220));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(45, 50, 56));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblUser = new JLabel("Username:");
        lblUser.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblUser, gbc);
        
        txtUsername = new JTextField(14);
        txtUsername.setBackground(new Color(60, 65, 70));
        txtUsername.setForeground(Color.WHITE);
        txtUsername.setCaretColor(Color.WHITE);
        txtUsername.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180)));
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtUsername, gbc);
        
        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(new Color(200, 200, 200));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblPass, gbc);
        
        txtPassword = new JPasswordField(14);
        txtPassword.setBackground(new Color(60, 65, 70));
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180)));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtPassword, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(45, 50, 56));
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(80, 85, 90));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(0, 120, 215)); // Blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnLogin);
        
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(buttonPanel, gbc);

        rightPanel.add(lblTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(formPanel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Action Listeners
        btnLogin.addActionListener(e -> authenticate());
        btnCancel.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);
    }

    private void authenticate() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            
            if (conn == null) return;
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                    // Close login window
                    this.dispose();
                    
                    // Launch main GUI
                    SwingUtilities.invokeLater(() -> {
                        EmployeeGUI gui = new EmployeeGUI(manager, user);
                        gui.setVisible(true);
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error during login: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Add shutdown hook to clean up unnecessary compiled files when the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Delete the bin directory if it exists
                java.nio.file.Path binPath = java.nio.file.Paths.get("bin");
                if (java.nio.file.Files.exists(binPath)) {
                    java.nio.file.Files.walk(binPath)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
                }
                
                // Delete any .class files in the employee directory
                java.nio.file.Path empPath = java.nio.file.Paths.get("employee");
                if (java.nio.file.Files.exists(empPath)) {
                    java.nio.file.Files.walk(empPath)
                        .filter(p -> p.toString().endsWith(".class"))
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
                }
            } catch (Exception ex) {
                System.err.println("Failed to clean up files: " + ex.getMessage());
            }
        }));

        // Set FlatLaf Look and Feel for a modern UI
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the manager which will also auto-create the database and tables
        EmployeeManager manager = new EmployeeManager();
        
        SwingUtilities.invokeLater(() -> {
            LoginGUI login = new LoginGUI(manager);
            login.setVisible(true);
        });
    }
}
