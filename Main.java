
package org.example;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "light";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Connected to MySQL Successfully!");

            int choice;

            do {
                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert 3 Sample Orders");
                System.out.println("2. Search Orders by Status");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Display Total Order Value and Delete Cancelled Order");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        insertOrders(con);
                        break;

                    case 2:
                        searchOrders(con);
                        break;

                    case 3:
                        increaseQuantity(con);
                        break;

                    case 4:
                        updateStatus(con);
                        break;

                    case 5:
                        totalAndDelete(con);
                        break;

                    case 6:
                        System.out.println("Exiting program...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 1. Insert at least three records
    static void insertOrders(Connection con) {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Object[][] orders = {
                    {101, "Aman", "Laptop", 3, 55000.0, "Pending"},
                    {102, "Riya", "Mobile", 5, 25000.0, "Shipped"},
                    {103, "Rahul", "Headphones", 2, 2000.0, "Cancelled"}
            };

            for (Object[] order : orders) {

                ps.setInt(1, (int) order[0]);
                ps.setString(2, (String) order[1]);
                ps.setString(3, (String) order[2]);
                ps.setInt(4, (int) order[3]);
                ps.setDouble(5, (double) order[4]);
                ps.setString(6, (String) order[5]);

                try {
                    ps.executeUpdate();
                    System.out.println("Order " + order[0] + " inserted successfully.");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID: " + order[0]);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 2. Search by status and quantity > 2
    static void searchOrders(Connection con) {

        sc.nextLine();
        System.out.print("Enter status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? " +
                "AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                displayOrder(rs);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 3. Increase quantity
    static void increaseQuantity(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();

        System.out.print("Enter additional quantity: ");
        int qty = sc.nextInt();

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Quantity updated successfully!");
            else
                System.out.println("Order does not exist.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 4. Update valid status
    static void updateStatus(Connection con) {

        System.out.print("Enter Order ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new status (Pending/Shipped/Delivered): ");
        String status = sc.nextLine();

        if (!status.equalsIgnoreCase("Pending") &&
                !status.equalsIgnoreCase("Shipped") &&
                !status.equalsIgnoreCase("Delivered")) {

            System.out.println("Invalid status!");
            return;
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Status updated successfully!");

                String query = "SELECT * FROM Orders WHERE order_id = ?";
                PreparedStatement ps2 = con.prepareStatement(query);
                ps2.setInt(1, id);

                ResultSet rs = ps2.executeQuery();

                if (rs.next())
                    displayOrder(rs);

                ps2.close();

            } else {
                System.out.println("Order not found.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 5. Calculate total value and delete only cancelled orders
    static void totalAndDelete(Connection con) {

        String totalSql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(totalSql)) {

            if (rs.next())
                System.out.println("Total Order Value = " + rs.getDouble("total"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.print("Enter Order ID to delete: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Cancelled order deleted successfully!");
            else
                System.out.println("Order not found or it is not Cancelled.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Display order details
    static void displayOrder(ResultSet rs) throws SQLException {

        System.out.println("----------------------------");
        System.out.println("Order ID: " + rs.getInt("order_id"));
        System.out.println("Customer: " + rs.getString("customer_name"));
        System.out.println("Product: " + rs.getString("product_name"));
        System.out.println("Quantity: " + rs.getInt("quantity"));
        System.out.println("Unit Price: " + rs.getDouble("unit_price"));
        System.out.println("Status: " + rs.getString("status"));
    }
}
