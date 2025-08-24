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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import koneksi.DBConnection;     
import ui.AdminDashboard;

public class ManajemenPembayaran extends javax.swing.JFrame {

    /**
     * Creates new form ManajemenPembayaranForm
     */
    private DefaultTableModel model;
    private Map<String, Integer> siswaMap; // Nama siswa -> ID Siswa
    private Map<String, Integer> tagihanMap; // Deskripsi tagihan -> ID Tagihan
    private Map<Integer, Double> tagihanNominalMap; // ID Tagihan -> Nominal Tagihan

    
    public ManajemenPembayaran() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE); 
        siswaMap = new HashMap<>();
        tagihanMap = new HashMap<>();
        tagihanNominalMap = new HashMap<>();
        setupTable();
        loadSiswaToComboBox();
        loadDataPembayaran(); // Memuat semua pembayaran atau bisa difilter
        clearFields();
    }
    
    private void setupTable() {
        model = new DefaultTableModel();
        tblPembayaran.setModel(model);
        model.addColumn("ID Pembayaran");
        model.addColumn("Nama Siswa");
        model.addColumn("Deskripsi Tagihan"); // Misal: Juli 2024 (Rp. 150.000)
        model.addColumn("Tanggal Bayar");
        model.addColumn("Nominal Bayar");
        model.addColumn("Status Tagihan"); // Tampilkan status tagihan terkait
    }
    
    private void loadSiswaToComboBox() {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        comboModel.addElement("-- Pilih Siswa --"); // Placeholder
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT id_siswa, nama_siswa FROM siswa ORDER BY nama_siswa";
            try (PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int idSiswa = rs.getInt("id_siswa");
                    String namaSiswa = rs.getString("nama_siswa");
                    comboModel.addElement(namaSiswa);
                    siswaMap.put(namaSiswa, idSiswa);
                }
                cmbSiswa.setModel(comboModel);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data siswa untuk combo box: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void loadTagihanBySiswa(int idSiswa) {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        tagihanMap.clear(); // Bersihkan mapping sebelumnya
        tagihanNominalMap.clear();
        
        comboModel.addElement("-- Pilih Tagihan --");
        if (idSiswa == -1) { // Jika "Pilih Siswa" yang terpilih
            cmbTagihan.setModel(comboModel);
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            // Tampilkan tagihan aktif siswa (SELECT * FROM tagihan WHERE status='Belum Lunas').
            String sql = "SELECT id_tagihan, bulan, tahun, nominal FROM tagihan WHERE id_siswa = ? AND status = 'Belum Lunas' ORDER BY tahun, FIELD(bulan, 'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni', 'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember')";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, idSiswa);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    int idTagihan = rs.getInt("id_tagihan");
                    String bulan = rs.getString("bulan");
                    int tahun = rs.getInt("tahun");
                    double nominal = rs.getDouble("nominal");
                    String deskripsiTagihan = bulan + " " + tahun + " (Rp. " + String.format("%.2f", nominal) + ")";
                    comboModel.addElement(deskripsiTagihan);
                    tagihanMap.put(deskripsiTagihan, idTagihan);
                    tagihanNominalMap.put(idTagihan, nominal);
                }
                cmbTagihan.setModel(comboModel);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat tagihan siswa: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void loadDataPembayaran() {
        model.setRowCount(0); // Bersihkan tabel
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT p.id_pembayaran, s.nama_siswa, CONCAT(t.bulan, ' ', t.tahun, ' (Rp. ', t.nominal, ')') AS deskripsi_tagihan, p.tanggal_bayar, p.nominal_bayar, t.status AS status_tagihan " +
                         "FROM pembayaran p " +
                         "JOIN siswa s ON p.id_siswa = s.id_siswa " +
                         "JOIN tagihan t ON p.id_tagihan = t.id_tagihan " +
                         "ORDER BY p.tanggal_bayar DESC";
            try (PreparedStatement pst = conn.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id_pembayaran"),
                        rs.getString("nama_siswa"),
                        rs.getString("deskripsi_tagihan"),
                        rs.getDate("tanggal_bayar"),
                        rs.getDouble("nominal_bayar"),
                        rs.getString("status_tagihan")
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data pembayaran: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        cmbSiswa.setSelectedIndex(0); // Kembali ke placeholder
        cmbTagihan.removeAllItems(); // Kosongkan tagihan
        cmbTagihan.addItem("-- Pilih Tagihan --");
        txtNominalBayar.setText("");
        JTanggal.setDate(new Date()); // Tanggal hari ini
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbSiswa = new javax.swing.JComboBox<>();
        txtNominalBayar = new javax.swing.JTextField();
        JTanggal = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPembayaran = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cmbTagihan = new javax.swing.JComboBox<>();
        btnBersihkan = new javax.swing.JButton();
        btnKembali = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel9.setText("MANAJEMEN PEMBAYARAN");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(357, 357, 357)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addContainerGap())
        );

        jLabel1.setText("pilih siswa");

        jLabel2.setText("nominal bayar");

        jLabel3.setText("tangggal bayar");

        cmbSiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSiswaActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(tblPembayaran);

        jButton1.setText("Bayar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("pilih tagihan");

        cmbTagihan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTagihanActionPerformed(evt);
            }
        });

        btnBersihkan.setText("Bersihkan");
        btnBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihkanActionPerformed(evt);
            }
        });

        btnKembali.setText("Kembali");
        btnKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(cmbSiswa, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtNominalBayar)
                        .addComponent(JTanggal, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addComponent(cmbTagihan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnBersihkan)
                    .addComponent(btnKembali))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 706, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cmbSiswa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(cmbTagihan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(JTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBersihkan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnKembali))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbSiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSiswaActionPerformed
        // TODO add your handling code here:
        String selectedSiswa = (String) cmbSiswa.getSelectedItem();
        if (selectedSiswa != null && !selectedSiswa.equals("-- Pilih Siswa --")) {
            int idSiswa = siswaMap.get(selectedSiswa);
            loadTagihanBySiswa(idSiswa);
        } else {
            cmbTagihan.removeAllItems();
            cmbTagihan.addItem("-- Pilih Tagihan --");
        }
        txtNominalBayar.setText(""); // Bersihkan nominal bayar saat siswa berubah  
    }//GEN-LAST:event_cmbSiswaActionPerformed

    private void cmbTagihanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTagihanActionPerformed
        // TODO add your handling code here:
        String selectedTagihan = (String) cmbTagihan.getSelectedItem();
        if (selectedTagihan != null && !selectedTagihan.equals("-- Pilih Tagihan --")) {
            int idTagihan = tagihanMap.get(selectedTagihan);
            double nominalTagihan = tagihanNominalMap.get(idTagihan);
            txtNominalBayar.setText(String.format("%.2f", nominalTagihan)); // Otomatis isi nominal
        } else {
            txtNominalBayar.setText("");
        }
    }//GEN-LAST:event_cmbTagihanActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String selectedSiswa = (String) cmbSiswa.getSelectedItem();
        String selectedTagihanDesk = (String) cmbTagihan.getSelectedItem();
        String nominalBayarStr = txtNominalBayar.getText();
        Date tanggalBayar = JTanggal.getDate(); // JDateChooser
        if (tanggalBayar == null) {
            JOptionPane.showMessageDialog(this, "Tanggal pembayaran harus dipilih!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tanggalBayarStr = new SimpleDateFormat("yyyy-MM-dd").format(tanggalBayar);

        if (selectedSiswa.equals("-- Pilih Siswa --") || selectedTagihanDesk.equals("-- Pilih Tagihan --") || nominalBayarStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedSiswa.equals("-- Pilih Siswa --") || selectedTagihanDesk.equals("-- Pilih Tagihan --") || nominalBayarStr.isEmpty() || tanggalBayarStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double nominalBayar;
        try {
            nominalBayar = Double.parseDouble(nominalBayarStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nominal bayar harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idSiswa = siswaMap.get(selectedSiswa);
        int idTagihan = tagihanMap.get(selectedTagihanDesk);
        double nominalTagihan = tagihanNominalMap.get(idTagihan);

        if (nominalBayar < nominalTagihan) {
            JOptionPane.showMessageDialog(this, "Nominal pembayaran tidak boleh kurang dari nominal tagihan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (nominalBayar > nominalTagihan) {
             JOptionPane.showMessageDialog(this, "Nominal pembayaran lebih dari nominal tagihan. Sistem akan mencatat sesuai nominal tagihan.", "Info", JOptionPane.INFORMATION_MESSAGE);
             nominalBayar = nominalTagihan; // Sesuaikan nominal bayar dengan nominal tagihan
        }

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false); // Mulai transaksi

                // 1. Sistem simpan data pembayaran (INSERT INTO pembayaran) [cite: 28]
                String insertPembayaranSql = "INSERT INTO pembayaran (id_tagihan, id_siswa, tanggal_bayar, nominal_bayar) VALUES (?, ?, ?, ?)";
                PreparedStatement pstPembayaran = conn.prepareStatement(insertPembayaranSql);
                pstPembayaran.setInt(1, idTagihan);
                pstPembayaran.setInt(2, idSiswa);
                pstPembayaran.setString(3, tanggalBayarStr);
                pstPembayaran.setDouble(4, nominalBayar);
                pstPembayaran.executeUpdate();
                pstPembayaran.close();

                // 2. Update status tagihan (UPDATE tagihan SET status='Lunas' WHERE id_tagihan=?) [cite: 29]
                String updateTagihanSql = "UPDATE tagihan SET status = 'Lunas' WHERE id_tagihan = ?";
                PreparedStatement pstTagihan = conn.prepareStatement(updateTagihanSql);
                pstTagihan.setInt(1, idTagihan);
                pstTagihan.executeUpdate();
                pstTagihan.close();

                conn.commit(); // Commit transaksi
                JOptionPane.showMessageDialog(this, "Pembayaran berhasil dicatat dan status tagihan diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadDataPembayaran(); // Perbarui riwayat pembayaran
                loadTagihanBySiswa(idSiswa); // Perbarui daftar tagihan aktif siswa ini
            } catch (SQLException e) {
                try {
                    conn.rollback(); // Rollback jika ada error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Gagal mencatat pembayaran: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                try {
                    conn.setAutoCommit(true); // Kembalikan auto-commit ke true
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanActionPerformed
        // TODO add your handling code here:
        clearFields();
    }//GEN-LAST:event_btnBersihkanActionPerformed

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        // TODO add your handling code here
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
            java.util.logging.Logger.getLogger(ManajemenPembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManajemenPembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManajemenPembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManajemenPembayaran.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManajemenPembayaran().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser JTanggal;
    private javax.swing.JButton btnBersihkan;
    private javax.swing.JButton btnKembali;
    private javax.swing.JComboBox<String> cmbSiswa;
    private javax.swing.JComboBox<String> cmbTagihan;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPembayaran;
    private javax.swing.JTextField txtNominalBayar;
    // End of variables declaration//GEN-END:variables
}
