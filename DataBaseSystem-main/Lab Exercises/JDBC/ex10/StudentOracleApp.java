package DB.JDBC.Ex7;
import java.awt.*;
import java.sql.*;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudentOracleApp extends JFrame {

    JTextField idField, avgField;
    JButton avgBtn;
    JTable table;
    DefaultTableModel model;

    Connection con;

    static class StudentRecord {
        final int id;
        final String name;
        final int m1;
        final int m2;
        final int m3;

        StudentRecord(int id, String name, int m1, int m2, int m3) {
            this.id = id;
            this.name = name;
            this.m1 = m1;
            this.m2 = m2;
            this.m3 = m3;
        }
    }

    StudentRecord fetchStudentById(int studentId) {
        if (con == null) {
            return null;
        }
        String sql = "SELECT stdid, name, mark1, mark2, mark3 FROM student WHERE stdid=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new StudentRecord(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getInt(3),
                            rs.getInt(4),
                            rs.getInt(5));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    void printStudentDetails(int id, String name, int m1, int m2, int m3, String actionLine) {
        System.out.println("id : " + id);
        System.out.println("name : " + name);
        System.out.println("mark1 : " + m1);
        System.out.println("mark2 : " + m2);
        System.out.println("mark3 : " + m3);
        if (actionLine != null && !actionLine.isEmpty()) {
            System.out.println(actionLine);
        }
    }

    void printStudentById(int studentId, String actionLineIfFound) {
        if (con == null) {
            return;
        }
        String sql = "SELECT stdid, name, mark1, mark2, mark3 FROM student WHERE stdid=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    printStudentDetails(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getInt(3),
                            rs.getInt(4),
                            rs.getInt(5),
                            actionLineIfFound);
                } else {
                    System.out.println("Student with id " + studentId + " not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void printAllStudents() {
        if (con == null) {
            return;
        }
        String sql = "SELECT stdid, name, mark1, mark2, mark3 FROM student ORDER BY stdid";
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- ALL STUDENTS ---");
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                int m1 = rs.getInt(3);
                int m2 = rs.getInt(4);
                int m3 = rs.getInt(5);
                System.out.println(
                        "id : " + id + ", name : " + name + ", mark1 : " + m1 + ", mark2 : " + m2 + ", mark3 : " + m3);
            }
            System.out.println("--------------------\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    StudentOracleApp() {
        setTitle("Student Management - Oracle");
        setLayout(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem insertItem = new JMenuItem("Insert");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem updateItem = new JMenuItem("Update");
        menu.add(insertItem);
        menu.add(deleteItem);
        menu.add(updateItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        JLabel idLabel = new JLabel("Enter Student ID:");
        idLabel.setBounds(50, 30, 200, 30);
        add(idLabel);

        idField = new JTextField();
        idField.setBounds(220, 30, 200, 30);
        add(idField);

        avgBtn = new JButton("Calculate Avg");
        avgBtn.setBounds(50, 80, 180, 35);
        add(avgBtn);

        avgField = new JTextField();
        avgField.setBounds(250, 80, 200, 35);
        avgField.setEditable(false);
        add(avgField);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] { "ID", "Name", "M1", "M2", "M3" });

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 200, 1200, 500);
        add(sp);

        connectDB();
        if (con != null) {
            loadTable();
        } else {
            avgBtn.setEnabled(false);
            insertItem.setEnabled(false);
            deleteItem.setEnabled(false);
            updateItem.setEnabled(false);
        }

        avgBtn.addActionListener(e -> calculateAvg());
        insertItem.addActionListener(e -> new InsertForm());
        deleteItem.addActionListener(e -> deleteStudent());
        updateItem.addActionListener(e -> new UpdateForm());

        setVisible(true);

        Thread terminalThread = new Thread(this::runTerminalMenu, "terminal-menu-thread");
        terminalThread.setDaemon(true);
        terminalThread.start();
    }

    void runTerminalMenu() {
        if (con == null) {
            System.out.println("DB not connected. Terminal menu disabled.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n--- TERMINAL MENU ---");
                System.out.println("1. Insert Student");
                System.out.println("2. Update Student");
                System.out.println("3. Delete Student");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");

                String choice = sc.nextLine().trim();
                if ("1".equals(choice)) {
                    terminalInsert(sc);
                } else if ("2".equals(choice)) {
                    terminalUpdate(sc);
                } else if ("3".equals(choice)) {
                    terminalDelete(sc);
                } else if ("4".equals(choice)) {
                    System.out.println("Exiting terminal menu.");
                    break;
                } else {
                    System.out.println("Invalid choice. Enter 1, 2, 3, or 4.");
                    continue;
                }
            } catch (Exception ex) {
                System.out.println("Operation failed: " + ex.getMessage());
            }
        }
    }

    void terminalInsert(Scanner sc) {
        try {
            System.out.print("Enter ID: ");
            int studentId = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Enter Name: ");
            String studentName = sc.nextLine().trim();

            System.out.print("Enter Mark1: ");
            int mark1 = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Enter Mark2: ");
            int mark2 = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Enter Mark3: ");
            int mark3 = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement pst = con.prepareStatement("INSERT INTO student VALUES(?,?,?,?,?)");
            pst.setInt(1, studentId);
            pst.setString(2, studentName);
            pst.setInt(3, mark1);
            pst.setInt(4, mark2);
            pst.setInt(5, mark3);

            pst.executeUpdate();

            System.out.println();
            printStudentDetails(studentId, studentName, mark1, mark2, mark3, "inserted into DB.");
            printAllStudents();
            SwingUtilities.invokeLater(this::loadTable);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input. ID and marks must be integers.");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                System.out.println("Insert failed: Student ID already exists.");
            } else {
                System.out.println("Insert failed: " + ex.getMessage());
            }
        }
    }

    void terminalUpdate(Scanner sc) {
        try {
            System.out.print("Enter ID to update: ");
            int studentId = Integer.parseInt(sc.nextLine().trim());

            StudentRecord loaded = fetchStudentById(studentId);
            if (loaded == null) {
                System.out.println("Student not found.");
                return;
            }

            printStudentDetails(loaded.id, loaded.name, loaded.m1, loaded.m2, loaded.m3, "current data.");

            System.out.print("Enter New Name (leave empty to keep same): ");
            String newNameRaw = sc.nextLine().trim();
            String newName = newNameRaw.isEmpty() ? loaded.name : newNameRaw;

            System.out.print("Enter New Mark1 (leave empty to keep same): ");
            String m1Raw = sc.nextLine().trim();
            int newM1 = m1Raw.isEmpty() ? loaded.m1 : Integer.parseInt(m1Raw);

            System.out.print("Enter New Mark2 (leave empty to keep same): ");
            String m2Raw = sc.nextLine().trim();
            int newM2 = m2Raw.isEmpty() ? loaded.m2 : Integer.parseInt(m2Raw);

            System.out.print("Enter New Mark3 (leave empty to keep same): ");
            String m3Raw = sc.nextLine().trim();
            int newM3 = m3Raw.isEmpty() ? loaded.m3 : Integer.parseInt(m3Raw);

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE student SET name=?, mark1=?, mark2=?, mark3=? WHERE stdid=?");
            pst.setString(1, newName);
            pst.setInt(2, newM1);
            pst.setInt(3, newM2);
            pst.setInt(4, newM3);
            pst.setInt(5, studentId);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                printStudentById(studentId, "updated in DB.");
                printAllStudents();
                SwingUtilities.invokeLater(this::loadTable);
            } else {
                System.out.println("Update failed: Student not found.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input. ID and marks must be integers.");
        } catch (SQLException ex) {
            System.out.println("Update failed: " + ex.getMessage());
        }
    }

    void terminalDelete(Scanner sc) {
        try {
            System.out.print("Enter ID to delete: ");
            int studentId = Integer.parseInt(sc.nextLine().trim());

            StudentRecord loaded = fetchStudentById(studentId);
            if (loaded == null) {
                System.out.println("Student not found.");
                return;
            }

            printStudentDetails(loaded.id, loaded.name, loaded.m1, loaded.m2, loaded.m3, "will be deleted.");
            System.out.print("Confirm delete? (y/n): ");
            String confirm = sc.nextLine().trim();
            if (!"y".equalsIgnoreCase(confirm) && !"yes".equalsIgnoreCase(confirm)) {
                System.out.println("Delete cancelled.");
                return;
            }

            PreparedStatement pst = con.prepareStatement("DELETE FROM student WHERE stdid=?");
            pst.setInt(1, studentId);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("deleted from DB.");
                printAllStudents();
                SwingUtilities.invokeLater(this::loadTable);
            } else {
                System.out.println("Delete failed: Student not found.");
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input. ID must be an integer.");
        } catch (SQLException ex) {
            System.out.println("Delete failed: " + ex.getMessage());
        }
    }

    void connectDB() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");

            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String user = "system";
            String pass = "1229";

            con = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to Oracle DB");

        } catch (Exception e) {
            con = null;
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "DB Connection Failed:\n" + e,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void loadTable() {
        if (con == null) {
            return;
        }
        try {
            model.setRowCount(0);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM student");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getInt(4),
                        rs.getInt(5)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void calculateAvg() {
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Not connected to DB.");
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText());

            PreparedStatement pst = con.prepareStatement(
                    "SELECT mark1, mark2, mark3 FROM student WHERE stdid=?");
            pst.setInt(1, id);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int avg = (rs.getInt(1) + rs.getInt(2) + rs.getInt(3)) / 3;
                avgField.setText(String.valueOf(avg));
            } else {
                JOptionPane.showMessageDialog(this, "Student not found!");
                avgField.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input!");
            avgField.setText("");
        }
    }

    class InsertForm extends JFrame {
        JTextField id, name, m1, m2, m3;

        InsertForm() {
            setTitle("Insert Student");
            setSize(350, 300);
            setLayout(new GridLayout(6, 2, 10, 10));
            setLocationRelativeTo(null);

            id = new JTextField();
            name = new JTextField();
            m1 = new JTextField();
            m2 = new JTextField();
            m3 = new JTextField();

            add(new JLabel("ID"));
            add(id);
            add(new JLabel("Name"));
            add(name);
            add(new JLabel("Mark1"));
            add(m1);
            add(new JLabel("Mark2"));
            add(m2);
            add(new JLabel("Mark3"));
            add(m3);

            JButton save = new JButton("Save");
            add(save);

            save.addActionListener(e -> {
                try {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Not connected to DB.");
                        return;
                    }

                    int studentId = Integer.parseInt(id.getText());
                    String studentName = name.getText();
                    int mark1 = Integer.parseInt(m1.getText());
                    int mark2 = Integer.parseInt(m2.getText());
                    int mark3 = Integer.parseInt(m3.getText());

                    PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO student VALUES(?,?,?,?,?)");

                    pst.setInt(1, studentId);
                    pst.setString(2, studentName);
                    pst.setInt(3, mark1);
                    pst.setInt(4, mark2);
                    pst.setInt(5, mark3);

                    pst.executeUpdate();

                    printStudentDetails(studentId, studentName, mark1, mark2, mark3, "inserted into DB.");

                    loadTable();
                    printAllStudents();
                    dispose();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Insert Error!");
                }
            });

            setVisible(true);
        }
    }

    void deleteStudent() {
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Not connected to DB.");
            return;
        }
        String id = JOptionPane.showInputDialog("Enter ID to delete");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int studentId = Integer.parseInt(id.trim());
                printStudentById(studentId, "will be deleted...");

                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM student WHERE stdid=?");

                pst.setInt(1, studentId);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    System.out.println("deleted from DB.");
                    printAllStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found!");
                }

                loadTable();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Delete Error!");
            }
        }
    }

    class UpdateForm extends JFrame {
        JTextField id, name, m1, m2, m3;

        StudentRecord loaded;

        UpdateForm() {
            setTitle("Update Student");
            setSize(350, 300);
            setLayout(new GridLayout(7, 2, 10, 10));
            setLocationRelativeTo(null);

            id = new JTextField();

            add(new JLabel("Enter ID"));
            add(id);

            JButton fetch = new JButton("Fetch");
            add(new JLabel(""));
            add(fetch);

            name = new JTextField();
            m1 = new JTextField();
            m2 = new JTextField();
            m3 = new JTextField();

            name.setEnabled(false);
            m1.setEnabled(false);
            m2.setEnabled(false);
            m3.setEnabled(false);

            add(new JLabel("New Name"));
            add(name);
            add(new JLabel("Mark1"));
            add(m1);
            add(new JLabel("Mark2"));
            add(m2);
            add(new JLabel("Mark3"));
            add(m3);

            JButton update = new JButton("Update");
            update.setEnabled(false);
            add(new JLabel(""));
            add(update);

            fetch.addActionListener(e -> {
                try {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Not connected to DB.");
                        return;
                    }

                    String rawId = id.getText();
                    if (rawId == null || rawId.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Enter a student ID.");
                        return;
                    }

                    int studentId = Integer.parseInt(rawId.trim());

                    loaded = fetchStudentById(studentId);
                    if (loaded == null) {
                        JOptionPane.showMessageDialog(this, "Student not found!");
                        name.setText("");
                        m1.setText("");
                        m2.setText("");
                        m3.setText("");
                        name.setEnabled(false);
                        m1.setEnabled(false);
                        m2.setEnabled(false);
                        m3.setEnabled(false);
                        update.setEnabled(false);
                        return;
                    }

                    // Display all fields except allowing ID edits
                    id.setEnabled(false);
                    name.setEnabled(true);
                    m1.setEnabled(true);
                    m2.setEnabled(true);
                    m3.setEnabled(true);
                    update.setEnabled(true);

                    name.setText(loaded.name);
                    m1.setText(String.valueOf(loaded.m1));
                    m2.setText(String.valueOf(loaded.m2));
                    m3.setText(String.valueOf(loaded.m3));

                    printStudentDetails(loaded.id, loaded.name, loaded.m1, loaded.m2, loaded.m3,
                            "loaded for update...");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid ID!");
                }
            });

            update.addActionListener(e -> {
                try {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Not connected to DB.");
                        return;
                    }

                    if (loaded == null) {
                        JOptionPane.showMessageDialog(this, "Fetch a student by ID first.");
                        return;
                    }

                    int studentId = loaded.id;

                    String newName = name.getText() == null ? "" : name.getText().trim();
                    if (newName.isEmpty()) {
                        newName = loaded.name;
                    }

                    String rawM1 = m1.getText() == null ? "" : m1.getText().trim();
                    String rawM2 = m2.getText() == null ? "" : m2.getText().trim();
                    String rawM3 = m3.getText() == null ? "" : m3.getText().trim();

                    int newM1 = rawM1.isEmpty() ? loaded.m1 : Integer.parseInt(rawM1);
                    int newM2 = rawM2.isEmpty() ? loaded.m2 : Integer.parseInt(rawM2);
                    int newM3 = rawM3.isEmpty() ? loaded.m3 : Integer.parseInt(rawM3);

                    PreparedStatement pst = con.prepareStatement(
                            "UPDATE student SET name=?, mark1=?, mark2=?, mark3=? WHERE stdid=?");

                    pst.setString(1, newName);
                    pst.setInt(2, newM1);
                    pst.setInt(3, newM2);
                    pst.setInt(4, newM3);
                    pst.setInt(5, studentId);

                    int rows = pst.executeUpdate();

                    if (rows > 0) {
                        printStudentById(studentId, "updated in DB.");
                        printAllStudents();
                    } else {
                        JOptionPane.showMessageDialog(this, "Student not found!");
                    }

                    loadTable();
                    dispose();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Update Error!");
                }
            });

            setVisible(true);
        }
    }

    public static void main(String[] args) {
        new StudentOracleApp();
    }
}