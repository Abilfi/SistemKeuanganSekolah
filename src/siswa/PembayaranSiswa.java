/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package siswa;

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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import koneksi.DBConnection;

public class PembayaranSiswa extends javax.swing.JFrame {

    /**
     * Creates new form PembayaranSiswa
     */
    private int idSiswaLoggedIn;
    private DefaultTableModel modelPembayaran; // Untuk riwayat pembayaran siswa
    private Map<String, Integer> tagihanMap; // Deskripsi tagihan -> ID Tagihan
    private Map<Integer, Double> tagihanNominalMap; // ID Tagihan -> Nominal Tagihan
    
    public PembayaranSiswa(int idSiswa) {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.idSiswaLoggedIn = idSiswa;
        tagihanMap = new HashMap<>();
        tagihanNominalMap = new HashMap<>();
        setupTablePembayaran();
        loadTagihanSiswaUntukPembayaran();
        loadRiwayatPembayaranSiswa();
        clearFields();
    }
    
    private void setupTablePembayaran() {
        modelPembayaran = new DefaultTableModel();
        tblRiwayatPembayaranSiswa.setModel(modelPembayaran);
        modelPembayaran.addColumn("ID Pembayaran");
        modelPembayaran.addColumn("Deskripsi Tagihan");
        modelPembayaran.addColumn("Tanggal Bayar");
        modelPembayaran.addColumn("Nominal Bayar");
        modelPembayaran.addColumn("Status Tagihan");
    }
    
