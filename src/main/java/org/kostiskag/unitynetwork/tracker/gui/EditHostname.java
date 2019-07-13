package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.entry.NodeType;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.HostnameLogic;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.data.InternalPublicKeyState;
import org.kostiskag.unitynetwork.tracker.database.data.Tuple;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditHostname {

	private final EditType type;
	private final String hostname;

	private JFrame frmEditHostnameEntry;
	private JLabel lblNewLabel;
	private JLabel infoLabel;
	private JTextField hostnameField;
	private JTextField usernameField;
	private JTextField publicKeyStatusTextField;
	private JTextArea publicKeyTextArea;
	private JButton updateButton;
	private JButton resetKeyButton;

	/**
	 * Create the application.
	 */
	public EditHostname(EditType type, String hostname) {
		this.type = type;
		this.hostname = hostname;
		initialize();
		
		if (type == EditType.NEW_ENTRY) {
			updateButton.setText("Add new hostname entry");
			publicKeyStatusTextField.setEnabled(false);
			publicKeyTextArea.setEnabled(false);
			resetKeyButton.setEnabled(false);
			
		} else {
			updateButton.setText("Update hostname entry");
			updateButton.setEnabled(false);
			hostnameField.setText(hostname);
			hostnameField.setEditable(false);
			usernameField.setEditable(false);
			infoLabel.setText("A hostname may not be changed.");
			Optional<Tuple<String, VirtualAddress, String>> tupleOpt = HostnameLogic.getHostnameEntry(hostname);
			if (tupleOpt.isPresent()) {
				var tuple = tupleOpt.get();
				usernameField.setText("" + tuple.getVal1());
				String[] key = tuple.getVal3().split("\\s+");
				publicKeyStatusTextField.setText(key[0]);
				publicKeyTextArea.setText(key[1]);
				if (key[0].equals(InternalPublicKeyState.NOT_SET.toString())) {
					lblNewLabel.setText("<html>Copy this session ticket in the rednode in order to upload its public key.</html>");
					resetKeyButton.setEnabled(false);
				}
			} else {
				frmEditHostnameEntry.dispose();
			}
		}
		frmEditHostnameEntry.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmEditHostnameEntry = new JFrame();
		frmEditHostnameEntry.setResizable(false);
		frmEditHostnameEntry.setTitle("Edit hostname entry");
		frmEditHostnameEntry.setBounds(100, 100, 939, 295);
		frmEditHostnameEntry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEditHostnameEntry.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(486, 11, 437, 241);
		frmEditHostnameEntry.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel label = new JLabel("Public Key Status");
		label.setBounds(10, 17, 110, 14);
		panel.add(label);
		
		publicKeyStatusTextField = new JTextField();
		publicKeyStatusTextField.setEditable(false);
		publicKeyStatusTextField.setBounds(130, 11, 126, 20);
		panel.add(publicKeyStatusTextField);
		publicKeyStatusTextField.setColumns(10);
		
		publicKeyTextArea = new JTextArea();
		publicKeyTextArea.setBounds(10, 42, 417, 82);
		panel.add(publicKeyTextArea);
		publicKeyTextArea.setLineWrap(true);
		publicKeyTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		resetKeyButton = new JButton("Reset Key");
		resetKeyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetKey();
			}
		});
		resetKeyButton.setBounds(252, 207, 175, 23);
		panel.add(resetKeyButton);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(10, 135, 417, 54);
		panel.add(lblNewLabel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(10, 11, 466, 241);
		frmEditHostnameEntry.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblHostname = new JLabel("Hostname");
		lblHostname.setBounds(10, 14, 56, 14);
		panel_1.add(lblHostname);
		
		hostnameField = new JTextField();
		hostnameField.setBounds(76, 11, 257, 20);
		panel_1.add(hostnameField);
		hostnameField.setColumns(10);
		
		JLabel lblUsername = new JLabel("username");
		lblUsername.setBounds(10, 63, 56, 14);
		panel_1.add(lblUsername);
		
		usernameField = new JTextField();
		usernameField.setBounds(76, 60, 75, 20);
		panel_1.add(usernameField);
		usernameField.setColumns(10);
		
	    infoLabel = new JLabel("");
	    infoLabel.setBounds(10, 91, 446, 105);
	    panel_1.add(infoLabel);
	    infoLabel.setForeground(new Color(204, 0, 0));
	    infoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
	    
	    updateButton = new JButton("Add new entry");
	    updateButton.setBounds(281, 207, 175, 23);
	    panel_1.add(updateButton);
	    updateButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		updateHostname();
	    	}			
	    });
	}
	
	private void updateHostname() {
		if (!hostnameField.getText().isEmpty() && !usernameField.getText().isEmpty()){
			if (hostnameField.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size() && usernameField.getText().length() <= NumericConstraints.MAX_INT_STR.size()) {
				String username = usernameField.getText();
				if (type == EditType.NEW_ENTRY) {
					String givenHostname = hostnameField.getText();
					Pattern pattern = Pattern.compile("^[a-z0-9-_]+$");
					Matcher matcher = pattern.matcher(givenHostname);
					if (!matcher.matches()) {
						infoLabel.setText("<html>In order to define a hostname, you are allowed to enter only digit numbers from 0 to 9, small capital letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
						return;
					}
					if (!HostnameLogic.addNewHostname(givenHostname, username)) {
						infoLabel.setText("<html>The given hostname is was not created!</html>");
						return;
					}
				}

				MainWindow.getInstance().updateDatabaseGUI();
				frmEditHostnameEntry.dispose();
			
			} else {
				infoLabel.setText("<html>Please provide a Hostname up to "+ NumericConstraints.MAX_STR_LEN_SMALL.size() +" characters and a number up to "+NumericConstraints.MAX_INT_STR.size() +" digits.</html>");
			}
		} else {
			infoLabel.setText("<html>Please fill in all the fields.</html>");
		}			
	}
	
	private void resetKey() {
		if (type == EditType.UPDATE && publicKeyStatusTextField.getText().equals(InternalPublicKeyState.KEY_SET.toString())) {
			try {
				Logic.revokePublicKey(NodeType.REDNODE, hostname);
			} catch (InterruptedException | SQLException e) {
				AppLogger.getLogger().consolePrint(e.getMessage());
			}
			frmEditHostnameEntry.dispose();
		} 
	}
}
