package employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.regex.Pattern;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class EmployeeGUI extends JFrame {
    private EmployeeManager manager;
    private User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId, txtName, txtEmail, txtSalary;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterDept, cbFilterRole, cbDepartment, cbRole;
    private JLabel lblHeadcount, lblPayroll;
    private JLabel lblPhoto;
    private String selectedPhotoPath = null;

    private JButton btnAdd, btnUpdate, btnDelete;
    private boolean isFormDirty = false;
    private boolean isDarkTheme = false;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 50;

    public EmployeeGUI(EmployeeManager manager, User user) {
        this.manager = manager;
        this.currentUser = user;
        setTitle("Employee Management System - " + user.getUsername() + " (" + user.getRole() + ")");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            ImageIcon icon = new ImageIcon("employee/logo.jpg");
            if (icon.getIconWidth() > 0) setIconImage(icon.getImage());
        } catch (Exception ignored) {}

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (checkUnsavedChanges()) System.exit(0);
            }
        });

        initUI();
        refreshTableAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ── Top: Dashboard + Filters ──────────────────────────────────────────
        JPanel topContainer = new JPanel(new BorderLayout());

        JPanel dashboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 8));
        dashboardPanel.setBorder(BorderFactory.createTitledBorder("Dashboard"));
        lblHeadcount = new JLabel("Total Headcount: 0");
        lblPayroll = new JLabel("Total Payroll: $0.00");
        dashboardPanel.add(lblHeadcount);
        dashboardPanel.add(lblPayroll);
        topContainer.add(dashboardPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.add(new JLabel("Search Name:"));
        txtSearch = new JTextField(14);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Dept:"));
        cbFilterDept = new JComboBox<>(new String[]{"All", "IT", "HR", "Finance", "Sales", "Engineering"});
        filterPanel.add(cbFilterDept);

        filterPanel.add(new JLabel("Role:"));
        cbFilterRole = new JComboBox<>(new String[]{"All", "Manager", "Developer", "Analyst", "Clerk"});
        filterPanel.add(cbFilterRole);

        JButton btnSearch = new JButton("Search");
        JButton btnClearSearch = new JButton("Clear Filters");
        JButton btnReports = new JButton("📊 Reports");
        JButton btnToggleTheme = new JButton("🌙 Dark Mode");
        filterPanel.add(btnSearch);
        filterPanel.add(btnClearSearch);
        filterPanel.add(btnReports);
        filterPanel.add(btnToggleTheme);
        topContainer.add(filterPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        btnSearch.addActionListener(e -> { currentPage = 0; refreshTableAsync(); });
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText(""); cbFilterDept.setSelectedIndex(0); cbFilterRole.setSelectedIndex(0);
            currentPage = 0; refreshTableAsync();
        });
        btnReports.addActionListener(e -> new ReportsGUI(manager).setVisible(true));
        btnToggleTheme.addActionListener(e -> toggleTheme(btnToggleTheme));

        // ── Centre: Table + Pagination ────────────────────────────────────────
        String[] columns = {"ID", "Name", "Department", "Role", "Email", "Salary"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                if (checkUnsavedChanges()) populateFormFromTable(table.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnPrev = new JButton("<< Prev");
        JButton btnNext = new JButton("Next >>");
        paginationPanel.add(btnPrev);
        paginationPanel.add(btnNext);
        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; refreshTableAsync(); } });
        btnNext.addActionListener(e -> { currentPage++; refreshTableAsync(); });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // ── Right: Form + Photo + Import/Export ───────────────────────────────
        KeyAdapter dirtyAdapter = new KeyAdapter() {
            public void keyTyped(KeyEvent e) { isFormDirty = true; }
        };

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        formPanel.add(new JLabel("ID:")); txtId = new JTextField(); txtId.addKeyListener(dirtyAdapter); formPanel.add(txtId);
        formPanel.add(new JLabel("Name:")); txtName = new JTextField(); txtName.addKeyListener(dirtyAdapter); formPanel.add(txtName);
        formPanel.add(new JLabel("Department:")); 
        cbDepartment = new JComboBox<>(new String[]{"IT", "HR", "Finance", "Sales", "Engineering"}); 
        cbDepartment.addItemListener(e -> isFormDirty = true); 
        formPanel.add(cbDepartment);
        
        formPanel.add(new JLabel("Role:")); 
        cbRole = new JComboBox<>(new String[]{"Manager", "Developer", "Analyst", "Clerk"}); 
        cbRole.addItemListener(e -> isFormDirty = true); 
        formPanel.add(cbRole);
        formPanel.add(new JLabel("Email:")); txtEmail = new JTextField(); txtEmail.addKeyListener(dirtyAdapter); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Salary:")); txtSalary = new JTextField(); txtSalary.addKeyListener(dirtyAdapter); formPanel.add(txtSalary);

        // Photo section
        JPanel photoPanel = new JPanel(new BorderLayout(5, 5));
        photoPanel.setBorder(BorderFactory.createTitledBorder("Profile Photo"));
        lblPhoto = new JLabel("No Photo", SwingConstants.CENTER);
        lblPhoto.setPreferredSize(new Dimension(130, 130));
        lblPhoto.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JButton btnBrowsePhoto = new JButton("Browse Photo");
        JButton btnRemovePhoto = new JButton("Remove");
        JPanel photoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        photoBtns.add(btnBrowsePhoto); photoBtns.add(btnRemovePhoto);
        photoPanel.add(lblPhoto, BorderLayout.CENTER);
        photoPanel.add(photoBtns, BorderLayout.SOUTH);

        btnBrowsePhoto.addActionListener(e -> browsePhoto());
        btnRemovePhoto.addActionListener(e -> { selectedPhotoPath = null; lblPhoto.setIcon(null); lblPhoto.setText("No Photo"); isFormDirty = true; });

        JPanel importExportPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        importExportPanel.setBorder(BorderFactory.createTitledBorder("Import / Export"));
        JButton btnImportCsv = new JButton("Import CSV");
        JButton btnExportCsv = new JButton("Export CSV");
        JButton btnExportPdf = new JButton("Export PDF");
        importExportPanel.add(btnImportCsv);
        importExportPanel.add(btnExportCsv);
        importExportPanel.add(btnExportPdf);
        btnImportCsv.addActionListener(e -> importCSV());
        btnExportCsv.addActionListener(e -> exportCSV());
        btnExportPdf.addActionListener(e -> exportToPDF());

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(formPanel, BorderLayout.NORTH);
        rightPanel.add(photoPanel, BorderLayout.CENTER);
        rightPanel.add(importExportPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // ── Bottom: Action Buttons ────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnAdd = new JButton("➕ Add");
        btnUpdate = new JButton("✏ Update");
        btnDelete = new JButton("🗑 Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnLogout = new JButton("🚪 Logout");

        btnAdd.addActionListener(e -> addEmployeeAsync());
        btnUpdate.addActionListener(e -> updateEmployeeAsync());
        btnDelete.addActionListener(e -> deleteEmployeeAsync());
        btnClear.addActionListener(e -> clearForm());
        btnLogout.addActionListener(e -> logout());

        boolean isAdmin = "Admin".equalsIgnoreCase(currentUser.getRole());
        btnAdd.setEnabled(isAdmin); btnUpdate.setEnabled(isAdmin); btnDelete.setEnabled(isAdmin);

        buttonPanel.add(btnAdd); buttonPanel.add(btnUpdate); buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear); buttonPanel.add(btnLogout);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // ── Theme Toggle ─────────────────────────────────────────────────────────
    private void toggleTheme(JButton btn) {
        isDarkTheme = !isDarkTheme;
        try {
            if (isDarkTheme) { FlatDarkLaf.setup(); btn.setText("☀ Light Mode"); }
            else              { FlatLightLaf.setup(); btn.setText("🌙 Dark Mode"); }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Theme switch failed: " + ex.getMessage());
        }
    }

    // ── Photo Browse ──────────────────────────────────────────────────────────
    private void browsePhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPhotoPath = fc.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedPhotoPath)
                .getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH));
            lblPhoto.setIcon(icon);
            lblPhoto.setText("");
            isFormDirty = true;
        }
    }

    // ── Unsaved Changes Guard ─────────────────────────────────────────────────
    private boolean checkUnsavedChanges() {
        if (!isFormDirty) return true;
        int res = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Discard them?", "Unsaved Changes", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) { isFormDirty = false; return true; }
        return false;
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    private void logout() {
        if (checkUnsavedChanges()) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginGUI(manager).setVisible(true));
        }
    }

    // ── Refresh Table (Async) ─────────────────────────────────────────────────
    private void refreshTableAsync() {
        String search = txtSearch.getText();
        String dept = (String) cbFilterDept.getSelectedItem();
        String role = (String) cbFilterRole.getSelectedItem();
        int offset = currentPage * PAGE_SIZE;

        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() throws Exception {
                return manager.getEmployees(search, dept, role, PAGE_SIZE, offset);
            }
            @Override protected void done() {
                try {
                    List<Employee> emps = get();
                    tableModel.setRowCount(0);
                    double payroll = 0;
                    for (Employee emp : emps) {
                        tableModel.addRow(new Object[]{emp.getId(), emp.getName(), emp.getDepartment(), emp.getRole(), emp.getEmail(), emp.getSalary()});
                        payroll += emp.getSalary();
                    }
                    lblHeadcount.setText("Total Headcount: " + emps.size() + (emps.size() == PAGE_SIZE ? "+" : ""));
                    lblPayroll.setText(String.format("Total Payroll: $%.2f", payroll));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeGUI.this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Populate Form ─────────────────────────────────────────────────────────
    private void populateFormFromTable(int viewRow) {
        int row = table.convertRowIndexToModel(viewRow);
        txtId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        cbDepartment.setSelectedItem(tableModel.getValueAt(row, 2) != null ? tableModel.getValueAt(row, 2).toString() : "IT");
        cbRole.setSelectedItem(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "Manager");
        txtEmail.setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
        txtSalary.setText(tableModel.getValueAt(row, 5).toString());
        txtId.setEditable(false);
        isFormDirty = false;

        // Load photo
        try {
            int id = Integer.parseInt(txtId.getText());
            Employee emp = manager.getEmployeeById(id);
            if (emp != null && emp.getPhotoPath() != null && !emp.getPhotoPath().isEmpty()) {
                selectedPhotoPath = emp.getPhotoPath();
                ImageIcon icon = new ImageIcon(new ImageIcon(selectedPhotoPath)
                    .getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH));
                lblPhoto.setIcon(icon); lblPhoto.setText("");
            } else {
                selectedPhotoPath = null; lblPhoto.setIcon(null); lblPhoto.setText("No Photo");
            }
        } catch (Exception ignored) {}
    }

    // ── Clear Form ────────────────────────────────────────────────────────────
    private void clearForm() {
        if (!checkUnsavedChanges()) return;
        txtId.setText(""); txtName.setText(""); cbDepartment.setSelectedIndex(0);
        cbRole.setSelectedIndex(0); txtEmail.setText(""); txtSalary.setText("");
        txtId.setEditable(true); table.clearSelection();
        selectedPhotoPath = null; lblPhoto.setIcon(null); lblPhoto.setText("No Photo");
        isFormDirty = false;
    }

    // ── Validation ────────────────────────────────────────────────────────────
    private boolean validateInput() {
        if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtSalary.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID, Name, and Salary are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            double s = Double.parseDouble(txtSalary.getText());
            if (s < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary must be a positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !Pattern.matches("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // ── Add Employee (Async) ──────────────────────────────────────────────────
    private void addEmployeeAsync() {
        if (!validateInput()) return;
        final int id = Integer.parseInt(txtId.getText());
        final String name = txtName.getText(), dept = (String) cbDepartment.getSelectedItem(),
                     role = (String) cbRole.getSelectedItem(), email = txtEmail.getText();
        final double salary = Double.parseDouble(txtSalary.getText());
        final String photo = selectedPhotoPath;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (manager.getEmployeeById(id) != null) throw new Exception("Employee ID " + id + " already exists!");
                manager.addEmployee(new Employee(id, name, dept, role, email, salary, photo));
                // Send welcome email asynchronously if email provided
                if (email != null && !email.isEmpty()) {
                    EmailService.sendWelcomeEmail(email, name);
                }
                return null;
            }
            @Override protected void done() {
                try { get(); isFormDirty = false; refreshTableAsync(); clearForm(); JOptionPane.showMessageDialog(EmployeeGUI.this, "Employee added! Welcome email sent (if configured)."); }
                catch (Exception ex) { JOptionPane.showMessageDialog(EmployeeGUI.this, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }

    // ── Update Employee (Async) ───────────────────────────────────────────────
    private void updateEmployeeAsync() {
        if (!validateInput()) return;
        final int id = Integer.parseInt(txtId.getText());
        final String name = txtName.getText(), dept = (String) cbDepartment.getSelectedItem(),
                     role = (String) cbRole.getSelectedItem(), email = txtEmail.getText();
        final double salary = Double.parseDouble(txtSalary.getText());
        final String photo = selectedPhotoPath;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return manager.updateEmployee(id, name, dept, role, email, salary, photo);
            }
            @Override protected void done() {
                try {
                    if (get()) { isFormDirty = false; refreshTableAsync(); clearForm(); JOptionPane.showMessageDialog(EmployeeGUI.this, "Employee updated!"); }
                    else JOptionPane.showMessageDialog(EmployeeGUI.this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) { JOptionPane.showMessageDialog(EmployeeGUI.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }

    // ── Delete Employee (Async) ───────────────────────────────────────────────
    private void deleteEmployeeAsync() {
        if (table.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "Select an employee first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            final int id = Integer.parseInt(txtId.getText());
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { manager.deleteEmployee(id); return null; }
                @Override protected void done() {
                    try { get(); isFormDirty = false; refreshTableAsync(); clearForm(); JOptionPane.showMessageDialog(EmployeeGUI.this, "Employee deleted."); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(EmployeeGUI.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        }
    }

    // ── Export PDF ────────────────────────────────────────────────────────────
    private void exportToPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("EmployeeData.pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<Employee> emps = manager.getEmployees("", "All", "All", Integer.MAX_VALUE, 0);
                Document doc = new Document();
                PdfWriter.getInstance(doc, new FileOutputStream(fc.getSelectedFile()));
                doc.open();
                doc.add(new Paragraph("Employee Management System Report\n\n"));
                PdfPTable t = new PdfPTable(6);
                for (String h : new String[]{"ID", "Name", "Dept", "Role", "Email", "Salary"}) t.addCell(h);
                for (Employee e : emps) {
                    t.addCell(String.valueOf(e.getId())); t.addCell(e.getName());
                    t.addCell(e.getDepartment()); t.addCell(e.getRole() != null ? e.getRole() : "");
                    t.addCell(e.getEmail() != null ? e.getEmail() : ""); t.addCell(String.valueOf(e.getSalary()));
                }
                doc.add(t); doc.close();
                JOptionPane.showMessageDialog(this, "PDF exported to: " + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Export CSV ────────────────────────────────────────────────────────────
    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("EmployeeData.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fc.getSelectedFile())) {
                pw.println("ID,Name,Department,Role,Email,Salary");
                List<Employee> emps = manager.getEmployees("", "All", "All", Integer.MAX_VALUE, 0);
                for (Employee e : emps) pw.printf("%d,%s,%s,%s,%s,%.2f\n", e.getId(), e.getName(), e.getDepartment(), e.getRole(), e.getEmail(), e.getSalary());
                JOptionPane.showMessageDialog(this, "CSV exported!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Import CSV ────────────────────────────────────────────────────────────
    private void importCSV() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                        br.readLine(); // skip header
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] p = line.split(",");
                            if (p.length >= 6) {
                                try {
                                    int id = Integer.parseInt(p[0].trim());
                                    if (manager.getEmployeeById(id) != null) continue; // skip duplicate
                                    manager.addEmployee(new Employee(id, p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim(), Double.parseDouble(p[5].trim())));
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                    return null;
                }
                @Override protected void done() {
                    try { get(); refreshTableAsync(); JOptionPane.showMessageDialog(EmployeeGUI.this, "Import complete (duplicates skipped)."); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(EmployeeGUI.this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        }
    }
}
