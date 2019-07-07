package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.BluenodeLogic;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.UserLogic;
import org.kostiskag.unitynetwork.tracker.database.data.Pair;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditBluenode {

	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9-_]+$");

	private final EditType type;
	private final String name;

	private JFrame frmEditBluenodeEntry;
	private JPanel panel;
	private JPanel panel_1;
	private JTextField nameField;
	private JTextField idField;
	private JTextField publicKeyStatusField;
	private JTextArea publicKeyArea;
	private JLabel infoLabel;
	private JLabel keyInfoLabel;
	private JButton btnResetKey;
	private JButton button;

	/**
	 * Create the application.
	 */
	public EditBluenode(EditType type, String name) {
		this.type = type;
		this.name = name;
		initialize();

		// new
		if (type == EditType.NEW_ENTRY) {
			button.setText("Add new entry");
			publicKeyStatusField.setEnabled(false);
			publicKeyArea.setEnabled(false);
			btnResetKey.setEnabled(false);
			
		} else {
			button.setText("Update entry");
			button.setEnabled(false);
			nameField.setText(name);
			nameField.setEditable(false);
			idField.setEditable(false);
			infoLabel.setText("<html>You are not allowed to update a bluenode please delete it and create a new one!</html>");

			Pair<Integer, String> details = BluenodeLogic.selectBluenodeDetails(name);
			if (details != null) {
				idField.setText("" + details.getVal1());
				String key = details.getVal2();
				String args[] = key.split("\\s+");
				if (args[0].equals("NOT_SET")) {
					keyInfoLabel.setText("<html>Copy this session ticket in the bluenode in order to upload its public key.</html>");
					btnResetKey.setEnabled(false);
				}
				publicKeyStatusField.setText(args[0]);
				publicKeyArea.setText(args[1]);
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

		publicKeyStatusField = new JTextField();
		publicKeyStatusField.setEditable(false);
		publicKeyStatusField.setBounds(130, 11, 126, 20);
		panel.add(publicKeyStatusField);
		publicKeyStatusField.setColumns(10);

		publicKeyArea = new JTextArea();
		publicKeyArea.setBounds(10, 42, 455, 70);
		panel.add(publicKeyArea);
		publicKeyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		publicKeyArea.setLineWrap(true);

		btnResetKey = new JButton("Reset Key");
		btnResetKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetKey();
			}			
		});
		btnResetKey.setBounds(290, 190, 175, 23);
		panel.add(btnResetKey);

		keyInfoLabel = new JLabel("");
		keyInfoLabel.setBounds(10, 123, 455, 56);
		panel.add(keyInfoLabel);

		panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(10, 11, 484, 224);
		frmEditBluenodeEntry.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JLabel lblName = new JLabel("BlueNode Name");
		lblName.setBounds(10, 14, 110, 14);
		panel_1.add(lblName);

		nameField = new JTextField();
		nameField.setBounds(130, 11, 257, 20);
		panel_1.add(nameField);
		nameField.setColumns(10);

		JLabel lblUserId = new JLabel("User ID");
		lblUserId.setBounds(10, 66, 56, 14);
		panel_1.add(lblUserId);

		idField = new JTextField();
		idField.setBounds(78, 63, 75, 20);
		panel_1.add(idField);
		idField.setColumns(10);

		infoLabel = new JLabel("");
		infoLabel.setBounds(10, 91, 464, 88);
		panel_1.add(infoLabel);
		infoLabel.setForeground(new Color(204, 0, 0));
		infoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));

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
		if (!nameField.getText().isEmpty() && !idField.getText().isEmpty()) {
			if (nameField.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size()
					&& idField.getText().length() <= NumericConstraints.MAX_INT_STR.size()) {

				int userid = -1;
				try {
					userid = Integer.parseInt(idField.getText());
				} catch (NumberFormatException ex) {
					infoLabel.setText("<html>Please provide a proper number with digits from 0 to 9</html>");
					return;
				}
				if (userid <= 0) {
					infoLabel.setText("<html>Please provide a number greater than 0.</html>");
					return;
				}

				if (UserLogic.checkExistingUserId(userid)) {
					if (type == EditType.NEW_ENTRY) {
						String givenBluenodeName = nameField.getText();
						Matcher matcher = NAME_PATTERN.matcher(givenBluenodeName);
						if (!matcher.matches()) {
							infoLabel.setText(
									"<html>In order to define a Blue Node name, you are allowed to enter only digit numbers from 0 to 9, lower case letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
							return;
						}
						BluenodeLogic.addNewBluenode(givenBluenodeName, userid);
					} else {
						//you can't update
						return;
					}
				} else {
					infoLabel.setText("<html>The given userid does not exist.</html>");
					return;
				}

				MainWindow.getInstance().updateDatabaseGUI();
				frmEditBluenodeEntry.dispose();

			} else {
				infoLabel.setText("<html>Please provide a Hostname up to " + NumericConstraints.MAX_STR_LEN_SMALL.size()
						+ " characters and a number up to " + NumericConstraints.MAX_INT_STR.size() + " digits.</html>");
			}
		} else {
			infoLabel.setText("<html>Please fill in all the fields.</html>");
		}
	}
	
	private void resetKey() {
		if (type== EditType.UPDATE && publicKeyStatusField.getText().equals("KEY_SET")) {
			try {
				Logic.revokePublicKey(NodeType.BLUENODE, name);
			} catch (InterruptedException | SQLException e) {
				AppLogger.getLogger().consolePrint(e.getMessage());
			}
			frmEditBluenodeEntry.dispose();
		} 
	}
}
