package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditBluenode {

	private final int type;
	private final String name;

	private JFrame frmEditBluenodeEntry;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextArea textArea;
	private JButton button;
	private JLabel lblNewLabel;
	private JTextField textField;
	private JPanel panel;
	private JButton btnResetKey;
	private JPanel panel_1;
	private JLabel lblNewLabel_1;

	/**
	 * Create the application.
	 */
	public EditBluenode(int type, String name) {
		this.type = type;
		this.name = name;
		initialize();

		// new
		if (type == 0) {
			button.setText("Add new entry");
			textField.setEnabled(false);
			textArea.setEnabled(false);
			btnResetKey.setEnabled(false);
			
		} else {
			button.setText("Update entry");
			textField_1.setText(name);
			textField_1.setEditable(false);

			Queries q = null;
			try {
				q = new Queries();
				ResultSet r = q.selectAllFromBluenodesWhereName(name);
				while (r.next()) {
					textField_2.setText("" + r.getInt("userid"));
					String key = r.getString("public");
					String args[] = key.split("\\s+");
					if (args[0].equals("NOT_SET")) {
						lblNewLabel_1.setText("<html>Copy this session ticket in the bluenode in order to upload its public key.</html>");
						btnResetKey.setEnabled(false);
					}
					textField.setText(args[0]);
					textArea.setText(args[1]);					
				}
				q.closeQueries();
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					q.closeQueries();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		frmEditBluenodeEntry.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmEditBluenodeEntry = new JFrame();
		frmEditBluenodeEntry.setResizable(false);
		frmEditBluenodeEntry.setTitle("Edit bluenode entry");
		frmEditBluenodeEntry.setBounds(100, 100, 995, 277);
		frmEditBluenodeEntry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEditBluenodeEntry.getContentPane().setLayout(null);
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(504, 11, 475, 224);
		frmEditBluenodeEntry.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblPublicKeyStatus = new JLabel("Public Key Status");
		lblPublicKeyStatus.setBounds(10, 17, 110, 14);
		panel.add(lblPublicKeyStatus);

		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(130, 11, 126, 20);
		panel.add(textField);
		textField.setColumns(10);

		textArea = new JTextArea();
		textArea.setBounds(10, 42, 455, 70);
		panel.add(textArea);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setLineWrap(true);

		btnResetKey = new JButton("Reset Key");
		btnResetKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetKey();
			}			
		});
		btnResetKey.setBounds(290, 190, 175, 23);
		panel.add(btnResetKey);

		lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setBounds(10, 123, 455, 56);
		panel.add(lblNewLabel_1);

		panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(10, 11, 484, 224);
		frmEditBluenodeEntry.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JLabel lblName = new JLabel("BlueNode Name");
		lblName.setBounds(10, 14, 110, 14);
		panel_1.add(lblName);

		textField_1 = new JTextField();
		textField_1.setBounds(130, 11, 257, 20);
		panel_1.add(textField_1);
		textField_1.setColumns(10);

		JLabel lblUserId = new JLabel("User ID");
		lblUserId.setBounds(10, 66, 56, 14);
		panel_1.add(lblUserId);

		textField_2 = new JTextField();
		textField_2.setBounds(78, 63, 75, 20);
		panel_1.add(textField_2);
		textField_2.setColumns(10);				

		lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(10, 91, 464, 88);
		panel_1.add(lblNewLabel);
		lblNewLabel.setForeground(new Color(204, 0, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));

		button = new JButton("Add new entry");
		button.setBounds(299, 190, 175, 23);
		panel_1.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateEntry();
			}
		});
	}

	private void updateEntry() {
		if (!textField_1.getText().isEmpty() && !textField_2.getText().isEmpty()) {
			if (textField_1.getText().length() <= App.MAX_STR_LEN_SMALL_SIZE
					&& textField_2.getText().length() <= App.MAX_INT_STR_LEN) {

				int userid = -1;
				try {
					userid = Integer.parseInt(textField_2.getText());
				} catch (NumberFormatException ex) {
					lblNewLabel.setText("<html>Please provide a proper number with digits from 0 to 9</html>");
					return;
				}

				if (userid <= 0) {
					lblNewLabel.setText("<html>Please provide a number greater than 0.</html>");
					return;
				}

				Queries q = null;
				try {
					q = new Queries();
					if (q.checkIfUserWithIdExists(userid)) {
						if (type == 0) {
							String givenBluenodeName = textField_1.getText();
							Pattern pattern = Pattern.compile("^[a-z0-9-_]+$");
							Matcher matcher = pattern.matcher(givenBluenodeName);
							if (!matcher.matches()) {
								lblNewLabel.setText(
										"<html>In order to define a Blue Node name, you are allowed to enter only digit numbers from 0 to 9, lower case letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
								q.closeQueries();
								return;
							}
							String publicKey = "NOT_SET " + CryptoMethods.generateQuestion();
							q.insertEntryBluenodes(givenBluenodeName, userid, publicKey);
						} else {
							q.updateEntryBluenodesWithName(name, userid);
						}
					} else {
						lblNewLabel.setText("<html>The given userid does not exist.</html>");
						q.closeQueries();
						return;
					}
					q.closeQueries();
				} catch (SQLException e) {
					if (e.getErrorCode() == 19) {
						lblNewLabel.setText("<html>The given bluenode name is already used.</html>");
						return;
					} else {
						e.printStackTrace();
					}
					try {
						q.closeQueries();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}

				App.TRACKER_APP.window.updateDatabaseGUI();
				frmEditBluenodeEntry.dispose();

			} else {
				lblNewLabel.setText("<html>Please provide a Hostname up to " + App.MAX_STR_LEN_SMALL_SIZE
						+ " characters and a number up to " + App.MAX_INT_STR_LEN + " digits.</html>");
			}
		} else {
			lblNewLabel.setText("<html>Please fill in all the fields.</html>");
		}
	}
	
	private void resetKey() {
		if (type==1 && textField.getText().equals("KEY_SET")) {			
			String key = "NOT_SET "+CryptoMethods.generateQuestion();
			Queries q = null;
			try {
				q = new Queries();
				q.updateEntryBluenodesPublicWithName(name, key);
				q.closeQueries();
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					q.closeQueries();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			frmEditBluenodeEntry.dispose();
		} 
	}
}
