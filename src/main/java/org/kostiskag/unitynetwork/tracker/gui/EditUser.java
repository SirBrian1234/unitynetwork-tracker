package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.rundata.calculated.NumericConstraints;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditUser {

	// type is
	// 0 for new entry
	// 1 for update
	public static final int NEW_ENTRY = 0;
	public static final int UPDATE = 1;

	private final int type;
	private final String username;

	private JFrame frmEditUserEntry;
	private JTextField textField;
	private JLabel lblUsername;
	private JTextField textField_1;
	private JLabel lblPassword;
	private JLabel lblType;
	private JTextField textField_3;
	private JPasswordField passwordField;
	private JButton btnNewButton;
	private JLabel label;
	private JComboBox<String> comboBox;
	private JCheckBox chckbxSetANew;

	/**
	 * Create the application.
	 */
	public EditUser(int type, String username) {
		this.type = type;
		this.username = username;
		initialize();
		if (type == EditUser.NEW_ENTRY) {
			btnNewButton.setText("Add new entry");
			chckbxSetANew.setSelected(true);
			chckbxSetANew.setEnabled(false);
		} else if (type == EditUser.UPDATE){
			btnNewButton.setText("Update entry");
			textField_1.setText(username);
			textField_1.setEditable(false);
			chckbxSetANew.setSelected(false);
			passwordField.setEditable(false);

			try (Queries q = Queries.getInstance()) {
				ResultSet r = q.selectIdScopeFullnameFromUsersWhereUsername(username);
				while (r.next()) {
					textField.setText("" + r.getInt("id"));
					comboBox.setSelectedIndex(r.getInt("scope"));
					textField_3.setText(r.getString("fullname"));
				}

			} catch (InterruptedException e) {
				AppLogger.getLogger().consolePrint("Could not acquire lock!");
			} catch (SQLException e) {
				AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
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
		frmEditUserEntry.setBounds(100, 100, 446, 451);
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
		lblUsername.setBounds(10, 58, 93, 14);
		frmEditUserEntry.getContentPane().add(lblUsername);

		textField_1 = new JTextField();
		textField_1.setBounds(115, 55, 279, 20);
		frmEditUserEntry.getContentPane().add(textField_1);
		textField_1.setColumns(10);

		lblPassword = new JLabel("password");
		lblPassword.setBounds(10, 128, 56, 14);
		frmEditUserEntry.getContentPane().add(lblPassword);

		lblType = new JLabel("type");
		lblType.setBounds(10, 169, 46, 14);
		frmEditUserEntry.getContentPane().add(lblType);

		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(
				new String[] { "system", "user", "robot", "goverment service/organisation/company " }));
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

				String givenFullname = textField_3.getText();
				if (givenFullname.length() > NumericConstraints.MAX_STR_LEN_SMALL.size()) {
					label.setText("<html>Please set a fullname not more than " + NumericConstraints.MAX_STR_LEN_SMALL.size() + " characters.</html>");
					return;
				}

				if (givenFullname.length() < NumericConstraints.MIN_USERNAME.size()) {
					label.setText("<html>Please set a fullname " + NumericConstraints.MIN_USERNAME.size() + " characters or more.</html>");
					return;
				}

				Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\.\\ \\-\\_]+$");
				Matcher matcher = pattern.matcher(givenFullname);
				if (!matcher.matches()) {
					label.setText(
							"<html>In order to define a fullname, you are allowed to enter digit numbers from 0 to 9, lower or upper case letters form aA to zZ, space and upper dash -, lower dash _ or dot . special characters</html>");
					return;
				}

				if (type == 0) {
					String givenUsername = textField_1.getText();
					if (givenUsername.length() > NumericConstraints.MAX_STR_LEN_SMALL.size()) {
						label.setText("<html>Please provide one username which is less than " + NumericConstraints.MAX_STR_LEN_SMALL.size()
								+ " characters.</html>");
						return;
					}
					
					if (givenUsername.length() < NumericConstraints.MIN_USERNAME.size()) {
						label.setText(
								"<html>Please provide one username " + NumericConstraints.MIN_USERNAME.size() + " characters or more.</html>");
						return;
					}
					
					pattern = Pattern.compile("^[a-z0-9-_]+$");
					matcher = pattern.matcher(givenUsername);
					if (!matcher.matches()) {
						label.setText(
								"<html>In order to define a username, you are allowed to enter digit numbers from 0 to 9, lower case letters form a to z and upper dash - or lower dash _ special characters</html>");
						return;
					}
					
					String givenPassword = new String(passwordField.getPassword());
					if (givenPassword.length() > NumericConstraints.MAX_STR_LEN_SMALL.size()) {
						label.setText("<html>Please set a password less than " + NumericConstraints.MAX_STR_LEN_SMALL.size() + " characters.</html>");
						return;
					}

					if (givenPassword.length() < NumericConstraints.MIN_PASSWORD.size()) {
						label.setText("<html>Please provide a password " + NumericConstraints.MIN_PASSWORD.size() + " characters or more.</html>");
						return;
					}
					
					try {
						Logic.addNewUser(givenUsername, givenPassword, comboBox.getSelectedIndex(), givenFullname);
					} catch (SQLException ex) {
						if (ex.getErrorCode() == 19) {
							label.setText("<html>The given username is already used.</html>");
						} else {
							ex.printStackTrace();
						}
					}
				} else if (chckbxSetANew.isSelected()) {
						// we have to provide all the other fields along with a
						// new password
						String givenPassword = new String(passwordField.getPassword());
						if (givenPassword.length() > NumericConstraints.MAX_STR_LEN_LARGE.size()) {
							label.setText(
									"<html>Please set a password less than " + NumericConstraints.MAX_STR_LEN_LARGE.size() + " characters.</html>");
							return;
						}

						if (givenPassword.length() < NumericConstraints.MIN_PASSWORD.size()) {
							label.setText("<html>Please provide a password " + NumericConstraints.MIN_PASSWORD.size() + " characters or more.</html>");
							return;
						}

						try {
							Logic.updateUserAndPassword(username, givenPassword, comboBox.getSelectedIndex(),
									givenFullname);
						} catch (SQLException ex) {
							ex.printStackTrace();
						}

				} else {
					// we have to provide all the other fields without
					// username and password
					try (Queries q = Queries.getInstance()) {
						q.updateEntryUsersWhitoutPasswordWithUsername(username, comboBox.getSelectedIndex(),
								givenFullname);
					} catch (InterruptedException e1) {
						AppLogger.getLogger().consolePrint("Could not acquire lock!");
					} catch (SQLException e2) {
						AppLogger.getLogger().consolePrint(e2.getLocalizedMessage());
					}
				}
				
				MainWindow.getInstance().updateDatabaseGUI();
				frmEditUserEntry.dispose();
			}
		});
		btnNewButton.setBounds(253, 380, 175, 23);
		frmEditUserEntry.getContentPane().add(btnNewButton);

		passwordField = new JPasswordField();
		passwordField.setBounds(113, 125, 279, 20);
		frmEditUserEntry.getContentPane().add(passwordField);

		label = new JLabel("");
		label.setForeground(new Color(204, 0, 0));
		label.setFont(new Font("Tahoma", Font.BOLD, 14));
		label.setBounds(10, 241, 414, 126);
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
		chckbxSetANew.setBounds(22, 96, 167, 23);
		frmEditUserEntry.getContentPane().add(chckbxSetANew);
	}

	//should be moved to tests
	//Launch the application.
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EditUser window = new EditUser(0, "none");
					window.frmEditUserEntry.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
