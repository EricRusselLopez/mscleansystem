package src.main.java.components;

import src.main.java.App;
import src.main.java.auth.Authenticator;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.json.JSONObject;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import src.main.java.auth.session.java.FetchUser;
import src.main.java.components.helper.GenerateReports;
import src.main.java.components.helper.Inventory;
import src.main.java.components.helper.generateReports;
import src.utils.CustomBorder;
import src.utils.CustomHeaderRenderer;
import src.utils.CustomImageResizer;
import src.utils.RowRenderer;
import src.utils.RowRendererInventory;
import src.utils.RowRendererReports;

public class Employee extends JPanel implements CustomBorder {

    JSONObject user = FetchUser.getUserData();
    Authenticator authenticator = new Authenticator();
    static TableRowSorter<DefaultTableModel> sorter;

    private App app;

    public Employee(App app) {

        this.app = app;

        if(authenticator.isAuthenticated() == null) {
            authenticator.logout();
            app.showLogin();
        }


        initComponents();
    }


    private void initComponents() {                          

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        logoutIconNav = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jButton2= new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel2.setMinimumSize(new java.awt.Dimension(1100, 0));
        jPanel2.setPreferredSize(new java.awt.Dimension(1100, 50));

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        ImageIcon labelLogo= new CustomImageResizer().resizeIcon(new ImageIcon("./src/assets/Laundry-removebg-preview.png"), 45, 50);
        jLabel3.setIcon(labelLogo);  


        logoutIconNav.setForeground(new java.awt.Color(0, 0, 0));
        logoutIconNav.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ImageIcon labelIconLogout = new CustomImageResizer().resizeIcon(new ImageIcon("./src/icons/arrow-down.png"), 25, 25);
        logoutIconNav.setIcon(labelIconLogout);
        logoutIconNav.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10)); 

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(new Font(null, Font.BOLD, 15));
        ImageIcon labelIconLogoutMain = new CustomImageResizer().resizeIcon(new ImageIcon("./src/icons/logout.png"), 15, 15);
        logoutItem.setIcon(labelIconLogoutMain); 
        logoutItem.addActionListener(e -> {
            int confirmLogout = JOptionPane.showConfirmDialog(this, "Are you sure you want to Logout now?", "Logout", JOptionPane.YES_NO_OPTION);
            if(confirmLogout == JOptionPane.YES_OPTION) {
                authenticator.logout();
                app.showLogin();
            }
        });
        popupMenu.add(logoutItem);

        logoutIconNav.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });


        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1015, Short.MAX_VALUE)
                .addComponent(logoutIconNav))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addGap(18, 18, 18)
                    .addComponent(logoutIconNav))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 50));

        jPanel1.setBackground(new java.awt.Color(234, 234, 234));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, new java.awt.Color(153, 153, 153)));
        jPanel1.setPreferredSize(new java.awt.Dimension(80, 650));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        jLabel1.setText(user.getString("firstname") + " " + user.getString("lastname"));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 12));
        jLabel4.setForeground(new java.awt.Color(51, 51, 51));
        jLabel4.setText(user.getString("branch"));


        jPanel3.setBackground(new java.awt.Color(234, 234, 234));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addContainerGap(19, Short.MAX_VALUE))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(496, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 180, 600));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable5.setBackground(new java.awt.Color(255, 255, 255));
        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Transaction ID", "Date & Time", "Customer", "Servive Type", "₱ Total Amount", "Branch", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sorter = new TableRowSorter<>((DefaultTableModel) jTable5.getModel());
        jTable5.setRowSorter(sorter);
        jTable5.setRowHeight(30);
        jTable5.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable5.getTableHeader().setResizingAllowed(false);
        jTable5.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(jTable5);
        if (jTable5.getColumnModel().getColumnCount() > 0) {
            jTable5.getColumnModel().getColumn(0).setResizable(false);
            jTable5.getColumnModel().getColumn(1).setResizable(false);
            jTable5.getColumnModel().getColumn(2).setResizable(false);
            jTable5.getColumnModel().getColumn(3).setResizable(false);
            jTable5.getColumnModel().getColumn(4).setResizable(false);
            jTable5.getColumnModel().getColumn(5).setResizable(false);
            jTable5.getColumnModel().getColumn(6).setResizable(false);

            jTable5.getColumnModel().getColumn(0).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/issue.png"));
            jTable5.getColumnModel().getColumn(1).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/calendar.png"));
            jTable5.getColumnModel().getColumn(2).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/user.png"));
            jTable5.getColumnModel().getColumn(3).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/customer-service.png"));
            jTable5.getColumnModel().getColumn(4).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/gross.png"));
            jTable5.getColumnModel().getColumn(5).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/git.png"));
            jTable5.getColumnModel().getColumn(6).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/status.png"));
        }
        jButton1.addActionListener(e -> new GenerateReports().newReport((DefaultTableModel) jTable5.getModel()));
        jButton2.addActionListener(e -> new GenerateReports().updateReport(jTable5, (DefaultTableModel) jTable5.getModel()));

        DefaultTableModel modelReports = (DefaultTableModel) jTable5.getModel();
        GenerateReports generateReports = new GenerateReports();

        generateReports.loadReportsRequests(modelReports);

        int delayMs = 2000;
        
        new Timer(delayMs, e -> generateReports.loadReportsRequests(modelReports))
            .start();

        jTable5.setDefaultRenderer(Object.class, new RowRendererReports());
        


        jPanel9.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 920, 420));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 3, 24));
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Reports");
        jPanel9.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 6, -1, -1));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(102, 102, 102)));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );

        jPanel9.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 10));

        jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox1.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jComboBox1.setForeground(new java.awt.Color(51, 51, 51));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"Status", "Pending", "Canceled", "Pickup", "Delivered", "Paid" }));

        TableRowSorter<DefaultTableModel> sorterEmployee = new TableRowSorter<>((DefaultTableModel) jTable5.getModel());
        jTable5.setRowSorter(sorterEmployee);

        final String[] selectedStatus = {"status"};
        final String[] enteredID = {""};

        jComboBox1.addActionListener(e -> {
            selectedStatus[0] = jComboBox1.getSelectedItem().toString().trim().toLowerCase();
            applyReportsFilter(sorterEmployee, selectedStatus[0], enteredID[0]);
        });
        
        Timer typingTimer = new Timer(500, null);
        typingTimer.setRepeats(false);

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                typingTimer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        typingTimer.addActionListener(e -> {
            enteredID[0] = jTextField1.getText().trim().toLowerCase();
            applyReportsFilter(sorterEmployee, selectedStatus[0], enteredID[0]);

            if (enteredID[0].isEmpty()) {
                setInputBorderColor(jTextField1, "🔎 Search by ID", Color.BLACK);
                jComboBox1.setSelectedIndex(0);
                sorterEmployee.setRowFilter(null);
                return;
            }

            if (jTable5.getRowCount() > 0) {
                setInputBorderColor(jTextField1, "🔎 Search by ID", Color.BLACK);
            } else {
                setInputBorderColor(jTextField1, "❌ No report found", Color.RED);
            }
        });



        jPanel9.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 70, -1, 30));

        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jTextField1.setForeground(new java.awt.Color(51, 51, 51));

        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jTextField1.setForeground(new java.awt.Color(51, 51, 51));
        setInputBorderColor(jTextField1, "🔎 Search by ID", Color.BLACK);


        jPanel9.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 180, 40));

        jButton1.setText("New Report");
        jButton1.setBackground(Color.CYAN);
        jPanel9.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 70, -1, 30));
        jButton2.setText("Update Report");
        jPanel9.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 70, -1, 30));

        jTabbedPane1.addTab("Reports", jPanel9);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable4.setBackground(new java.awt.Color(255, 255, 255));
        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "Item Name", "Quantity", "Theshold", "Branch", "Status", "Last Restocked"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable4.getTableHeader().setResizingAllowed(false);
        jTable4.getTableHeader().setReorderingAllowed(false);
        jTable4.setRowHeight(30);
        jTable4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(jTable4);
                if (jTable4.getColumnModel().getColumnCount() > 0) {
            jTable4.getColumnModel().getColumn(0).setResizable(false);
            jTable4.getColumnModel().getColumn(1).setResizable(false);
            jTable4.getColumnModel().getColumn(2).setResizable(false);
            jTable4.getColumnModel().getColumn(3).setResizable(false);
            jTable4.getColumnModel().getColumn(4).setResizable(false);
            jTable4.getColumnModel().getColumn(5).setResizable(false);

            jTable4.getColumnModel().getColumn(0).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/box.png"));
            jTable4.getColumnModel().getColumn(1).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/bulk.png"));
            jTable4.getColumnModel().getColumn(2).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/type.png"));
            jTable4.getColumnModel().getColumn(3).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/git.png"));
            jTable4.getColumnModel().getColumn(4).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/status.png"));
            jTable4.getColumnModel().getColumn(5).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/calendar.png"));
        }
        jTable4.setDefaultRenderer(Object.class, new RowRendererInventory());
        DefaultTableModel modelInventory = (DefaultTableModel) jTable4.getModel();
        Inventory inventory = new Inventory();
        inventory.loadInventoryRequests(modelInventory);

        new Timer(2000, e -> inventory.loadInventoryRequests(modelInventory))
            .start();

        jPanel8.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 102, 920, 460));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 3, 24));
        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Inventory");
        jPanel8.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 6, -1, -1));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(102, 102, 102)));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 10));

        jButton4.setText("Use Item");
        jButton4.addActionListener(e -> inventory.useInventory(jTable4, modelInventory));
        jPanel8.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, 30));


        jTabbedPane1.addTab("Inventory", jPanel8);

        add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 50, 920, 610));
    }      



    private void applyReportsFilter(TableRowSorter<DefaultTableModel> sorter, String statusFilter, String idFilter) {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        boolean hasStatus = statusFilter != null && !statusFilter.equals("status");
        boolean hasID = idFilter != null && !idFilter.isEmpty();

        if (hasStatus) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(statusFilter), 6));
        }
        if (hasID) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(idFilter), 0));
        }

        if (!hasStatus && !hasID) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
            if (jTable5.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No report found", "No data", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel logoutIconNav;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTextField jTextField1;
}
