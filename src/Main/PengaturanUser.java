/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

/**
 *
 * @author abil6
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import koneksi.DBConnection;

public class PengaturanUser extends javax.swing.JFrame {

    /**
     * Creates new form PengaturanUserForm
     */
     private DefaultTableModel model;
     private Map<String, Integer> siswaMap; // Nama Siswa -> ID Siswa
     private Map<Integer, Integer> userIdSiswaMap; // ID User -> ID Siswa (untuk mapping user ke siswa)
    
    public PengaturanUser() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        siswaMap = new HashMap<>();
        userIdSiswaMap = new HashMap<>();
        setupTable();
        loadUsersToTable();
        populateSiswaComboBox();
        clearFields();
        
         // Listener untuk JTable saat baris dipilih
        tblUsers.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && tblUsers.getSelectedRow() != -1) {
                    displaySelectedUser();
                }
            }
        });
        
        // Set echo char ke bullet
        txtPassword.setEchoChar('\u2022');
        

        // Listener untuk checkbox
        cbLihatPassword.addActionListener(evt -> {
            char echo = cbLihatPassword.isSelected() ? (char) 0 : '\u2022';
            txtPassword.setEchoChar(echo);
        });
        
    }
    
    private void setupTable() {
        model = new DefaultTableModel();
        tblUsers.setModel(model);
        model.addColumn("ID User");
        model.addColumn("Username");
        model.addColumn("Role");
        model.addColumn("Siswa Terkait"); // Akan menampilkan nama siswa jika role=siswa
    }
    
    private void loadUsersToTable() {
        model.setRowCount(0); // Bersihkan tabel
        userIdSiswaMap.clear(); // Bersihkan mapping siswa terkait
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT u.id_user, u.username, u.role, s.nama_siswa, s.id_siswa " +
                         "FROM users u LEFT JOIN siswa s ON u.id_user = s.id_user " + // LEFT JOIN untuk user yang tidak terkait siswa
                         "ORDER BY u.username";
            try (PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int idUser = rs.getInt("id_user");
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    String namaSiswa = rs.getString("nama_siswa"); // Bisa null
                    Integer idSiswa = rs.getObject("id_siswa") != null ? rs.getInt("id_siswa") : null;
                    
                    model.addRow(new Object[]{idUser, username, role, namaSiswa != null ? namaSiswa : ""});
                    
                    if (idSiswa != null) {
                        userIdSiswaMap.put(idUser, idSiswa);
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data user: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void populateSiswaComboBox() {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        siswaMap.clear();
        comboModel.addElement("-- Pilih Siswa (Jika Role Siswa) --");
        
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            // Hanya tampilkan siswa yang belum memiliki akun user
            String sql = "SELECT id_siswa, nama_siswa FROM siswa WHERE id_user IS NULL ORDER BY nama_siswa";
            try (PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int idSiswa = rs.getInt("id_siswa");
                    String namaSiswa = rs.getString("nama_siswa");
                    comboModel.addElement(namaSiswa);
                    siswaMap.put(namaSiswa, idSiswa);
                }
                cmbSiswaTerkait.setModel(comboModel);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data siswa untuk combo box: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        cmbRole.setSelectedIndex(0); // "admin"
        cmbSiswaTerkait.setSelectedIndex(0);
        cmbSiswaTerkait.setEnabled(false); // Default non-aktif
        tblUsers.clearSelection(); // Bersihkan seleksi tabel
        btnTambah.setEnabled(true);
        btnUbah.setEnabled(false);
        btnHapus.setEnabled(false);
        txtUsername.setEditable(true); // Username bisa diubah saat tambah
    }
    
    private void displaySelectedUser() {
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow != -1) {
            txtUsername.setEditable(false); // Username tidak bisa diubah saat edit
            btnTambah.setEnabled(false);
            btnUbah.setEnabled(true);
            btnHapus.setEnabled(true);

            String username = (String) model.getValueAt(selectedRow, 1);
            String role = (String) model.getValueAt(selectedRow, 2);
            String siswaTerkait = (String) model.getValueAt(selectedRow, 3); // Nama Siswa

            txtUsername.setText(username);
            txtPassword.setText(""); // Jangan tampilkan password demi keamanan
            cmbRole.setSelectedItem(role);
            
            if (role.equals("siswa")) {
                cmbSiswaTerkait.setEnabled(true);
                if (!siswaTerkait.isEmpty()) {
                    cmbSiswaTerkait.setSelectedItem(siswaTerkait);
                } else {
                    cmbSiswaTerkait.setSelectedIndex(0); // Jika tidak ada siswa terkait
                }
            } else {
                cmbSiswaTerkait.setSelectedIndex(0);
                cmbSiswaTerkait.setEnabled(false);
            }
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        cmbRole = new javax.swing.JComboBox<>();
        cmbSiswaTerkait = new javax.swing.JComboBox<>();
        btnTambah = new javax.swing.JButton();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnBersihkan = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        btnKembali = new javax.swing.JButton();
        cbLihatPassword = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("MANAJEMEN AKUN USER");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(322, 322, 322)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jLabel2.setText("username");

        jLabel3.setText("password");

        jLabel4.setText("role");

        jLabel5.setText("siswa terkait");

        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "siswa", "admin" }));
        cmbRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRoleActionPerformed(evt);
            }
        });

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnUbah.setText("Ubah");
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnBersihkan.setText("Bersihkan");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihkanActionPerformed(evt);
            }
        });

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblUsers);

        btnKembali.setText("Kembali");
        btnKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliActionPerformed(evt);
            }
        });

        cbLihatPassword.setText("tampilkan password");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGap(41, 41, 41)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtUsername)
                        .addComponent(txtPassword)
                        .addComponent(cmbSiswaTerkait, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnTambah, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUbah, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHapus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBersihkan, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(btnKembali)
                    .addComponent(cbLihatPassword))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(19, 19, 19)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(cmbSiswaTerkait, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbLihatPassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTambah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUbah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnHapus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBersihkan)
                        .addGap(28, 28, 28)
                        .addComponent(btnKembali)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRoleActionPerformed
        // TODO add your handling code here:
        if ("siswa".equals(cmbRole.getSelectedItem())) {
            cmbSiswaTerkait.setEnabled(true);
        } else {
            cmbSiswaTerkait.setSelectedIndex(0);
            cmbSiswaTerkait.setEnabled(false);
        }
    }//GEN-LAST:event_cmbRoleActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String role = (String) cmbRole.getSelectedItem();
        String selectedSiswa = (String) cmbSiswaTerkait.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (role.equals("siswa") && (selectedSiswa == null || selectedSiswa.equals("-- Pilih Siswa (Jika Role Siswa) --"))) {
            JOptionPane.showMessageDialog(this, "Untuk role 'siswa', harus pilih siswa terkait!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false); // Mulai transaksi

                // 1. Insert ke tabel users
                String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                PreparedStatement pstUser = conn.prepareStatement(insertUserSql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstUser.setString(1, username);
                pstUser.setString(2, password); // Di aplikasi nyata, gunakan hashing password!
                pstUser.setString(3, role);
                pstUser.executeUpdate();

                ResultSet rs = pstUser.getGeneratedKeys();
                int newUserId = -1;
                if (rs.next()) {
                    newUserId = rs.getInt(1);
                }
                rs.close();
                pstUser.close();

                // 2. Jika role siswa, update id_user di tabel siswa
                if (role.equals("siswa") && newUserId != -1) {
                    int idSiswa = siswaMap.get(selectedSiswa);
                    String updateSiswaSql = "UPDATE siswa SET id_user = ? WHERE id_siswa = ?";
                    PreparedStatement pstSiswa = conn.prepareStatement(updateSiswaSql);
                    pstSiswa.setInt(1, newUserId);
                    pstSiswa.setInt(2, idSiswa);
                    pstSiswa.executeUpdate();
                    pstSiswa.close();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadUsersToTable();
                populateSiswaComboBox(); // Refresh siswa yang tersedia
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Gagal menambahkan user: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
        // TODO add your handling code here:
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin diubah!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idUser = (int) model.getValueAt(selectedRow, 0);
        String username = txtUsername.getText(); // Tidak bisa diedit di UI, tapi diambil dari field
        String password = new String(txtPassword.getPassword()); // Jika kosong, tidak diubah
        String role = (String) cmbRole.getSelectedItem();
        String selectedSiswa = (String) cmbSiswaTerkait.getSelectedItem();
        Integer newIdSiswa = null;

        if (role.equals("siswa")) {
            if (selectedSiswa == null || selectedSiswa.equals("-- Pilih Siswa (Jika Role Siswa) --")) {
                JOptionPane.showMessageDialog(this, "Untuk role 'siswa', harus pilih siswa terkait!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            newIdSiswa = siswaMap.get(selectedSiswa);
        }

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false); // Mulai transaksi

                // Update tabel users
                StringBuilder updateUserSql = new StringBuilder("UPDATE users SET role = ?");
                if (!password.isEmpty()) {
                    updateUserSql.append(", password = ?"); // Tambahkan password jika diisi
                }
                updateUserSql.append(" WHERE id_user = ?");

                PreparedStatement pstUser = conn.prepareStatement(updateUserSql.toString());
                int paramIndex = 1;
                pstUser.setString(paramIndex++, role);
                if (!password.isEmpty()) {
                    pstUser.setString(paramIndex++, password); // Hashing di aplikasi nyata
                }
                pstUser.setInt(paramIndex++, idUser);
                pstUser.executeUpdate();
                pstUser.close();

                // Handle perubahan id_user di tabel siswa
                Integer oldIdSiswa = userIdSiswaMap.get(idUser); // Siswa terkait sebelumnya
                
                // Kasus 1: Role berubah dari siswa ke non-siswa, atau siswa yang terkait diubah
                if (oldIdSiswa != null && (role.equals("admin") || (newIdSiswa != null && !newIdSiswa.equals(oldIdSiswa)))) {
                    String updateOldSiswaSql = "UPDATE siswa SET id_user = NULL WHERE id_siswa = ?";
                    PreparedStatement pstUpdateOldSiswa = conn.prepareStatement(updateOldSiswaSql);
                    pstUpdateOldSiswa.setInt(1, oldIdSiswa);
                    pstUpdateOldSiswa.executeUpdate();
                    pstUpdateOldSiswa.close();
                }
                
                // Kasus 2: Role adalah siswa dan ada siswa baru yang dipilih atau siswa lama tetap
                if (role.equals("siswa") && newIdSiswa != null) {
                     String updateNewSiswaSql = "UPDATE siswa SET id_user = ? WHERE id_siswa = ?";
                     PreparedStatement pstUpdateNewSiswa = conn.prepareStatement(updateNewSiswaSql);
                     pstUpdateNewSiswa.setInt(1, idUser);
                     pstUpdateNewSiswa.setInt(2, newIdSiswa);
                     pstUpdateNewSiswa.executeUpdate();
                     pstUpdateNewSiswa.close();
                }


                conn.commit();
                JOptionPane.showMessageDialog(this, "User berhasil diubah!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadUsersToTable();
                populateSiswaComboBox(); // Refresh siswa yang tersedia
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Gagal mengubah user: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_btnUbahActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        int selectedRow = tblUsers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idUser = (int) model.getValueAt(selectedRow, 0);
        String username = (String) model.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus user '" + username + "'?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                try {
                    conn.setAutoCommit(false); // Mulai transaksi
                    
                    // Lepas kaitan id_user dari tabel siswa terlebih dahulu
                    String updateSiswaSql = "UPDATE siswa SET id_user = NULL WHERE id_user = ?";
                    PreparedStatement pstUpdateSiswa = conn.prepareStatement(updateSiswaSql);
                    pstUpdateSiswa.setInt(1, idUser);
                    pstUpdateSiswa.executeUpdate();
                    pstUpdateSiswa.close();

                    // Hapus user dari tabel users
                    String deleteUserSql = "DELETE FROM users WHERE id_user = ?";
                    PreparedStatement pstUser = conn.prepareStatement(deleteUserSql);
                    pstUser.setInt(1, idUser);
                    pstUser.executeUpdate();
                    pstUser.close();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "User berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    loadUsersToTable();
                    populateSiswaComboBox(); // Refresh siswa yang tersedia
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(this, "Gagal menghapus user: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        // TODO add your handling code here:
        clearFields();
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnKembaliActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PengaturanUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PengaturanUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PengaturanUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengaturanUser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PengaturanUser().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnKembali;
    private javax.swing.JButton btnTambah;
    private javax.swing.JButton btnUbah;
    private javax.swing.JCheckBox cbLihatPassword;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JComboBox<String> cmbSiswaTerkait;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblUsers;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
