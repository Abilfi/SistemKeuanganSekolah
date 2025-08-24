// File: src/aplikasikeuangansekolah/DBConnection.java
package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/db_keuangan_sekolah";
    private static final String USER = "root"; // Ganti dengan username MySQL Anda
    private static final String PASSWORD = ""; // Ganti dengan password MySQL Anda

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Open a connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi database berhasil!");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "JDBC Driver tidak ditemukan: " + e.getMessage(), "Kesalahan Koneksi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Koneksi database gagal: " + e.getMessage(), "Kesalahan Koneksi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Koneksi database ditutup.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Gagal menutup koneksi database: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}