package org.kostiskag.unitynetwork.tracker.gui;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.pairs.Pair;

import org.kostiskag.unitynetwork.tracker.database.BluenodeLogic;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.data.InternalPublicKeyState;
import org.kostiskag.unitynetwork.tracker.AppLogger;

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
	private JTextField usernameField;
	private JTextField publicKeyStatusField;
	private JTextArea publicKeyArea;
	private JLabel infoLabel;
	private JLabel keyInfoLabel;
	private JButton btnResetKey;
	private JButton buttonApply;

	/**
	 * Create the application.
	 */
	public EditBluenode(EditType type, String name) {
		this.type = type;
		this.name = name;
		initialize();

		if (type == EditType.NEW_ENTRY) {
			// new entry
			buttonApply.setText("Add new entry");
			publicKeyStatusField.setEnabled(false);
			publicKeyArea.setEnabled(false);
			btnResetKey.setEnabled(false);
			
		} else {
			// update
			buttonApply.setText("Update entry");
			buttonApply.setEnabled(false);
			nameField.setText(name);
			nameField.setEditable(false);
			usernameField.setEditable(false);
			infoLabel.setText("<html>A bluenode entry may not be changed.</html>");

			Optional<Pair<String, String>> detailsOpt = BluenodeLogic.selectBluenodesUserPublicKey(name);
			if (detailsOpt.isPresent()) {
				var details = detailsOpt.get();
				usernameField.setText("" + details.getVal1());
				String[] args = details.getVal2().split("\\s+");
				if (args[0].equals(InternalPublicKeyState.NOT_SET.toString())) {
					keyInfoLabel.setText("<html>Copy this session ticket in the bluenode in order to upload its public key.</html>");
					btnResetKey.setEnabled(false);
				}
				publicKeyStatusField.setText(args[0]);
				publicKeyArea.setText(args[1]);
			} else {
				frmEditBluenodeEntry.dispose();
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

		JLabel lblUsername = new JLabel("username");
		lblUsername.setBounds(10, 66, 56, 14);
		panel_1.add(lblUsername);

		usernameField = new JTextField();
		usernameField.setBounds(78, 63, 75, 20);
		panel_1.add(usernameField);
		usernameField.setColumns(10);

		infoLabel = new JLabel("");
		infoLabel.setBounds(10, 91, 464, 88);
		panel_1.add(infoLabel);
		infoLabel.setForeground(new Color(204, 0, 0));
		infoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));

		buttonApply = new JButton("Add new entry");
		buttonApply.setBounds(299, 190, 175, 23);
		panel_1.add(buttonApply);
		buttonApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateEntry();
			}
		});
	}

	private void updateEntry() {
		if (!nameField.getText().isEmpty() && !usernameField.getText().isEmpty()) {
			if (nameField.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size()
					&& usernameField.getText().length() <= NumericConstraints.MAX_INT_STR.size()) {

				if (type == EditType.NEW_ENTRY) {
					String givenBluenodeName = nameField.getText();
					Matcher matcher = NAME_PATTERN.matcher(givenBluenodeName);
					if (!matcher.matches()) {
						infoLabel.setText(
								"<html>In order to define a Blue Node name, you are allowed to enter only digit numbers from 0 to 9, lower case letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
						return;
					}
					BluenodeLogic.addNewBluenode(givenBluenodeName, usernameField.getText());
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
		if (type == EditType.UPDATE && publicKeyStatusField.getText().equals(InternalPublicKeyState.KEY_SET.toString())) {
			try {
				Logic.revokePublicKey(NodeType.BLUENODE, name);
			} catch (InterruptedException | SQLException e) {
				AppLogger.getLogger().consolePrint(e.getMessage());
			}
			frmEditBluenodeEntry.dispose();
		} 
	}
}
