package DB.JDBC.Ex7;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OnlineShoppingApp2 extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private int customerId = -1;
    Connection con;

    OnlineShoppingApp2() {
        setTitle("Online Shopping Application");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectDB();

        mainPanel.add(new LoginForm(this), "Login");
        mainPanel.add(new RegistrationForm(this), "Register");
        mainPanel.add(new CustomerDashboard(this), "Customer");
        mainPanel.add(new AdminDashboard(this), "Admin");

        add(mainPanel);
        showPanel("Login");
        setVisible(true);
    }

    void connectDB() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", "system", "1229");
            System.out.println("Connected to Oracle DB");
        } catch (Exception e) {
            con = null;
            JOptionPane.showMessageDialog(this,
                    "DB Connection Failed:\n" + e, "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    void setCustomerId(int id) {
        this.customerId = id;
    }

    int getCustomerId() {
        return customerId;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineShoppingApp2());
    }
}

class LoginForm extends JPanel {
    public LoginForm(OnlineShoppingApp2 app) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);

        JComboBox<String> userTypeCombo = new JComboBox<>(new String[] { "Customer", "Admin" });
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        add(new JLabel("Login As:"), g);
        g.gridy = 1;
        g.gridwidth = 1;
        add(new JLabel("User Type:"), g);
        g.gridx = 1;
        add(userTypeCombo, g);
        g.gridx = 0;
        g.gridy = 2;
        add(new JLabel("Username:"), g);
        g.gridx = 1;
        add(usernameField, g);
        g.gridx = 0;
        g.gridy = 3;
        add(new JLabel("Password:"), g);
        g.gridx = 1;
        add(passwordField, g);
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        JPanel bp = new JPanel();
        bp.add(loginBtn);
        bp.add(registerBtn);
        add(bp, g);

        loginBtn.addActionListener(e -> {
            String type = (String) userTypeCombo.getSelectedItem();
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            if ("Admin".equals(type)) {
                if ("admin".equals(user) && "admin123".equals(pass)) {
                    app.showPanel("Admin");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Admin Credentials");
                }
                return;
            }

            if (app.con == null) {
                JOptionPane.showMessageDialog(this, "DB not connected");
                return;
            }
            try {
                PreparedStatement st = app.con.prepareStatement(
                        "SELECT customer_id FROM Customerss WHERE name=? AND password=?");
                st.setString(1, user);
                st.setString(2, pass);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    app.setCustomerId(rs.getInt("customer_id"));
                    app.showPanel("Customer");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Customer Credentials");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        registerBtn.addActionListener(e -> app.showPanel("Register"));
    }
}

class RegistrationForm extends JPanel {
    public RegistrationForm(OnlineShoppingApp2 app) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JTextArea addressArea = new JTextArea(3, 20);

        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                String current = phoneField.getText();
                if (!Character.isDigit(ch) || current.length() >= 10) {
                    e.consume();
                }
            }
        });

        g.gridx = 0;
        g.gridy = 0;
        add(new JLabel("Name:"), g);
        g.gridx = 1;
        add(nameField, g);
        g.gridx = 0;
        g.gridy = 1;
        add(new JLabel("Email:"), g);
        g.gridx = 1;
        add(emailField, g);
        g.gridx = 0;
        g.gridy = 2;
        add(new JLabel("Phone:"), g);
        g.gridx = 1;
        add(phoneField, g);
        g.gridx = 0;
        g.gridy = 3;
        add(new JLabel("Password:"), g);
        g.gridx = 1;
        add(passField, g);
        g.gridx = 0;
        g.gridy = 4;
        add(new JLabel("Address:"), g);
        g.gridx = 1;
        add(new JScrollPane(addressArea), g);

        JButton registerBtn = new JButton("Register");
        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        add(registerBtn, g);

        registerBtn.addActionListener(e -> {
            if (app.con == null) {
                JOptionPane.showMessageDialog(this, "DB not connected");
                return;
            }
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String password = new String(passField.getPassword()).trim();
                String address = addressArea.getText().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "fill all the field");
                    return;
                }

                int atCount = email.length() - email.replace("@", "").length();
                int dotCount = email.length() - email.replace(".", "").length();
                int atIndex = email.indexOf('@');
                int dotIndex = email.lastIndexOf('.');
                if (atCount != 1 || dotCount != 1 || atIndex <= 0 || dotIndex <= atIndex + 1
                        || dotIndex == email.length() - 1) {
                    JOptionPane.showMessageDialog(this, "Email must contain exactly one '@' and one '.'.");
                    return;
                }

                if (!phone.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(this, "Mobile number must be exactly 10 digits (numbers only).");
                    return;
                }

                PreparedStatement emailCheckSt = app.con
                        .prepareStatement("SELECT COUNT(*) FROM Customerss WHERE LOWER(email)=LOWER(?)");
                emailCheckSt.setString(1, email);
                ResultSet emailRs = emailCheckSt.executeQuery();
                if (emailRs.next() && emailRs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "User with this email exists.");
                    return;
                }

                PreparedStatement st = app.con.prepareStatement(
                        "INSERT INTO Customerss (customer_id,name,email,phone,password,address,created_date) " +
                                "VALUES (customerss_seq.NEXTVAL,?,?,?,?,?,SYSDATE)");
                st.setString(1, name);
                st.setString(2, email);
                st.setString(3, phone);
                st.setString(4, password);
                st.setString(5, address);
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration Successful! Please login.");
                app.showPanel("Login");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}

