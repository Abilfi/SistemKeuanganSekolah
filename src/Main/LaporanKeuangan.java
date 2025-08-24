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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import koneksi.DBConnection;

public class LaporanKeuangan extends javax.swing.JFrame {

    /**
     * Creates new form LaporanKeuanganForm
     */
    private DefaultTableModel modelRingkasanTagihan;
    private DefaultTableModel modelRiwayatPembayaran;
    
    public LaporanKeuangan() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setupTableRingkasanTagihan();
        setupTableRiwayatPembayaran();
        populateFilterComboBoxes();
        // Load laporan default (misal: semua data)
        loadLaporanKeuangan(null, null); 
    }
    
    private void setupTableRingkasanTagihan() {
        modelRingkasanTagihan = new DefaultTableModel();
        tblRingkasanTagihan.setModel(modelRingkasanTagihan);
        modelRingkasanTagihan.addColumn("Bulan");
        modelRingkasanTagihan.addColumn("Tahun");
        modelRingkasanTagihan.addColumn("NIS");
        modelRingkasanTagihan.addColumn("Nama Siswa");
        modelRingkasanTagihan.addColumn("Nominal Tagihan");
        modelRingkasanTagihan.addColumn("Status");
    }
    
    private void setupTableRiwayatPembayaran() {
        modelRiwayatPembayaran = new DefaultTableModel();
        tblRiwayatPembayaran.setModel(modelRiwayatPembayaran);
        modelRiwayatPembayaran.addColumn("ID Pembayaran");
        modelRiwayatPembayaran.addColumn("Nama Siswa");
        modelRiwayatPembayaran.addColumn("Tagihan (Bulan, Tahun)");
        modelRiwayatPembayaran.addColumn("Tanggal Bayar");
        modelRiwayatPembayaran.addColumn("Nominal Bayar");
    }
    
    private void populateFilterComboBoxes() {
        // Populate Bulan
        String[] months = {"-- Semua Bulan --", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        cmbBulanFilter.setModel(new DefaultComboBoxModel<>(months));

        // Populate Tahun (misal, 5 tahun terakhir dan 2 tahun ke depan)
        DefaultComboBoxModel<String> yearModel = new DefaultComboBoxModel<>();
        yearModel.addElement("-- Semua Tahun --");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear - 5; year <= currentYear + 2; year++) {
            yearModel.addElement(String.valueOf(year));
        }
        cmbTahunFilter.setModel(yearModel);
    }
    
    private void loadLaporanKeuangan(String bulan, String tahun) {
        modelRingkasanTagihan.setRowCount(0);
        modelRiwayatPembayaran.setRowCount(0);

        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            double totalPemasukan = 0;
            double totalTunggakan = 0;

            try {
                // Query untuk Ringkasan Tagihan
                StringBuilder sqlTagihan = new StringBuilder(
                    "SELECT t.bulan, t.tahun, s.nis, s.nama_siswa, t.nominal, t.status " +
                    "FROM tagihan t JOIN siswa s ON t.id_siswa = s.id_siswa "
                );
                List<String> conditionsTagihan = new ArrayList<>();
                if (bulan != null && !bulan.equals("-- Semua Bulan --")) {
                    conditionsTagihan.add("t.bulan = ?");
                }
                if (tahun != null && !tahun.equals("-- Semua Tahun --")) {
                    conditionsTagihan.add("t.tahun = ?");
                }
                if (!conditionsTagihan.isEmpty()) {
                    sqlTagihan.append(" WHERE ").append(String.join(" AND ", conditionsTagihan));
                }
                sqlTagihan.append(" ORDER BY t.tahun DESC, FIELD(t.bulan, 'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni', 'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember') DESC, s.nama_siswa");

                PreparedStatement pstTagihan = conn.prepareStatement(sqlTagihan.toString());
                int paramIndexTagihan = 1;
                if (bulan != null && !bulan.equals("-- Semua Bulan --")) {
                    pstTagihan.setString(paramIndexTagihan++, bulan);
                }
                if (tahun != null && !tahun.equals("-- Semua Tahun --")) {
                    pstTagihan.setInt(paramIndexTagihan++, Integer.parseInt(tahun));
                }

                ResultSet rsTagihan = pstTagihan.executeQuery();
                while (rsTagihan.next()) {
                    modelRingkasanTagihan.addRow(new Object[]{
                        rsTagihan.getString("bulan"),
                        rsTagihan.getInt("tahun"),
                        rsTagihan.getString("nis"),
                        rsTagihan.getString("nama_siswa"),
                        rsTagihan.getDouble("nominal"),
                        rsTagihan.getString("status")
                    });

                    if (rsTagihan.getString("status").equals("Belum Lunas")) {
                        totalTunggakan += rsTagihan.getDouble("nominal");
                    }
                }
                rsTagihan.close();
                pstTagihan.close();

                // Query untuk Riwayat Pembayaran (berdasarkan bulan/tahun TAGIHAN)
                StringBuilder sqlPembayaran = new StringBuilder(
                    "SELECT p.id_pembayaran, s.nama_siswa, CONCAT(t.bulan, ' ', t.tahun) AS deskripsi_tagihan, p.tanggal_bayar, p.nominal_bayar " +
                    "FROM pembayaran p " +
                    "JOIN siswa s ON p.id_siswa = s.id_siswa " +
                    "JOIN tagihan t ON p.id_tagihan = t.id_tagihan "
                );
                List<String> conditionsPembayaran = new ArrayList<>();
                if (bulan != null && !bulan.equals("-- Semua Bulan --")) {
                    conditionsPembayaran.add("t.bulan = ?");
                }
                if (tahun != null && !tahun.equals("-- Semua Tahun --")) {
                    conditionsPembayaran.add("t.tahun = ?");
                }

                if (!conditionsPembayaran.isEmpty()) {
                    sqlPembayaran.append(" WHERE ").append(String.join(" AND ", conditionsPembayaran));
                }
                sqlPembayaran.append(" ORDER BY p.tanggal_bayar DESC");

                PreparedStatement pstPembayaran = conn.prepareStatement(sqlPembayaran.toString());
                int paramIndexPembayaran = 1;
                if (bulan != null && !bulan.equals("-- Semua Bulan --")) {
                    pstPembayaran.setString(paramIndexPembayaran++, bulan);
                }
                if (tahun != null && !tahun.equals("-- Semua Tahun --")) {
                    pstPembayaran.setInt(paramIndexPembayaran++, Integer.parseInt(tahun));
                }

                ResultSet rsPembayaran = pstPembayaran.executeQuery();
                while (rsPembayaran.next()) {
                    modelRiwayatPembayaran.addRow(new Object[]{
                        rsPembayaran.getInt("id_pembayaran"),
                        rsPembayaran.getString("nama_siswa"),
                        rsPembayaran.getString("deskripsi_tagihan"),
                        rsPembayaran.getDate("tanggal_bayar"),
                        rsPembayaran.getDouble("nominal_bayar")
                    });
                    totalPemasukan += rsPembayaran.getDouble("nominal_bayar");
                }
                rsPembayaran.close();
                pstPembayaran.close();

                // Update label total
                lblTotalPemasukan.setText("Total Pemasukan (Pembayaran Lunas): Rp. " + String.format("%.2f", totalPemasukan));
                lblTotalTunggakan.setText("Total Tunggakan (Tagihan Belum Lunas): Rp. " + String.format("%.2f", totalTunggakan));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat laporan keuangan: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private int getMonthNumber(String monthName) {
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1; // Bulan dimulai dari 1 (Januari)
            }
        }
        return -1; // Invalid month
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
        cmbBulanFilter = new javax.swing.JComboBox<>();
        cmbTahunFilter = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        lblTotalPemasukan = new javax.swing.JLabel();
        lblTotalTunggakan = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRingkasanTagihan = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblRiwayatPembayaran = new javax.swing.JTable();
        btnKembali = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("LAPORAN KEUANGAN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(427, 427, 427)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        jLabel2.setText("filter bulan");

        jLabel3.setText("filter tahun");

        cmbBulanFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Januari", "Februari", "Maret", "April", "Mei", "Juni", " Juli", "Agustus", "September", "Oktober", "November", "Desember" }));

        cmbTahunFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2023", "2024", "2025" }));

        jButton1.setText("Tampilkan Laporan");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblTotalPemasukan.setText("jLabel1");

        lblTotalTunggakan.setText("jLabel4");

        jLabel4.setText("Pembayaran Lunas :");

        jLabel5.setText("Tagihan Belum Lunas :");

        tblRingkasanTagihan.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRingkasanTagihan);

        tblRiwayatPembayaran.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblRiwayatPembayaran);

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
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(33, 33, 33)
                                .addComponent(cmbBulanFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(32, 32, 32)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbTahunFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1))))
                        .addGap(61, 61, 61)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addGap(71, 71, 71)
                                    .addComponent(lblTotalTunggakan))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTotalPemasukan)))
                            .addComponent(btnKembali))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbBulanFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTotalPemasukan)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbTahunFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(lblTotalTunggakan))))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(btnKembali))
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String selectedBulan = (String) cmbBulanFilter.getSelectedItem();
        String selectedTahun = (String) cmbTahunFilter.getSelectedItem();
        loadLaporanKeuangan(selectedBulan, selectedTahun);
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(LaporanKeuangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LaporanKeuangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LaporanKeuangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LaporanKeuangan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LaporanKeuangan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKembali;
    private javax.swing.JComboBox<String> cmbBulanFilter;
    private javax.swing.JComboBox<String> cmbTahunFilter;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTotalPemasukan;
    private javax.swing.JLabel lblTotalTunggakan;
    private javax.swing.JTable tblRingkasanTagihan;
    private javax.swing.JTable tblRiwayatPembayaran;
    // End of variables declaration//GEN-END:variables
}