    private void loadTagihanSiswaUntukPembayaran() {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        tagihanMap.clear();
        tagihanNominalMap.clear();
        
        comboModel.addElement("-- Pilih Tagihan untuk Dibayar --");

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT id_tagihan, bulan, tahun, nominal FROM tagihan WHERE id_siswa = ? AND status = 'Belum Lunas' ORDER BY tahun, FIELD(bulan, 'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni', 'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember')";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, idSiswaLoggedIn);
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
                cmbTagihanSiswa.setModel(comboModel);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat tagihan siswa: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void loadRiwayatPembayaranSiswa() {
        modelPembayaran.setRowCount(0); // Bersihkan tabel
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT p.id_pembayaran, CONCAT(t.bulan, ' ', t.tahun, ' (Rp. ', t.nominal, ')') AS deskripsi_tagihan, p.tanggal_bayar, p.nominal_bayar, t.status AS status_tagihan " +
                         "FROM pembayaran p " +
                         "JOIN tagihan t ON p.id_tagihan = t.id_tagihan " +
                         "WHERE p.id_siswa = ? ORDER BY p.tanggal_bayar DESC";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, idSiswaLoggedIn);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    modelPembayaran.addRow(new Object[]{
                        rs.getInt("id_pembayaran"),
                        rs.getString("deskripsi_tagihan"),
                        rs.getDate("tanggal_bayar"),
                        rs.getDouble("nominal_bayar"),
                        rs.getString("status_tagihan")
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat riwayat pembayaran: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void clearFields() {
        if (cmbTagihanSiswa.getItemCount() > 0) {
            cmbTagihanSiswa.setSelectedIndex(0);
        }
        txtNominalBayarSiswa.setText("");
        jTanggalBayarSiswa.setDate(new Date()); // Tanggal hari ini
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
        txtNominalBayarSiswa = new javax.swing.JTextField();
        cmbTagihanSiswa = new javax.swing.JComboBox<>();
        jTanggalBayarSiswa = new com.toedter.calendar.JDateChooser();
        btnBayarSiswa = new javax.swing.JButton();
        btnBersihkanSiswa = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRiwayatPembayaranSiswa = new javax.swing.JTable();
        btnKembali = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("PEMBAYARAN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(393, 393, 393)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("tagihan siswa");

        jLabel3.setText("nominal bayar");

        jLabel4.setText("tanggal bayar");

        txtNominalBayarSiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNominalBayarSiswaActionPerformed(evt);
            }
        });

        cmbTagihanSiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTagihanSiswaActionPerformed(evt);
            }
        });

        btnBayarSiswa.setText("Bayar");
        btnBayarSiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarSiswaActionPerformed(evt);
            }
        });

        btnBersihkanSiswa.setText("Bersihkan");
        btnBersihkanSiswa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihkanSiswaActionPerformed(evt);
            }
        });

        tblRiwayatPembayaranSiswa.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRiwayatPembayaranSiswa);

        btnKembali.setText("Kembali");
        btnKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKembaliActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBersihkanSiswa)
                    .addComponent(btnBayarSiswa)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtNominalBayarSiswa)
                        .addComponent(cmbTagihanSiswa, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTanggalBayarSiswa, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                    .addComponent(btnKembali))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 635, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbTagihanSiswa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtNominalBayarSiswa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jTanggalBayarSiswa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnBayarSiswa)
                        .addGap(18, 18, 18)
                        .addComponent(btnBersihkanSiswa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnKembali)))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void txtNominalBayarSiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNominalBayarSiswaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNominalBayarSiswaActionPerformed

    private void cmbTagihanSiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTagihanSiswaActionPerformed
        String selectedTagihan = (String) cmbTagihanSiswa.getSelectedItem();
        if (selectedTagihan != null && !selectedTagihan.equals("-- Pilih Tagihan untuk Dibayar --")) {
            int idTagihan = tagihanMap.get(selectedTagihan);
            double nominalTagihan = tagihanNominalMap.get(idTagihan);
            txtNominalBayarSiswa.setText(String.format("%.2f", nominalTagihan)); // Otomatis isi nominal
        } else {
            txtNominalBayarSiswa.setText("");
        }
    }//GEN-LAST:event_cmbTagihanSiswaActionPerformed

    private void btnBayarSiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarSiswaActionPerformed
        // TODO add your handling code here:
        String selectedTagihanDesk = (String) cmbTagihanSiswa.getSelectedItem();
        String nominalBayarStr = txtNominalBayarSiswa.getText();
        Date tanggalBayar = jTanggalBayarSiswa.getDate(); // JDateChooser
        if (tanggalBayar == null) {
            JOptionPane.showMessageDialog(this, "Tanggal pembayaran harus dipilih!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tanggalBayarStr = new SimpleDateFormat("yyyy-MM-dd").format(tanggalBayar);

        if (selectedTagihanDesk.equals("-- Pilih Tagihan untuk Dibayar --") || nominalBayarStr.isEmpty() || tanggalBayarStr.isEmpty()) {
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

                // 1. Sistem simpan data pembayaran (INSERT INTO pembayaran)
                String insertPembayaranSql = "INSERT INTO pembayaran (id_tagihan, id_siswa, tanggal_bayar, nominal_bayar) VALUES (?, ?, ?, ?)";
                PreparedStatement pstPembayaran = conn.prepareStatement(insertPembayaranSql);
                pstPembayaran.setInt(1, idTagihan);
                pstPembayaran.setInt(2, idSiswaLoggedIn);
                pstPembayaran.setString(3, tanggalBayarStr);
                pstPembayaran.setDouble(4, nominalBayar);
                pstPembayaran.executeUpdate();
                pstPembayaran.close();

                // 2. Update status tagihan (UPDATE tagihan SET status='Lunas' WHERE id_tagihan=?)
                String updateTagihanSql = "UPDATE tagihan SET status = 'Lunas' WHERE id_tagihan = ?";
                PreparedStatement pstTagihan = conn.prepareStatement(updateTagihanSql);
                pstTagihan.setInt(1, idTagihan);
                pstTagihan.executeUpdate();
                pstTagihan.close();

                conn.commit(); // Commit transaksi
                JOptionPane.showMessageDialog(this, "Pembayaran berhasil dicatat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadTagihanSiswaUntukPembayaran(); // Perbarui daftar tagihan aktif siswa ini
                loadRiwayatPembayaranSiswa(); // Perbarui riwayat pembayaran siswa
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
    }//GEN-LAST:event_btnBayarSiswaActionPerformed

    private void btnBersihkanSiswaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihkanSiswaActionPerformed
        // TODO add your handling code here:
        clearFields();
    }//GEN-LAST:event_btnBersihkanSiswaActionPerformed

    private void btnKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKembaliActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_btnKembaliActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(PembayaranSiswa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(PembayaranSiswa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(PembayaranSiswa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(PembayaranSiswa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new PembayaranSiswa().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBayarSiswa;
    private javax.swing.JButton btnBersihkanSiswa;
    private javax.swing.JButton btnKembali;
    private javax.swing.JComboBox<String> cmbTagihanSiswa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jTanggalBayarSiswa;
    private javax.swing.JTable tblRiwayatPembayaranSiswa;
    private javax.swing.JTextField txtNominalBayarSiswa;
    // End of variables declaration//GEN-END:variables
}
