package kostiskag.unitynetwork.tracker.GUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.mysql.jdbc.UpdatableResultSet;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.database.Logic;
import kostiskag.unitynetwork.tracker.database.Logic.*;
import kostiskag.unitynetwork.tracker.runData.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.TitledBorder;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author kostis
 */
public class MainWindow extends javax.swing.JFrame {

	/**
	 * Creates new form MainWindow
	 */
	public static boolean lockDbEdit = false;
	
	private static DefaultTableModel bluenodes;
	private static DefaultTableModel rednodes;

	private DefaultTableModel modelBluenodesDb;
	private DefaultTableModel modelUsersDb;
	private DefaultTableModel modelHostnamesDb;
	
	private String[] bluenodesTableHead = new String[] { "Hostname", "Physical Address", "Auth Port", "RedNode Load", "Timestamp" };
	private String[] rednodesTableHead = new String[] { "Hostname", "Virtual Address", "BlueNode Hostname", "Timestamp" };
	
	private String[] usersDbHead = new String[] { "id", "username", "password", "type", "fullname" };
	private String[] hostnamesDbHead = new String[] { "address", "hostname", "userid" };
	private String[] blunodesDbHead = new String[] { "name", "userid" };

	public MainWindow() {
		bluenodes = new DefaultTableModel(new String[][] {}, bluenodesTableHead);
		rednodes = new DefaultTableModel(new String[][] {}, rednodesTableHead);

		modelUsersDb = new DefaultTableModel(new String[][] {}, usersDbHead);
		modelHostnamesDb = new DefaultTableModel(new String[][] {}, hostnamesDbHead);
		modelBluenodesDb = new DefaultTableModel(new String[][] {}, blunodesDbHead);

		initComponents();
		setTitle("UnityNetwork Tracker");

		table.setModel(modelUsersDb);
		table_1.setModel(modelHostnamesDb);
		table_2.setModel(modelBluenodesDb);

		table.setDefaultEditor(Object.class, null);
		table_1.setDefaultEditor(Object.class, null);
		table_2.setDefaultEditor(Object.class, null);

		updateDatabaseGUI();
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				String ObjButtons[] = { "Yes", "No" };
				int PromptResult = JOptionPane.showOptionDialog(null,
						"Are you sure you wish to terminate the Tracker?\nThis may result in the overall network termination.\nIf you decide to close the Tracker, it will send the appropriate kill signals to the connected BlueNodes.",
						"", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, ObjButtons, ObjButtons[1]);
				if (PromptResult == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 1810, Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE).addContainerGap()));

		panel = new JPanel();
		tabbedPane.addTab("View Active Network", null, panel, null);
		panel.setLayout(null);

		BlueNodes = new javax.swing.JPanel();
		BlueNodes.setBounds(0, 5, 599, 728);
		panel.add(BlueNodes);
		jPanel1 = new javax.swing.JPanel();
		jPanel1.setBounds(0, 0, 599, 686);
		jScrollPane2 = new javax.swing.JScrollPane();
		jScrollPane2.setBounds(6, 27, 583, 648);
		jTable2 = new javax.swing.JTable();
		jTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPanel4 = new javax.swing.JPanel();
		jPanel4.setBounds(10, 697, 589, 31);
		jButton1 = new javax.swing.JButton();
		jButton1.setToolTipText("Refresh makes the GUI table to catch up with the internal data");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BlueNodes"));

		jTable2.setModel(bluenodes);
		jScrollPane2.setViewportView(jTable2);

		jButton1.setText("Table Refresh");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		BlueNodes.setLayout(null);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4Layout.setHorizontalGroup(
			jPanel4Layout.createParallelGroup(Alignment.TRAILING)
				.addGroup(jPanel4Layout.createSequentialGroup()
					.addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
					.addGap(577))
		);
		jPanel4Layout.setVerticalGroup(
			jPanel4Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup()
					.addComponent(jButton1)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		jPanel4.setLayout(jPanel4Layout);
		BlueNodes.add(jPanel4);
		BlueNodes.add(jPanel1);
		jPanel1.setLayout(null);
		jPanel1.add(jScrollPane2);
		RedNodes = new javax.swing.JPanel();
		RedNodes.setBounds(609, 5, 556, 728);
		panel.add(RedNodes);
		jPanel2 = new javax.swing.JPanel();
		jPanel2.setBounds(0, 0, 556, 686);
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPanel5 = new javax.swing.JPanel();
		jPanel5.setBounds(10, 697, 546, 31);
		jButton2 = new javax.swing.JButton();
		jButton2.setToolTipText("Refresh makes the GUI table to catch up with the internal data");

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("RedNodes"));

		jTable1.setModel(rednodes);
		jScrollPane1.setViewportView(jTable1);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING).addComponent(jScrollPane1,
				GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE));
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap().addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)));
		jPanel2.setLayout(jPanel2Layout);

		jButton2.setText("Table Refresh");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addComponent(jButton2,
						javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0,
								290, Short.MAX_VALUE)));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addComponent(jButton2).addGap(0, 40, Short.MAX_VALUE)));
		RedNodes.setLayout(null);
		RedNodes.add(jPanel2);
		RedNodes.add(jPanel5);
		Console = new javax.swing.JPanel();
		Console.setBounds(1175, 5, 620, 728);
		panel.add(Console);
		jPanel3 = new javax.swing.JPanel();
		jPanel3.setBounds(0, 0, 620, 686);
		jScrollPane3 = new javax.swing.JScrollPane();
		jTextArea1 = new javax.swing.JTextArea();
		jPanel6 = new javax.swing.JPanel();
		jPanel6.setBounds(10, 697, 610, 31);
		jButton4 = new javax.swing.JButton();
		jCheckBox1 = new javax.swing.JCheckBox();

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Console"));

		jTextArea1.setColumns(20);
		jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
		jTextArea1.setRows(5);
		jScrollPane3.setViewportView(jTextArea1);

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane3)));

		jButton4.setText("Clean");
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		jCheckBox1.setSelected(true);
		jCheckBox1.setText("KeepScrolledDown");
		jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBox1ActionPerformed(evt);
			}
		});
		Console.setLayout(null);

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup()
						.addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 128,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18).addComponent(jCheckBox1).addContainerGap(361, Short.MAX_VALUE)));
		jPanel6Layout
				.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel6Layout.createSequentialGroup()
								.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButton4).addComponent(jCheckBox1))
								.addGap(0, 40, Short.MAX_VALUE)));
		Console.add(jPanel6);
		Console.add(jPanel3);

		panel_1 = new JPanel();
		tabbedPane.addTab("Manage Network Data", null, panel_1, null);
		panel_1.setLayout(null);

		panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Table users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(10, 11, 670, 711);
		panel_1.add(panel_2);
		panel_2.setLayout(null);

		JPanel panel_3 = new JPanel();
		panel_3.setBounds(10, 655, 650, 45);
		panel_2.add(panel_3);
		panel_3.setLayout(null);

		JButton btnNewButton = new JButton("Add new entry");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new user
				if (!lockDbEdit) {
					// lockDbEdit = true;
					new EditUser(0, "");
				}
			}
		});
		btnNewButton.setBounds(10, 11, 116, 23);
		panel_3.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Edit selected entry");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// update user
				if (!lockDbEdit) {
					// lockDbEdit = true;
					int row = table.getSelectedRow();
					if (row != -1) {
						new EditUser(1, (String) table.getValueAt(row, 1));
					}
				}
			}
		});
		btnNewButton_1.setBounds(136, 11, 133, 23);
		panel_3.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Delete selected entry");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// delete user
				if (!lockDbEdit) {
					int row = table.getSelectedRow();
					if (row != -1) {
						// lockDbEdit = true;
						String username = (String) table.getValueAt(row, 1);
						try {
							Logic.removeUserAndAllHisItems(username);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						updateDatabaseGUI();
						// lockDbEdit = false;
					}
				}
			}
		});
		btnNewButton_2.setBounds(279, 11, 152, 23);
		panel_3.add(btnNewButton_2);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 650, 633);
		panel_2.add(scrollPane);

		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(table);

		btnNewButton_3 = new JButton("Refresh DB");
		btnNewButton_3.setToolTipText("Refresh makes the GUI database tables to catch up with the internal data");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateDatabaseGUI();
			}
		});
		btnNewButton_3.setBounds(1690, 24, 105, 89);
		panel_1.add(btnNewButton_3);

		panel_4 = new JPanel();
		panel_4.setLayout(null);
		panel_4.setBorder(
				new TitledBorder(null, "Table hostnames", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBounds(690, 11, 490, 711);
		panel_1.add(panel_4);

		panel_5 = new JPanel();
		panel_5.setLayout(null);
		panel_5.setBounds(10, 655, 470, 45);
		panel_4.add(panel_5);

		button = new JButton("Add new entry");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// new hostname
				if (!lockDbEdit) {
					// lockDbEdit = true;
					new EditHostname(0, "");
				}
			}
		});
		button.setBounds(10, 11, 116, 23);
		panel_5.add(button);

		button_1 = new JButton("Edit selected entry");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// update hostname
				if (!lockDbEdit) {
					// lockDbEdit = true;
					int row = table_1.getSelectedRow();
					if (row != -1) {
						new EditHostname(1, (String) table_1.getValueAt(row, 1));
					}
				}
			}
		});
		button_1.setBounds(136, 11, 133, 23);
		panel_5.add(button_1);

		button_2 = new JButton("Delete selected entry");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// delete hostname
				if (!lockDbEdit) {
					// lockDbEdit = true;
					int row = table_1.getSelectedRow();
					if (row != -1) {
						try {
							Logic.removeHostname((String) table_1.getValueAt(row, 1));
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						updateDatabaseGUI();
						// lockDbEdit = false;
					}
				}
			}
		});
		button_2.setBounds(279, 11, 152, 23);
		panel_5.add(button_2);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 11, 470, 633);
		panel_4.add(scrollPane_1);

		table_1 = new JTable();
		table_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table_1.setModel(
				new DefaultTableModel(new Object[][] { { null, null, null }, }, new String[] { "hostname", "userid" }));
		scrollPane_1.setViewportView(table_1);

		panel_6 = new JPanel();
		panel_6.setLayout(null);
		panel_6.setBorder(
				new TitledBorder(null, "Table bluenodes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_6.setBounds(1190, 11, 490, 711);
		panel_1.add(panel_6);

		panel_7 = new JPanel();
		panel_7.setLayout(null);
		panel_7.setBounds(10, 655, 470, 45);
		panel_6.add(panel_7);

		button_3 = new JButton("Add new entry");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// new bluenode
				if (!lockDbEdit) {
					// lockDbEdit = true;
					new EditBluenode(0, "");
				}
			}
		});
		button_3.setBounds(10, 11, 116, 23);
		panel_7.add(button_3);

		button_4 = new JButton("Edit selected entry");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// edit bluenode
				if (!lockDbEdit) {
					// lockDbEdit = true;
					int row = table_2.getSelectedRow();
					if (row != -1) {
						new EditBluenode(1, (String) table_2.getValueAt(row, 0));
					}
				}
			}
		});
		button_4.setBounds(136, 11, 133, 23);
		panel_7.add(button_4);

		button_5 = new JButton("Delete selected entry");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// delete bluenode
				if (!lockDbEdit) {
					// lockDbEdit = true;
					int row = table_2.getSelectedRow();
					if (row != -1) {
						try {
							Logic.removeBluenode((String) table_2.getValueAt(row, 0));
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						updateDatabaseGUI();
						// lockDbEdit = false;
					}
				}
			}
		});
		button_5.setBounds(279, 11, 152, 23);
		panel_7.add(button_5);

		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 11, 470, 633);
		panel_6.add(scrollPane_2);

		table_2 = new JTable();
		table_2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane_2.setViewportView(table_2);
		getContentPane().setLayout(layout);

		pack();
	}

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
		jTextArea1.setText("");
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
		updateBlueNodeTable();
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
		updateRedNodeTable();
	}

	private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckBox1ActionPerformed
		App.autoScrollDown = jCheckBox1.isSelected();
	}

	public synchronized void updateDatabaseGUI() {
		LinkedList<String[][]> data = Logic.buildGUIObject();
		modelUsersDb = new DefaultTableModel(data.poll(), usersDbHead);
		modelHostnamesDb = new DefaultTableModel(data.poll(), hostnamesDbHead);
		modelBluenodesDb = new DefaultTableModel(data.poll(), blunodesDbHead);
		table.setModel(modelUsersDb);
		table_1.setModel(modelHostnamesDb);
		table_2.setModel(modelBluenodesDb);
		repaint();
	}
	
	public synchronized void updateBlueNodeTable() {
		String[][] data = App.BNtable.buildStringInstanceObject();
        bluenodes = new DefaultTableModel(data, bluenodesTableHead);
        jTable2.setModel(bluenodes);
        repaint();
	}
	
	public synchronized void updateRedNodeTable() {
		String[][] data = App.BNtable.buildRednodeStringInstanceObject();
        rednodes = new DefaultTableModel(data, rednodesTableHead);
        jTable1.setModel(rednodes);
        repaint();
	}

	private javax.swing.JPanel BlueNodes;
	private javax.swing.JPanel Console;
	private javax.swing.JPanel RedNodes;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton4;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JTable jTable1;
	private javax.swing.JTable jTable2;
	public javax.swing.JTextArea jTextArea1;
	private JTabbedPane tabbedPane;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JButton btnNewButton_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JButton button;
	private JButton button_1;
	private JButton button_2;
	private JPanel panel_6;
	private JPanel panel_7;
	private JButton button_3;
	private JButton button_4;
	private JButton button_5;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPane_2;
	private JTable table;
	private JTable table_1;
	private JTable table_2;
}
