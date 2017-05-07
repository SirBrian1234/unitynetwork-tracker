package kostiskag.unitynetwork.tracker.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class EditBluenode {

	private JFrame frmEditBluenodeEntry;
	private JTextField textField_1;
	private JTextField textField_2;
	private int type;
	private String name;
	private JButton button;
	private JLabel lblNewLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EditBluenode window = new EditBluenode(0, "none");
					window.frmEditBluenodeEntry.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EditBluenode(int type, String name) {
		this.type = type;
		this.name = name;
		initialize();
		lblNewLabel.setText("");
		
		//new
		if (type == 0) {
			this.button.setText("Add new entry");
		} else {
			this.button.setText("Update entry");
			textField_1.setText(name);
			textField_1.setEditable(false);
			
			Queries q = null;
			try {
				q = new Queries();
				ResultSet r = q.selectAllFromBluenodesWhereName(name);
				while(r.next()) {
					textField_2.setText(""+r.getInt("userid"));
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
		frmEditBluenodeEntry.setBounds(100, 100, 450, 300);
		frmEditBluenodeEntry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEditBluenodeEntry.getContentPane().setLayout(null);
		
		JLabel lblName = new JLabel("name");
		lblName.setBounds(10, 29, 56, 14);
		frmEditBluenodeEntry.getContentPane().add(lblName);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(76, 26, 257, 20);
		frmEditBluenodeEntry.getContentPane().add(textField_1);
		
		JLabel label_2 = new JLabel("user id");
		label_2.setBounds(10, 78, 56, 14);
		frmEditBluenodeEntry.getContentPane().add(label_2);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(76, 75, 75, 20);
		frmEditBluenodeEntry.getContentPane().add(textField_2);
		
	    button = new JButton("Add new entry");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!textField_1.getText().isEmpty() && !textField_2.getText().isEmpty()){
					if (textField_1.getText().length() <= App.max_str_len_small_size && textField_2.getText().length() <= App.max_int_str_len) {
						
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
								    	lblNewLabel.setText("<html>In order to define a Blue Node name, you are allowed to enter only digit numbers from 0 to 9, lower case letters form a to z and upper dash '-' or lower dash '_' special characters</html>");
								    	q.closeQueries();
								    	return;
								    }
									q.insertEntryBluenodes(givenBluenodeName, userid);
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
						
						App.window.updateDatabaseGUI();
						frmEditBluenodeEntry.dispose();
					
					} else {
						lblNewLabel.setText("<html>Please provide a Hostname up to "+App.max_str_len_small_size+" characters and a number up to "+App.max_int_str_len+" digits.</html>");
					}
				} else {
					lblNewLabel.setText("<html>Please fill in all the fields.</html>");
				}
			}
		});
		button.setBounds(249, 228, 175, 23);
		frmEditBluenodeEntry.getContentPane().add(button);
		
		lblNewLabel = new JLabel("Please correct your mistakes");
		lblNewLabel.setForeground(new Color(204, 0, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(10, 105, 414, 110);
		frmEditBluenodeEntry.getContentPane().add(lblNewLabel);
	}
}