class CustomerDashboard extends JPanel {
    CustomerDashboard(OnlineShoppingApp2 app) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);

        JLabel lbl = new JLabel("Select Action:");
        g.gridx = 0;
        g.gridy = 0;
        add(lbl, g);

        JComboBox<String> actionMenu = new JComboBox<>(new String[] {
                "A) Place an Order",
                "B) Shopping Cart"
        });
        actionMenu.setPreferredSize(new Dimension(240, 30));
        g.gridy = 1;
        add(actionMenu, g);

        JButton openBtn = new JButton("Open");
        g.gridy = 2;
        add(openBtn, g);

        openBtn.addActionListener(e -> {
            String choice = (String) actionMenu.getSelectedItem();
            if ("B) Shopping Cart".equals(choice)) {
                new ShoppingCartForm(app).setVisible(true);
            } else {
                new OrderPlacementForm(app).setVisible(true);
            }
        });
    }
}

class AdminDashboard extends JPanel {
    AdminDashboard(OnlineShoppingApp2 app) {
        setLayout(new GridLayout(3, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton addBtn = new JButton("Add Product");
        JButton updateBtn = new JButton("Update Product");
        JButton manageBtn = new JButton("Manage Products");

        add(addBtn);
        add(updateBtn);
        add(manageBtn);

        addBtn.addActionListener(e -> new AddProductForm(app).setVisible(true));
        updateBtn.addActionListener(e -> new UpdateProductForm(app).setVisible(true));
        manageBtn.addActionListener(e -> new ManageProductsForm(app).setVisible(true));
    }
}

class OrderPlacementForm extends JFrame {
    private OnlineShoppingApp2 app;
    private DefaultTableModel productModel;
    private JTextField quantityField;

    public OrderPlacementForm(OnlineShoppingApp2 app) {
        this.app = app;
        setTitle("A) Place an Order");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        productModel = new DefaultTableModel(
                new Object[] { "Product ID", "Name", "Category", "Price", "Stock" }, 0);
        JTable table = new JTable(productModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        quantityField = new JTextField(5);
        JComboBox<String> actionBox = new JComboBox<>(new String[] {
                "Add to Cart",
                "Place Order Now"
        });
        JButton goBtn = new JButton("Go");
        bottom.add(new JLabel("Quantity:"));
        bottom.add(quantityField);
        bottom.add(actionBox);
        bottom.add(goBtn);
        add(bottom, BorderLayout.SOUTH);

        goBtn.addActionListener(e -> {
            String action = (String) actionBox.getSelectedItem();
            if ("Place Order Now".equals(action)) {
                placeOrderNow(table);
            } else {
                addToCart(table);
            }
        });

        loadProducts();
    }

    private void loadProducts() {
        try {
            ResultSet rs = app.con.prepareStatement(
                    "SELECT product_id,product_name,category,price,stock_quantity FROM Productss")
                    .executeQuery();
            productModel.setRowCount(0);
            while (rs.next())
                productModel.addRow(new Object[] {
                        rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getString("category"), rs.getDouble("price"),
                        rs.getInt("stock_quantity") });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addToCart(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product");
            return;
        }
        try {
            int qty = Integer.parseInt(quantityField.getText());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be > 0");
                return;
            }

            int productId = (int) productModel.getValueAt(row, 0);
            int stock = (int) productModel.getValueAt(row, 4);
            if (qty > stock) {
                JOptionPane.showMessageDialog(this, "Not enough stock");
                return;
            }

            PreparedStatement getCart = app.con.prepareStatement(
                    "SELECT cart_id FROM Cartss WHERE customer_id=?");
            getCart.setInt(1, app.getCustomerId());
            ResultSet rs = getCart.executeQuery();
            int cartId;
            if (rs.next()) {
                cartId = rs.getInt("cart_id");
            } else {
                PreparedStatement create = app.con.prepareStatement(
                        "INSERT INTO Cartss (cart_id,customer_id,created_date) VALUES (cartss_seq.NEXTVAL,?,SYSDATE)");
                create.setInt(1, app.getCustomerId());
                create.executeUpdate();
                rs = getCart.executeQuery();
                rs.next();
                cartId = rs.getInt("cart_id");
            }

            PreparedStatement ins = app.con.prepareStatement(
                    "INSERT INTO CartItemss (cart_item_id,cart_id,product_id,quantity) VALUES (cartitemss_seq.NEXTVAL,?,?,?)");
            ins.setInt(1, cartId);
            ins.setInt(2, productId);
            ins.setInt(3, qty);
            ins.executeUpdate();
            JOptionPane.showMessageDialog(this, "Added to cart!");
            quantityField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void placeOrderNow(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity");
            return;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be > 0");
            return;
        }

        int productId = (int) productModel.getValueAt(row, 0);
        double price = (double) productModel.getValueAt(row, 3);
        int stock = (int) productModel.getValueAt(row, 4);

        if (qty > stock) {
            JOptionPane.showMessageDialog(this, "Not enough stock");
            return;
        }

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = app.con.getAutoCommit();
            app.con.setAutoCommit(false);
            double total = qty * price;

            PreparedStatement orderSt = app.con.prepareStatement(
                    "INSERT INTO Orderss (order_id,customer_id,order_date,total_amount,order_status) " +
                            "VALUES (orderss_seq.NEXTVAL,?,SYSDATE,?,'Pending')");
            orderSt.setInt(1, app.getCustomerId());
            orderSt.setDouble(2, total);
            orderSt.executeUpdate();

            PreparedStatement getOrder = app.con.prepareStatement(
                    "SELECT orderss_seq.CURRVAL AS order_id FROM dual");
            ResultSet ors = getOrder.executeQuery();
            ors.next();
            int orderId = ors.getInt("order_id");

            PreparedStatement oiSt = app.con.prepareStatement(
                    "INSERT INTO OrderItemss (order_item_id,order_id,product_id,quantity,price) " +
                            "VALUES (orderitemss_seq.NEXTVAL,?,?,?,?)");
            oiSt.setInt(1, orderId);
            oiSt.setInt(2, productId);
            oiSt.setInt(3, qty);
            oiSt.setDouble(4, price);
            oiSt.executeUpdate();

            PreparedStatement stockSt = app.con.prepareStatement(
                    "UPDATE Productss SET stock_quantity = stock_quantity - ? " +
                            "WHERE product_id=? AND stock_quantity >= ?");
            stockSt.setInt(1, qty);
            stockSt.setInt(2, productId);
            stockSt.setInt(3, qty);
            int updated = stockSt.executeUpdate();
            if (updated == 0) {
                app.con.rollback();
                app.con.setAutoCommit(oldAutoCommit);
                JOptionPane.showMessageDialog(this,
                        "Order failed: insufficient stock for product ID " + productId);
                return;
            }

            app.con.commit();
            app.con.setAutoCommit(oldAutoCommit);

            JOptionPane.showMessageDialog(this, "Order placed! Order ID: " + orderId);
            loadProducts();
            quantityField.setText("");
            dispose();
        } catch (SQLException ex) {
            try {
                app.con.rollback();
                app.con.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignore) {
            }
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

class ShoppingCartForm extends JFrame {
    private OnlineShoppingApp2 app;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;

    public ShoppingCartForm(OnlineShoppingApp2 app) {
        this.app = app;
        setTitle("B) Shopping Cart");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cartModel = new DefaultTableModel(
                new Object[] { "Cart Item ID", "Product Name", "Quantity", "Price", "Subtotal" }, 0);
        JTable cartTable = new JTable(cartModel);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        totalLabel = new JLabel("Total: 0.00");
        JButton updateBtn = new JButton("Update Quantity");
        JButton removeBtn = new JButton("Remove Item");
        JButton placeOrderBtn = new JButton("Place Order");

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(updateBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(placeOrderBtn);
        bottom.add(totalLabel, BorderLayout.NORTH);
        bottom.add(btnPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        updateBtn.addActionListener(e -> updateQuantity(cartTable));
        removeBtn.addActionListener(e -> removeItem(cartTable));
        placeOrderBtn.addActionListener(e -> placeOrderFromCart());

        loadCartItems();
    }

    private void loadCartItems() {
        try {
            PreparedStatement getCart = app.con.prepareStatement(
                    "SELECT cart_id FROM Cartss WHERE customer_id=?");
            getCart.setInt(1, app.getCustomerId());
            ResultSet rs = getCart.executeQuery();
            if (!rs.next()) {
                cartModel.setRowCount(0);
                totalLabel.setText("Total: 0.00");
                return;
            }
            int cartId = rs.getInt("cart_id");

            PreparedStatement st = app.con.prepareStatement(
                    "SELECT ci.cart_item_id,p.product_name,ci.quantity,p.price," +
                            "(ci.quantity*p.price) AS subtotal FROM CartItemss ci " +
                            "JOIN Productss p ON ci.product_id=p.product_id WHERE ci.cart_id=?");
            st.setInt(1, cartId);
            ResultSet r = st.executeQuery();

            cartModel.setRowCount(0);
            double total = 0;
            while (r.next()) {
                double sub = r.getDouble("subtotal");
                total += sub;
                cartModel.addRow(new Object[] { r.getInt("cart_item_id"),
                        r.getString("product_name"), r.getInt("quantity"),
                        r.getDouble("price"), sub });
            }
            totalLabel.setText(String.format("Total: %.2f", total));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void updateQuantity(JTable cartTable) {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "New quantity:");
        if (input == null || input.trim().isEmpty())
            return;
        try {
            int qty = Integer.parseInt(input);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be > 0");
                return;
            }
            int cartItemId = (int) cartModel.getValueAt(row, 0);
            PreparedStatement st = app.con.prepareStatement(
                    "UPDATE CartItemss SET quantity=? WHERE cart_item_id=?");
            st.setInt(1, qty);
            st.setInt(2, cartItemId);
            st.executeUpdate();
            loadCartItems();
            JOptionPane.showMessageDialog(this, "Quantity updated");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void removeItem(JTable cartTable) {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item");
            return;
        }
        try {
            int cartItemId = (int) cartModel.getValueAt(row, 0);
            PreparedStatement st = app.con.prepareStatement(
                    "DELETE FROM CartItemss WHERE cart_item_id=?");
            st.setInt(1, cartItemId);
            st.executeUpdate();
            loadCartItems();
            JOptionPane.showMessageDialog(this, "Item removed");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void placeOrderFromCart() {
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = app.con.getAutoCommit();
            app.con.setAutoCommit(false);

            PreparedStatement getCart = app.con.prepareStatement(
                    "SELECT cart_id FROM Cartss WHERE customer_id=?");
            getCart.setInt(1, app.getCustomerId());
            ResultSet rs = getCart.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Cart is empty");
                app.con.rollback();
                app.con.setAutoCommit(oldAutoCommit);
                return;
            }
            int cartId = rs.getInt("cart_id");

            PreparedStatement totalSt = app.con.prepareStatement(
                    "SELECT SUM(ci.quantity*p.price) AS total FROM CartItemss ci " +
                            "JOIN Productss p ON ci.product_id=p.product_id WHERE ci.cart_id=?");
            totalSt.setInt(1, cartId);
            ResultSet tr = totalSt.executeQuery();
            tr.next();
            double total = tr.getDouble("total");
            if (total <= 0) {
                JOptionPane.showMessageDialog(this, "Cart is empty");
                app.con.rollback();
                app.con.setAutoCommit(oldAutoCommit);
                return;
            }

            PreparedStatement orderSt = app.con.prepareStatement(
                    "INSERT INTO Orderss (order_id,customer_id,order_date,total_amount,order_status) " +
                            "VALUES (orderss_seq.NEXTVAL,?,SYSDATE,?,'Pending')");
            orderSt.setInt(1, app.getCustomerId());
            orderSt.setDouble(2, total);
            orderSt.executeUpdate();

            PreparedStatement getOrder = app.con.prepareStatement(
                    "SELECT orderss_seq.CURRVAL AS order_id FROM dual");
            ResultSet or = getOrder.executeQuery();
            or.next();
            int orderId = or.getInt("order_id");

            PreparedStatement items = app.con.prepareStatement(
                    "SELECT product_id,quantity FROM CartItemss WHERE cart_id=?");
            items.setInt(1, cartId);
            ResultSet ir = items.executeQuery();
            while (ir.next()) {
                int pid = ir.getInt("product_id");
                int qty = ir.getInt("quantity");
                PreparedStatement pr = app.con.prepareStatement(
                        "SELECT price FROM Productss WHERE product_id=?");
                pr.setInt(1, pid);
                ResultSet prRs = pr.executeQuery();
                prRs.next();
                double price = prRs.getDouble("price");

                PreparedStatement oi = app.con.prepareStatement(
                        "INSERT INTO OrderItemss (order_item_id,order_id,product_id,quantity,price) " +
                                "VALUES (orderitemss_seq.NEXTVAL,?,?,?,?)");
                oi.setInt(1, orderId);
                oi.setInt(2, pid);
                oi.setInt(3, qty);
                oi.setDouble(4, price);
                oi.executeUpdate();

                PreparedStatement stockSt = app.con.prepareStatement(
                        "UPDATE Productss SET stock_quantity = stock_quantity - ? " +
                                "WHERE product_id=? AND stock_quantity >= ?");
                stockSt.setInt(1, qty);
                stockSt.setInt(2, pid);
                stockSt.setInt(3, qty);
                int updated = stockSt.executeUpdate();
                if (updated == 0) {
                    app.con.rollback();
                    app.con.setAutoCommit(oldAutoCommit);
                    JOptionPane.showMessageDialog(this,
                            "Order failed: insufficient stock for product ID " + pid);
                    return;
                }
            }

            PreparedStatement clear = app.con.prepareStatement(
                    "DELETE FROM CartItemss WHERE cart_id=?");
            clear.setInt(1, cartId);
            clear.executeUpdate();

            app.con.commit();
            app.con.setAutoCommit(oldAutoCommit);

            JOptionPane.showMessageDialog(this, "Order placed! Order ID: " + orderId);
            dispose();
        } catch (SQLException ex) {
            try {
                app.con.rollback();
                app.con.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignore) {
            }
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

class AddProductForm extends JFrame {
    public AddProductForm(OnlineShoppingApp2 app) {
        setTitle("Add Product");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);

        JTextField nameField = new JTextField(15);
        JTextField catField = new JTextField(15);
        JTextField priceField = new JTextField(15);
        JTextField stockField = new JTextField(15);

        g.gridx = 0;
        g.gridy = 0;
        add(new JLabel("Product Name:"), g);
        g.gridx = 1;
        add(nameField, g);
        g.gridx = 0;
        g.gridy = 1;
        add(new JLabel("Category:"), g);
        g.gridx = 1;
        add(catField, g);
        g.gridx = 0;
        g.gridy = 2;
        add(new JLabel("Price:"), g);
        g.gridx = 1;
        add(priceField, g);
        g.gridx = 0;
        g.gridy = 3;
        add(new JLabel("Stock:"), g);
        g.gridx = 1;
        add(stockField, g);

        JButton addBtn = new JButton("Add Product");
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 2;
        add(addBtn, g);

        addBtn.addActionListener(e -> {
            try {
                PreparedStatement st = app.con.prepareStatement(
                        "INSERT INTO Productss (product_id,product_name,category,price,stock_quantity) " +
                                "VALUES (productss_seq.NEXTVAL,?,?,?,?)");
                st.setString(1, nameField.getText());
                st.setString(2, catField.getText());
                st.setDouble(3, Double.parseDouble(priceField.getText()));
                st.setInt(4, Integer.parseInt(stockField.getText()));
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid price and stock");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}

class UpdateProductForm extends JFrame {
    private OnlineShoppingApp2 app;
    private JComboBox<String> productCombo;
    private JTextField nameField, catField, priceField, stockField;
    private boolean loading = false;

    public UpdateProductForm(OnlineShoppingApp2 app) {
        this.app = app;
        setTitle("Update Product");
        setSize(400, 320);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);

        productCombo = new JComboBox<>();
        nameField = new JTextField(15);
        catField = new JTextField(15);
        priceField = new JTextField(15);
        stockField = new JTextField(15);

        g.gridx = 0;
        g.gridy = 0;
        add(new JLabel("Select Product:"), g);
        g.gridx = 1;
        add(productCombo, g);
        g.gridx = 0;
        g.gridy = 1;
        add(new JLabel("Name:"), g);
        g.gridx = 1;
        add(nameField, g);
        g.gridx = 0;
        g.gridy = 2;
        add(new JLabel("Category:"), g);
        g.gridx = 1;
        add(catField, g);
        g.gridx = 0;
        g.gridy = 3;
        add(new JLabel("Price:"), g);
        g.gridx = 1;
        add(priceField, g);
        g.gridx = 0;
        g.gridy = 4;
        add(new JLabel("Stock:"), g);
        g.gridx = 1;
        add(stockField, g);

        JButton updateBtn = new JButton("Update Product");
        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        add(updateBtn, g);

        loading = true;
        loadProductsIntoCombo();
        loading = false;
        if (productCombo.getItemCount() > 0)
            loadProductDetails();

        productCombo.addActionListener(e -> {
            if (!loading)
                loadProductDetails();
        });

        updateBtn.addActionListener(e -> {
            try {
                String selected = productCombo.getSelectedItem().toString();
                int pid = Integer.parseInt(selected.split(" - ")[0]);
                PreparedStatement st = app.con.prepareStatement(
                        "UPDATE Productss SET product_name=?,category=?,price=?,stock_quantity=? WHERE product_id=?");
                st.setString(1, nameField.getText());
                st.setString(2, catField.getText());
                st.setDouble(3, Double.parseDouble(priceField.getText()));
                st.setInt(4, Integer.parseInt(stockField.getText()));
                st.setInt(5, pid);
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }

    private void loadProductsIntoCombo() {
        try {
            ResultSet rs = app.con.prepareStatement(
                    "SELECT product_id,product_name FROM Productss ORDER BY product_id").executeQuery();
            productCombo.removeAllItems();
            while (rs.next())
                productCombo.addItem(rs.getInt("product_id") + " - " + rs.getString("product_name"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void loadProductDetails() {
        Object sel = productCombo.getSelectedItem();
        if (sel == null)
            return;
        try {
            int pid = Integer.parseInt(sel.toString().split(" - ")[0]);
            PreparedStatement st = app.con.prepareStatement(
                    "SELECT product_name,category,price,stock_quantity FROM Productss WHERE product_id=?");
            st.setInt(1, pid);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("product_name"));
                catField.setText(rs.getString("category"));
                priceField.setText(String.valueOf(rs.getDouble("price")));
                stockField.setText(String.valueOf(rs.getInt("stock_quantity")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

class ManageProductsForm extends JFrame {
    private OnlineShoppingApp2 app;
    private DefaultTableModel model;

    public ManageProductsForm(OnlineShoppingApp2 app) {
        this.app = app;
        setTitle("Manage Products");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[] { "ID", "Name", "Category", "Price", "Stock" }, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Product");
        JButton refreshBtn = new JButton("Refresh");
        JPanel bottom = new JPanel();
        bottom.add(deleteBtn);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a product");
                return;
            }
            try {
                int pid = (Integer) model.getValueAt(row, 0);
                String name = (String) model.getValueAt(row, 1);
                if (JOptionPane.showConfirmDialog(this, "Delete '" + name + "'?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    PreparedStatement st = app.con.prepareStatement(
                            "DELETE FROM Productss WHERE product_id=?");
                    st.setInt(1, pid);
                    st.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Product deleted!");
                    loadProducts();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        refreshBtn.addActionListener(e -> loadProducts());
        loadProducts();
    }

    private void loadProducts() {
        try {
            ResultSet rs = app.con.prepareStatement(
                    "SELECT product_id,product_name,category,price,stock_quantity FROM Productss ORDER BY product_id")
                    .executeQuery();
            model.setRowCount(0);
            while (rs.next())
                model.addRow(new Object[] { rs.getInt("product_id"), rs.getString("product_name"),
                        rs.getString("category"), rs.getDouble("price"), rs.getInt("stock_quantity") });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}