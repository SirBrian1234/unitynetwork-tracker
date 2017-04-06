package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.omg.PortableServer.ServantRetentionPolicyValue;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;

import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class editHostname {

	private JFrame frmEditHostnameEntry;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	int type;
	String hostname;
	private JLabel label_1;
	private JButton btnAddNewEntry;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					editHostname window = new editHostname(0,"none");
					window.frmEditHostnameEntry.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public editHostname(int type, String hostname) {
		this.type = type;
		this.hostname = hostname;
		initialize();
		if (type == 0) { 
			btnAddNewEntry.setText("Add new entry");
		} else {
			btnAddNewEntry.setText("Update entry");
			textField_1.setText(hostname);
			textField_1.setEditable(false);
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
		frmEditHostnameEntry.setBounds(100, 100, 450, 300);
		frmEditHostnameEntry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEditHostnameEntry.getContentPane().setLayout(null);
		
		JLabel label = new JLabel("id");
		label.setBounds(10, 14, 17, 14);
		frmEditHostnameEntry.getContentPane().add(label);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setColumns(10);
		textField.setBounds(76, 11, 75, 20);
		frmEditHostnameEntry.getContentPane().add(textField);
		
		JLabel lblHostname = new JLabel("hostname");
		lblHostname.setBounds(10, 65, 56, 14);
		frmEditHostnameEntry.getContentPane().add(lblHostname);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(76, 62, 257, 20);
		frmEditHostnameEntry.getContentPane().add(textField_1);
		
		btnAddNewEntry = new JButton("Add new entry");
		btnAddNewEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!textField_1.getText().isEmpty() && !textField_2.getText().isEmpty()){
					if (textField_1.getText().length() <= App.max_str_len_small_size && textField_2.getText().length() <= App.max_int_str_len) {
						int userid = -1;
						try {
							userid = Integer.parseInt(textField_2.getText());
						} catch (NumberFormatException ex) {
							label_1.setText("Please provide a proper number with digits from 0 to 9");
							return;
						}
						
						Queries q = null;
						try {
							q = new Queries();
							if (type == 0) {			
								q.insertEntryHostnames(textField_1.getText(), userid);
							} else {
								q.updateEntryHostnamesWithHostname(hostname, userid);
							}
							q.closeQueries();
						} catch (SQLException e) {
							if (e.getErrorCode() == 19) { 
								label_1.setText("The given hostname is already taken.");
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
						
						App.window.updateDatabaseGUI();
						frmEditHostnameEntry.dispose();
					
					} else {
						label_1.setText("Please provide a Hostname up to "+App.max_str_len_small_size+" characters and a number up to "+App.max_int_str_len+" digits.");
					}
				} else {
					label_1.setText("Please fill in all the fields.");
				}			
			}
		});
		btnAddNewEntry.setBounds(300, 228, 124, 23);
		frmEditHostnameEntry.getContentPane().add(btnAddNewEntry);
		
		JLabel lblUserId = new JLabel("user id");
		lblUserId.setBounds(10, 114, 56, 14);
		frmEditHostnameEntry.getContentPane().add(lblUserId);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(76, 111, 75, 20);
		frmEditHostnameEntry.getContentPane().add(textField_2);
		
	    label_1 = new JLabel("");
		label_1.setForeground(new Color(204, 0, 0));
		label_1.setFont(new Font("Tahoma", Font.BOLD, 14));
		label_1.setBounds(10, 167, 414, 14);
		frmEditHostnameEntry.getContentPane().add(label_1);
	}
}
