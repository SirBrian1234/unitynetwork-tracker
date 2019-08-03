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

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.database.Queries;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditHostname {

	private final int type;
	private final String hostname;

	private JFrame frmEditHostnameEntry;
	private JTextField textField_1;
	private JTextField textField_2;
	private JLabel label_1;
	private JButton btnAddNewEntry;
	private JTextField textField;
	private JTextArea textArea;
	private JButton btnNewButton;
	private JLabel lblNewLabel;

	/**
	 * Create the application.
	 */
	public EditHostname(int type, String hostname) {
		this.type = type;
		this.hostname = hostname;
		initialize();
		
		if (type == 0) { 
			btnAddNewEntry.setText("Add new hostname entry");
			textField.setEnabled(false);
			textArea.setEnabled(false);
			btnNewButton.setEnabled(false);
			
		} else {
			btnAddNewEntry.setText("Update hostname entry");
			textField_1.setText(hostname);
			textField_1.setEditable(false);

			updateHostnameEntry(hostname);
		}
		frmEditHostnameEntry.setVisible(true);
	}

	void updateHostnameEntry(String hostname) {
		try (Queries q = Queries.getInstance()) {
			ResultSet r = q.selectAllFromHostnamesWhereHostname(hostname);
			while(r.next()) {
				textField_2.setText(""+r.getInt("userid"));
				String key = r.getString("public");
				String args[] = key.split("\\s+");
				textField.setText(args[0]);
				textArea.setText(args[1]);
				if (args[0].equals("NOT_SET")) {
					lblNewLabel.setText("<html>Copy this session ticket in the rednode in order to upload its public key.</html>");
					btnNewButton.setEnabled(false);
				}
			}
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
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
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(130, 11, 126, 20);
		panel.add(textField);
		textField.setColumns(10);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 42, 417, 82);
		panel.add(textArea);
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		btnNewButton = new JButton("Reset Key");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetKey();
			}
		});
		btnNewButton.setBounds(252, 207, 175, 23);
		panel.add(btnNewButton);
		
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
		
		textField_1 = new JTextField();
		textField_1.setBounds(76, 11, 257, 20);
		panel_1.add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblUserId = new JLabel("User ID");
		lblUserId.setBounds(10, 63, 56, 14);
		panel_1.add(lblUserId);
		
		textField_2 = new JTextField();
		textField_2.setBounds(76, 60, 75, 20);
		panel_1.add(textField_2);
		textField_2.setColumns(10);
		
	    label_1 = new JLabel("");
	    label_1.setBounds(10, 91, 446, 105);
	    panel_1.add(label_1);
	    label_1.setForeground(new Color(204, 0, 0));
	    label_1.setFont(new Font("Tahoma", Font.BOLD, 14));
	    
	    btnAddNewEntry = new JButton("Add new entry");
	    btnAddNewEntry.setBounds(281, 207, 175, 23);
	    panel_1.add(btnAddNewEntry);
	    btnAddNewEntry.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		updateHostname();
	    	}			
	    });
	}
	
	private void updateHostname() {
		if (!textField_1.getText().isEmpty() && !textField_2.getText().isEmpty()){
			if (textField_1.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size() && textField_2.getText().length() <= NumericConstraints.MAX_INT_STR.size()) {
				
				int userid = -1;
				try {
					userid = Integer.parseInt(textField_2.getText());
				} catch (NumberFormatException ex) {
					label_1.setText("<html>Please provide a proper number with digits from 0 to 9</html>");
					return;
				}
				
				if (userid <= 0) {
					label_1.setText("<html>Please provide a number greater than 0.</html>");
					return;
				}
				
				try {
					if (type == 0) {
						String givenHostname = textField_1.getText();
						Pattern pattern = Pattern.compile("^[a-z0-9-_]+$");
					    Matcher matcher = pattern.matcher(givenHostname);
					    if (!matcher.matches()) {
					    	label_1.setText("<html>In order to define a hostname, you are allowed to enter only digit numbers from 0 to 9, small capital letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
					    	return;
					    }
					    Logic.addNewHostname(givenHostname, userid);
					} else {
						Logic.updateHostname(hostname, userid);
					}
				} catch (SQLException e) {
					if (e.getErrorCode() == 19) { 
						label_1.setText("<html>The given hostname is already used.</html>");
						return;
				    } else { 
				    	e.printStackTrace();
				    }	
				} catch (Exception e) {
					label_1.setText("<html>The given userid does not exist.</html>");
					return;
				}
				
				MainWindow.getInstance().updateDatabaseGUI();
				frmEditHostnameEntry.dispose();
			
			} else {
				label_1.setText("<html>Please provide a Hostname up to "+ NumericConstraints.MAX_STR_LEN_SMALL.size() +" characters and a number up to "+NumericConstraints.MAX_INT_STR.size() +" digits.</html>");
			}
		} else {
			label_1.setText("<html>Please fill in all the fields.</html>");
		}			
	}
	
	private void resetKey() {
		if (type==1 && textField.getText().equals("KEY_SET")) {			
			String key = "NOT_SET "+ CryptoUtilities.generateQuestion();

			try (Queries q = Queries.getInstance()) {
				q.updateEntryHostnamesPublicWithHostname(hostname, key);
			} catch (InterruptedException e) {
				AppLogger.getLogger().consolePrint("Could not aquire lock!");
			} catch (SQLException e) {
				AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
			}
			frmEditHostnameEntry.dispose();
		} 
	}
}
