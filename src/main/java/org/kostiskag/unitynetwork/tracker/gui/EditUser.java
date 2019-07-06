package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;

import org.kostiskag.unitynetwork.tracker.database.data.Tuple;
import org.kostiskag.unitynetwork.tracker.database.UserLogic;

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
	private JTextField useridField;
	private JLabel lblUsername;
	private JTextField usernameField;
	private JLabel lblPassword;
	private JLabel lblType;
	private JTextField fullnameField;
	private JPasswordField passwordField;
	private JButton btnNewButton;
	private JLabel label;
	private JComboBox<String> scopeComboBox;
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
			usernameField.setText(username);
			usernameField.setEditable(false);
			chckbxSetANew.setSelected(false);
			passwordField.setEditable(false);

			Tuple<Integer, Integer, String> details = UserLogic.getUserDetails(username);
			if (details != null) {
				useridField.setText("" + details.getVal1());
				scopeComboBox.setSelectedIndex(details.getVal2());
				fullnameField.setText(details.getVal3());
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

		useridField = new JTextField();
		useridField.setEditable(false);
		useridField.setBounds(76, 11, 75, 20);
		frmEditUserEntry.getContentPane().add(useridField);
		useridField.setColumns(10);

		lblUsername = new JLabel("username");
		lblUsername.setBounds(10, 58, 93, 14);
		frmEditUserEntry.getContentPane().add(lblUsername);

		usernameField = new JTextField();
		usernameField.setBounds(115, 55, 279, 20);
		frmEditUserEntry.getContentPane().add(usernameField);
		usernameField.setColumns(10);

		lblPassword = new JLabel("password");
		lblPassword.setBounds(10, 128, 56, 14);
		frmEditUserEntry.getContentPane().add(lblPassword);

		lblType = new JLabel("type");
		lblType.setBounds(10, 169, 46, 14);
		frmEditUserEntry.getContentPane().add(lblType);

		scopeComboBox = new JComboBox<String>();
		scopeComboBox.setModel(new DefaultComboBoxModel<String>(
				new String[] { "system", "user", "robot", "goverment service/organisation/company " }));
		scopeComboBox.setBounds(76, 166, 257, 20);
		frmEditUserEntry.getContentPane().add(scopeComboBox);

		JLabel lblFullName = new JLabel("full name");
		lblFullName.setBounds(10, 211, 56, 14);
		frmEditUserEntry.getContentPane().add(lblFullName);

		fullnameField = new JTextField();
		fullnameField.setColumns(10);
		fullnameField.setBounds(76, 208, 257, 20);
		frmEditUserEntry.getContentPane().add(fullnameField);

		btnNewButton = new JButton("Add new entry");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String givenFullname = fullnameField.getText();
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
					String givenUsername = usernameField.getText();
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
						UserLogic.addNewUser(givenUsername, givenPassword, scopeComboBox.getSelectedIndex(), givenFullname);
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
							UserLogic.updateUserAndPassword(username, givenPassword, scopeComboBox.getSelectedIndex(),
									givenFullname);
						} catch (SQLException ex) {
							ex.printStackTrace();
						}

				} else {
					// we have to provide all the other fields without password
					UserLogic.updateUserScopeFullName(username, scopeComboBox.getSelectedIndex(), givenFullname);
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
