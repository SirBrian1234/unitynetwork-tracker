package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.BluenodeLogic;
import org.kostiskag.unitynetwork.tracker.database.HostnameLogic;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.UserLogic;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

import javax.swing.LayoutStyle.ComponentPlacement;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class MainWindow extends javax.swing.JFrame {

	private static MainWindow WINDOW;

	private final String[] bluenodesTableHead = { "Hostname", "Physical Address", "Auth Port", "RedNode Load", "Timestamp" };
	private final String[] rednodesTableHead = { "Hostname", "Virtual Address", "BlueNode Hostname", "Timestamp" };

	private final String[] usersDbHead = { "id", "username", "password", "type", "fullname" };
	private final String[] hostnamesDbHead = { "address", "hostname", "userid" };
	private final String[] blunodesDbHead = { "name", "userid" };

	private final int MAX_MESSAGE_COUNT = 10000;

	private DefaultTableModel bluenodes;
	private DefaultTableModel rednodes;

	private DefaultTableModel modelBluenodesDb;
	private DefaultTableModel modelUsersDb;
	private DefaultTableModel modelHostnamesDb;

	private int messageCount = 0;
	private boolean autoScrollDown = true;
	private About about;

	//enforces singleton obj
	public static MainWindow newInstance() {
		if (MainWindow.WINDOW == null){
			MainWindow.WINDOW = new MainWindow();
		}
		return MainWindow.WINDOW;
	}

	public static MainWindow getInstance() {
		return MainWindow.WINDOW;
	}

	public static boolean isInstance() {
		return MainWindow.WINDOW != null;
	}

	private static void terminateInstance() {
		MainWindow.WINDOW = null;
	}

	private MainWindow() {
		setTitle("Unity Network Tracker");
		bluenodes = new DefaultTableModel(new String[][]{}, bluenodesTableHead);
		rednodes = new DefaultTableModel(new String[][]{}, rednodesTableHead);

		modelUsersDb = new DefaultTableModel(new String[][]{}, usersDbHead);
		modelHostnamesDb = new DefaultTableModel(new String[][]{}, hostnamesDbHead);
		modelBluenodesDb = new DefaultTableModel(new String[][]{}, blunodesDbHead);

		initComponents();

		userTable.setModel(modelUsersDb);
		hostnameTable.setModel(modelHostnamesDb);
		bluenodeTable.setModel(modelBluenodesDb);

		userTable.setDefaultEditor(Object.class, null);
		hostnameTable.setDefaultEditor(Object.class, null);
		bluenodeTable.setDefaultEditor(Object.class, null);

		updateDatabaseGUI();
	}

	public void showWindow() {
		this.setVisible(true);
	}

	public void verboseInfo(String message) {
		jTextArea1.append(message + "\n");
		messageCount++;
		if (messageCount > MAX_MESSAGE_COUNT) {
			messageCount = 0;
			jTextArea1.setText("");
		}
		if (autoScrollDown) {
			jTextArea1.select(jTextArea1.getHeight() + MAX_MESSAGE_COUNT, 0);
		}
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
					MainWindow.terminateInstance();
					App.TRACKER_APP.terminate();
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
		bluenodeActiveTable = new javax.swing.JTable();
		bluenodeActiveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPanel4 = new javax.swing.JPanel();
		jPanel4.setBounds(10, 697, 589, 31);
		jButton1 = new javax.swing.JButton();
		jButton1.setToolTipText("Refresh makes the GUI table to catch up with the internal data");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BlueNodes"));

		bluenodeActiveTable.setModel(bluenodes);
		jScrollPane2.setViewportView(bluenodeActiveTable);

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
		rednodeActiveTable = new javax.swing.JTable();
		rednodeActiveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPanel5 = new javax.swing.JPanel();
		jPanel5.setBounds(10, 697, 546, 31);
		jButton2 = new javax.swing.JButton();
		jButton2.setToolTipText("Refresh makes the GUI table to catch up with the internal data");

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("RedNodes"));

		rednodeActiveTable.setModel(rednodes);
		jScrollPane1.setViewportView(rednodeActiveTable);

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

		jButton4.setText("Clear");
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

		btnAbout = new JButton("About");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (about == null) {
					about = About.instanceOf();
				} else if (!about.isVisible()) {
					about.show();
				}
			}
		});

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6Layout.setHorizontalGroup(
			jPanel6Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup()
					.addComponent(jButton4, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(jCheckBox1)
					.addPreferredGap(ComponentPlacement.RELATED, 223, Short.MAX_VALUE)
					.addComponent(btnAbout, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
		);
		jPanel6Layout.setVerticalGroup(
			jPanel6Layout.createParallelGroup(Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup()
					.addGroup(jPanel6Layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jButton4)
						.addComponent(jCheckBox1)
						.addComponent(btnAbout))
					.addGap(0, 8, Short.MAX_VALUE))
		);
		jPanel6.setLayout(jPanel6Layout);
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
				addNewUser();
			}
		});
		btnNewButton.setBounds(10, 11, 145, 23);
		panel_3.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Edit selected entry");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateUser();
			}
		});
		btnNewButton_1.setBounds(167, 11, 152, 23);
		panel_3.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Delete selected entry");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteUser();
			}
		});
		btnNewButton_2.setBounds(331, 11, 170, 23);
		panel_3.add(btnNewButton_2);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 650, 633);
		panel_2.add(scrollPane);

		userTable = new JTable();
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(userTable);

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
				addNewHostname();
			}
		});
		button.setBounds(10, 11, 116, 23);
		panel_5.add(button);

		button_1 = new JButton("Edit selected entry");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateHostname();
			}
		});
		button_1.setBounds(136, 11, 158, 23);
		panel_5.add(button_1);

		button_2 = new JButton("Delete selected entry");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteHostname();
			}
		});
		button_2.setBounds(306, 11, 152, 23);
		panel_5.add(button_2);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 11, 470, 633);
		panel_4.add(scrollPane_1);

		hostnameTable = new JTable();
		hostnameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		hostnameTable.setModel(
				new DefaultTableModel(new Object[][] { { null, null, null }, }, new String[] { "hostname", "userid" }));
		scrollPane_1.setViewportView(hostnameTable);

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
				addNewBluenode();
			}
		});
		button_3.setBounds(10, 11, 116, 23);
		panel_7.add(button_3);

		button_4 = new JButton("Edit selected entry");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBluenode();
			}
		});
		button_4.setBounds(136, 11, 158, 23);
		panel_7.add(button_4);

		button_5 = new JButton("Delete selected entry");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteBluenode();
			}
		});
		button_5.setBounds(306, 11, 152, 23);
		panel_7.add(button_5);

		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 11, 470, 633);
		panel_6.add(scrollPane_2);

		bluenodeTable = new JTable();
		bluenodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane_2.setViewportView(bluenodeTable);
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
		autoScrollDown = jCheckBox1.isSelected();
	}

	private void addNewUser() {
		// add new user
		new EditUser(EditType.NEW_ENTRY, "");
	}

	private void updateUser() {
		// update user
		int row = userTable.getSelectedRow();
		if (row != -1) {
			new EditUser(EditType.UPDATE, (String) userTable.getValueAt(row, 1));
		}
	}

	private void deleteUser() {
		// delete user
		int row = userTable.getSelectedRow();
		if (row != -1) {
			String username = (String) userTable.getValueAt(row, 1);
			UserLogic.removeUserAndAllHisItems(username);
			updateDatabaseGUI();
		}
	}

	private void addNewHostname() {
		// new hostname
		new EditHostname(EditType.NEW_ENTRY, "");
	}

	private void updateHostname() {
		int row = hostnameTable.getSelectedRow();
		if (row != -1) {
			new EditHostname(EditType.UPDATE, (String) hostnameTable.getValueAt(row, 1));
		}
	}

	private void deleteHostname() {
		// delete hostname
		int row = hostnameTable.getSelectedRow();
		if (row != -1) {
			HostnameLogic.removeHostname((String) hostnameTable.getValueAt(row, 1));
			updateDatabaseGUI();
		}
	}

	private void addNewBluenode() {
		// new bluenode
		new EditBluenode(EditType.NEW_ENTRY, "");
	}

	private void updateBluenode() {
		// edit bluenode
		int row = bluenodeTable.getSelectedRow();
		if (row != -1) {
			new EditBluenode(EditType.UPDATE, (String) bluenodeTable.getValueAt(row, 0));
		}
	}

	private void deleteBluenode() {
		// delete bluenode
		int row = bluenodeTable.getSelectedRow();
		if (row != -1) {
			try {
				BluenodeLogic.removeBluenode((String) bluenodeTable.getValueAt(row, 0));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			updateDatabaseGUI();
			// lockDbEdit = false;
		}
	}

	public synchronized void updateDatabaseGUI() {
		String[][][] data = Logic.buildGUIObject();
		modelUsersDb = new DefaultTableModel(data[0], usersDbHead);
		modelHostnamesDb = new DefaultTableModel(data[1], hostnamesDbHead);
		modelBluenodesDb = new DefaultTableModel(data[2], blunodesDbHead);
		userTable.setModel(modelUsersDb);
		hostnameTable.setModel(modelHostnamesDb);
		bluenodeTable.setModel(modelBluenodesDb);
		repaint();
	}
	
	public synchronized void updateBlueNodeTable() {
	try {
			Lock lock = BlueNodeTable.getInstance().aquireLock();
			String[][] data = BlueNodeTable.getInstance().buildStringInstanceObject(lock);
			bluenodes = new DefaultTableModel(data, bluenodesTableHead);
			bluenodeActiveTable.setModel(bluenodes);
			repaint();
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint(e.getMessage());
		} finally {
			BlueNodeTable.getInstance().releaseLock();
		}
	}
	
	public synchronized void updateRedNodeTable() {
		try {
			Lock lock = BlueNodeTable.getInstance().aquireLock();
			String[][] data = BlueNodeTable.getInstance().buildRednodeStringInstanceObject(lock);
			rednodes = new DefaultTableModel(data, rednodesTableHead);
			rednodeActiveTable.setModel(rednodes);
			repaint();
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint(e.getMessage());
		} finally {
			BlueNodeTable.getInstance().releaseLock();
		}
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
	private javax.swing.JTable rednodeActiveTable;
	private javax.swing.JTable bluenodeActiveTable;
	private javax.swing.JTextArea jTextArea1;
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
	private JTable userTable;
	private JTable hostnameTable;
	private JTable bluenodeTable;
	private JButton btnAbout;
}
