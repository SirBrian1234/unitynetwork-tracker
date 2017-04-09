package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Logic;
import kostiskag.unitynetwork.tracker.database.Queries;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class editUser {

	private JFrame frmEditUserEntry;
	private JTextField textField;
	private JLabel lblUsername;
	private JTextField textField_1;
	private JLabel lblPassword;
	private JLabel lblType;
	private JTextField textField_3;
	private int type;
	private String username;
	private JPasswordField passwordField;
	private JButton btnNewButton;
	private JLabel label;
	private JComboBox<String> comboBox;
	JCheckBox chckbxSetANew;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					editUser window = new editUser(0,"none");
					window.frmEditUserEntry.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public editUser(int type, String username) {
		//type is 
		//0 for new entry
		//1 for update
		this.type = type;
		this.username = username;
		initialize();
		if (type == 0) {
			btnNewButton.setText("Add new entry");
			chckbxSetANew.setSelected(true);
			chckbxSetANew.setEnabled(false);
		} else {
			btnNewButton.setText("Update entry");
			textField_1.setText(username);
			textField_1.setEditable(false);
			chckbxSetANew.setSelected(false);
			passwordField.setEditable(false);
			
			Queries q = null;
			try {
				q = new Queries();
				ResultSet r = q.selectIdScopeFullnameFromUsersWhereUsername(username);
				while(r.next()) {
					textField.setText(""+r.getInt("id"));
					comboBox.setSelectedIndex(r.getInt("scope"));
					textField_3.setText(r.getString("fullname"));
				}
				q.closeQueries();
			} catch (SQLException | InterruptedException e) {
				e.printStackTrace();
				try {
					q.closeQueries();
				} catch (SQLException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		frmEditUserEntry.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmEditUserEntry = new JFrame();
		frmEditUserEntry.setResizable(false);
		frmEditUserEntry.setTitle("Edit user entry");
		frmEditUserEntry.setBounds(100, 100, 450, 325);
		frmEditUserEntry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEditUserEntry.getContentPane().setLayout(null);
		
		JLabel lblId = new JLabel("id");
		lblId.setBounds(10, 14, 17, 14);
		frmEditUserEntry.getContentPane().add(lblId);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(76, 11, 75, 20);
		frmEditUserEntry.getContentPane().add(textField);
		textField.setColumns(10);
		
		lblUsername = new JLabel("username");
		lblUsername.setBounds(10, 58, 56, 14);
		frmEditUserEntry.getContentPane().add(lblUsername);
		
		textField_1 = new JTextField();
		textField_1.setBounds(76, 55, 128, 20);
		frmEditUserEntry.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		lblPassword = new JLabel("password");
		lblPassword.setBounds(47, 128, 56, 14);
		frmEditUserEntry.getContentPane().add(lblPassword);
		
		lblType = new JLabel("type");
		lblType.setBounds(10, 169, 46, 14);
		frmEditUserEntry.getContentPane().add(lblType);
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"system", "user", "robot", "goverment service/organisation/company "}));
		comboBox.setBounds(76, 166, 257, 20);
		frmEditUserEntry.getContentPane().add(comboBox);
		
		JLabel lblFullName = new JLabel("full name");
		lblFullName.setBounds(10, 211, 56, 14);
		frmEditUserEntry.getContentPane().add(lblFullName);
		
		textField_3 = new JTextField();
		textField_3.setColumns(10);
		textField_3.setBounds(76, 208, 257, 20);
		frmEditUserEntry.getContentPane().add(textField_3);
		
		btnNewButton = new JButton("Add new entry");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String password = new String(passwordField.getPassword());
				if (!textField_1.getText().isEmpty() && !textField_3.getText().isEmpty()){
					if (textField_1.getText().length() <= App.max_str_len_small_size && textField_3.getText().length() <= App.max_str_len_large_size) {
						
						try {
							if (type == 0) {
								Logic.addNewUser(textField_1.getText(), password, comboBox.getSelectedIndex(), textField_3.getText());
							} else {
								if (chckbxSetANew.isSelected()) {
									//we have to provide all the other fields along with a new password
									if (!password.isEmpty()) {
										if ( password.length() <= App.max_str_len_large_size ) {
											Logic.updateUserAndPassword(username, password, comboBox.getSelectedIndex(), textField_3.getText());
										} else {
											label.setText("Please provide a password up to "+App.max_str_len_large_size+" characters.");
											return;
										}
									} else {
										label.setText("Please fill in all the fields.");
										return;
									}
								} else {
									//we have to provide just the other fields without the password
									Queries q = null;
									q = new Queries();
									q.updateEntryUsersWhitoutPasswordWithUsername(username, comboBox.getSelectedIndex(), textField_3.getText());
									q.closeQueries();
								}
							}							
						} catch (SQLException ex) {
							if (ex.getErrorCode() == 19) { 
								label.setText("The given username is already taken.");
						    } else { 
						    	ex.printStackTrace();
						    }	
						} catch (Exception e1) {
							e1.printStackTrace();
						} 										
						
						App.window.updateDatabaseGUI();
						frmEditUserEntry.dispose();
					
					} else {
						label.setText("Please provide a username and a fullname up to "+App.max_str_len_large_size+" characters.");
					}
				} else {
					label.setText("Please fill in all the fields.");
				}			
			}
		});
		btnNewButton.setBounds(296, 263, 128, 23);
		frmEditUserEntry.getContentPane().add(btnNewButton);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(113, 125, 257, 20);
		frmEditUserEntry.getContentPane().add(passwordField);
		
		label = new JLabel("");
		label.setForeground(new Color(204, 0, 0));
		label.setFont(new Font("Tahoma", Font.BOLD, 14));
		label.setBounds(10, 238, 414, 14);
		frmEditUserEntry.getContentPane().add(label);
		
		chckbxSetANew = new JCheckBox("set a new password");
		chckbxSetANew.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (chckbxSetANew.isSelected()) {
					passwordField.setEditable(true);
				} else {
					passwordField.setEditable(false);
				}
			}
		});
		chckbxSetANew.setSelected(true);
		chckbxSetANew.setBounds(10, 95, 167, 23);
		frmEditUserEntry.getContentPane().add(chckbxSetANew);
	}
}
