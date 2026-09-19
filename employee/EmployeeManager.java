package employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManager {

    public EmployeeManager() {
        DatabaseConnection.getConnection();
    }

    private Employee mapRow(ResultSet rs) throws java.sql.SQLException {
        return new Employee(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("department"),
            rs.getString("role"),
            rs.getString("email"),
            rs.getDouble("salary"),
            rs.getString("photo_path")
        );
    }

    public void addEmployee(Employee employee) throws java.sql.SQLException {
        String sql = "INSERT INTO employees (id, name, department, role, email, salary, photo_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new java.sql.SQLException("Database connection failed.");
            pstmt.setInt(1, employee.getId());
            pstmt.setString(2, employee.getName());
            pstmt.setString(3, employee.getDepartment());
            pstmt.setString(4, employee.getRole());
            pstmt.setString(5, employee.getEmail());
            pstmt.setDouble(6, employee.getSalary());
            pstmt.setString(7, employee.getPhotoPath());
            pstmt.executeUpdate();
        }
    }

    public List<Employee> getEmployees(String searchQuery, String departmentFilter, String roleFilter, int limit, int offset) throws java.sql.SQLException {
        List<Employee> employees = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM employees WHERE 1=1");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) sql.append(" AND name LIKE ?");
        if (departmentFilter != null && !departmentFilter.equals("All")) sql.append(" AND department = ?");
        if (roleFilter != null && !roleFilter.equals("All")) sql.append(" AND role = ?");
        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            if (conn == null) throw new java.sql.SQLException("Database connection failed.");
            int i = 1;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) pstmt.setString(i++, "%" + searchQuery.trim() + "%");
            if (departmentFilter != null && !departmentFilter.equals("All")) pstmt.setString(i++, departmentFilter);
            if (roleFilter != null && !roleFilter.equals("All")) pstmt.setString(i++, roleFilter);
            pstmt.setInt(i++, limit);
            pstmt.setInt(i++, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) employees.add(mapRow(rs));
            }
        }
        return employees;
    }

    public Employee getEmployeeById(int id) throws java.sql.SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new java.sql.SQLException("Database connection failed.");
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean updateEmployee(int id, String name, String department, String role, String email, double salary, String photoPath) throws java.sql.SQLException {
        String sql = "UPDATE employees SET name=?, department=?, role=?, email=?, salary=?, photo_path=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new java.sql.SQLException("Database connection failed.");
            pstmt.setString(1, name);
            pstmt.setString(2, department);
            pstmt.setString(3, role);
            pstmt.setString(4, email);
            pstmt.setDouble(5, salary);
            pstmt.setString(6, photoPath);
            pstmt.setInt(7, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteEmployee(int id) throws java.sql.SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new java.sql.SQLException("Database connection failed.");
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}
