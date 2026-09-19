package employee;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static boolean errorShown = false;
    private static Properties props = new Properties();
    private static boolean configLoaded = false;

    private static void loadConfig() {
        if (configLoaded) return;
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            configLoaded = true;
        } catch (Exception e) {
            System.err.println("Could not load config.properties. Using default settings.");
            props.setProperty("db.url", "jdbc:mysql://localhost:3306/");
            props.setProperty("db.name", "employee_db");
            props.setProperty("db.username", "root");
            props.setProperty("db.password", "");
            configLoaded = true;
        }
    }

    public static Connection getConnection() {
        loadConfig();
        try {
            // 1. Connect to the local MySQL server
            String baseUrl = props.getProperty("db.url");
            String dbName = props.getProperty("db.name");
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");

            Connection initConn = DriverManager.getConnection(baseUrl, user, pass);
            
            // 2. Auto-create the database if it doesn't exist
            initConn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            initConn.close();

            // 3. Connect to the specific database
            Connection conn = DriverManager.getConnection(baseUrl + dbName, user, pass);
            
            // 4. Auto-create the employees table if it doesn't exist
            String createEmpTableSQL = "CREATE TABLE IF NOT EXISTS employees ("
                    + "id INT PRIMARY KEY, "
                    + "name VARCHAR(100), "
                    + "department VARCHAR(100), "
                    + "role VARCHAR(100), "
                    + "email VARCHAR(150), "
                    + "salary DOUBLE, "
                    + "photo_path VARCHAR(500))";
            conn.createStatement().executeUpdate(createEmpTableSQL);
            
            try { conn.createStatement().executeUpdate("ALTER TABLE employees ADD COLUMN role VARCHAR(100)"); } catch (Exception ignore) {}
            try { conn.createStatement().executeUpdate("ALTER TABLE employees ADD COLUMN email VARCHAR(150)"); } catch (Exception ignore) {}
            try { conn.createStatement().executeUpdate("ALTER TABLE employees ADD COLUMN photo_path VARCHAR(500)"); } catch (Exception ignore) {}
            
            // 5. Auto-create the users table
            String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(50) NOT NULL)";
            conn.createStatement().executeUpdate(createUsersTableSQL);
            
            // 6. Insert default admin if table is empty
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                pstmt.setString(1, "admin");
                // For simplicity, we are storing plain text or simple hash here. 
                // A real app should use BCrypt. Storing plain for demonstration per requirements.
                pstmt.setString(2, "admin123"); 
                pstmt.setString(3, "Admin");
                pstmt.executeUpdate();
            }
            
            return conn;
        } catch (Exception e) {
            if (!errorShown) {
                JOptionPane.showMessageDialog(null, 
                    "Cannot connect to database!\nCheck config.properties and ensure MySQL is running.", 
                    "Database Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
                errorShown = true;
            }
            e.printStackTrace();
            return null;
        }
    }
}

