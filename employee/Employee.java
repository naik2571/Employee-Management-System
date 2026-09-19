package employee;

public class Employee {
    private int id;
    private String name;
    private String department;
    private String role;
    private String email;
    private double salary;
    private String photoPath;

    public Employee(int id, String name, String department, String role, String email, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.role = role;
        this.email = email;
        this.salary = salary;
        this.photoPath = null;
    }

    public Employee(int id, String name, String department, String role, String email, double salary, String photoPath) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.role = role;
        this.email = email;
        this.salary = salary;
        this.photoPath = photoPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Dept: " + department
                + " | Role: " + role + " | Email: " + email + " | Salary: $" + salary;
    }
}
