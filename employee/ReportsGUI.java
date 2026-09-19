package employee;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class ReportsGUI extends JFrame {
    private EmployeeManager manager;

    public ReportsGUI(EmployeeManager manager) {
        this.manager = manager;
        setTitle("Reports & Analytics");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            ImageIcon icon = new ImageIcon("employee/logo.jpg");
            if (icon.getIconWidth() > 0) setIconImage(icon.getImage());
        } catch (Exception ignored) {}

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Employees per Department", createDeptPieChart());
        tabs.addTab("Salary by Department", createSalaryBarChart());
        add(tabs);
    }

    private JPanel createDeptPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            List<Employee> employees = manager.getEmployees("", "All", "All", Integer.MAX_VALUE, 0);
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (Employee e : employees) {
                String dept = e.getDepartment() != null && !e.getDepartment().isEmpty() ? e.getDepartment() : "Unknown";
                counts.merge(dept, 1, Integer::sum);
            }
            counts.forEach(dataset::setValue);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Headcount by Department", dataset, true, true, false);
        chart.getPlot().setBackgroundAlpha(0.8f);
        return new ChartPanel(chart);
    }

    private JPanel createSalaryBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            List<Employee> employees = manager.getEmployees("", "All", "All", Integer.MAX_VALUE, 0);
            Map<String, Double> totals = new LinkedHashMap<>();
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (Employee e : employees) {
                String dept = e.getDepartment() != null && !e.getDepartment().isEmpty() ? e.getDepartment() : "Unknown";
                totals.merge(dept, e.getSalary(), Double::sum);
                counts.merge(dept, 1, Integer::sum);
            }
            totals.forEach((dept, total) -> {
                double avg = total / counts.get(dept);
                dataset.addValue(avg, "Avg Salary ($)", dept);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Average Salary by Department", "Department", "Avg Salary ($)",
            dataset, PlotOrientation.VERTICAL, false, true, false);
        return new ChartPanel(chart);
    }
}

