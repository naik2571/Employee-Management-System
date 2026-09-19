# Employee Management System (EMS)

A comprehensive, desktop-based Employee Management System built with Java (Swing) and MySQL. This application provides a modern user interface using FlatLaf and includes advanced features such as auto-configuring databases, PDF reporting, CSV data import/export, email notifications, and graphical analytics.

## ✨ Features

- **Auto-Initializing Database:** The system automatically creates the database and required tables upon the first launch.
- **User Authentication:** Secure login system with role-based access control.
- **Modern UI:** Clean, responsive design featuring Dark/Light mode toggle, powered by FlatLaf.
- **Dashboard & Analytics:** Real-time metrics including total headcount and payroll, plus visual charts using JFreeChart.
- **Employee Management:** Full CRUD operations (Add, Update, Delete) with profile photo support.
- **Search & Filters:** Quickly locate employees by Name, Department, or Role.
- **Data Export & Import:** 
  - Export records to PDF reports using iText.
  - Import and Export data seamlessly via CSV.
- **Email Notifications:** Built-in SMTP support for sending emails (powered by JavaMail).
- **Pagination:** Efficiently handle large datasets with built-in table pagination.

## 🛠️ Tech Stack

- **Language:** Java 8+
- **GUI Framework:** Java Swing
- **Database:** MySQL
- **Dependencies (Libraries):**
  - [FlatLaf](https://www.formdev.com/flatlaf/) (Modern UI Look & Feel)
  - [iTextPDF](https://itextpdf.com/) (PDF Generation)
  - [JFreeChart](https://www.jfree.org/jfreechart/) (Data Visualization & Reports)
  - MySQL Connector/J (Database Driver)
  - JavaMail & Activation (Email Services)

## 🚀 Setup & Installation

### Prerequisites
- **Java Development Kit (JDK) 8** or higher installed.
- **MySQL Server** installed and running on your machine.

### Configuration
1. Clone the repository to your local machine.
2. Open `employee/config.properties` and update the settings to match your environment:
   ```properties
   # Database Configuration
   db.url=jdbc:mysql://localhost:3306/
   db.name=employee_db
   db.username=root
   db.password=your_db_password

   # Email / SMTP Configuration (fill in your Gmail credentials)
   smtp.host=smtp.gmail.com
   smtp.port=587
   smtp.username=your_email@gmail.com
   smtp.password=your_app_password
   ```
   *(Note: If using Gmail, you will need to generate an "App Password" to use SMTP services securely.)*

### Running the Application

**Windows (Recommended):**
A convenient batch script is provided. Simply navigate into the `employee` folder and double-click `run.bat`. 
This script will automatically:
1. Compile the source code into a `bin` directory.
2. Launch the application.
3. Clean up the compiled files when you exit the application.

**Manual Compilation:**
If you prefer to run it manually via the terminal from the project root:
```bash
# Create bin directory
mkdir bin

# Compile the source files
javac -d bin -cp "employee;employee/lib/*" employee/*.java

# Run the application
java -cp "bin;employee/lib/*" employee.LoginGUI
```

## 📁 Folder Structure

```
employee-management-system/
│
├── employee/                 # Main package directory
│   ├── lib/                  # External JAR dependencies
│   ├── config.properties     # App configuration file
│   ├── logo.jpg              # Application logo
│   ├── run.bat               # Windows execution script
│   └── *.java                # Java source files (LoginGUI, EmployeeGUI, etc.)
└── README.md
```

## 🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the issues page and submit a Pull Request.


