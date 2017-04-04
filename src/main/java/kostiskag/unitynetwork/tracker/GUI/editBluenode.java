package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;

public class editBluenode {

	private JFrame frmEditBluenodeEntry;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private int type;
	private int bluenodeId;
	private JButton button;
	private JLabel lblNewLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					editBluenode window = new editBluenode(0,0);
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
	public editBluenode(int type, int bluenodeId) {
		this.type = type;
		this.bluenodeId = bluenodeId;
		initialize();
		lblNewLabel.setText("");
		
		//new
		if (type == 0) {
			this.button.setText("Add new entry");
		} else {
			this.button.setText("Update entry");
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
		
		JLabel label = new JLabel("id");
		label.setBounds(10, 14, 17, 14);
		frmEditBluenodeEntry.getContentPane().add(label);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setColumns(10);
		textField.setBounds(76, 11, 75, 20);
		frmEditBluenodeEntry.getContentPane().add(textField);
		
		JLabel lblName = new JLabel("name");
		lblName.setBounds(10, 65, 56, 14);
		frmEditBluenodeEntry.getContentPane().add(lblName);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(76, 62, 257, 20);
		frmEditBluenodeEntry.getContentPane().add(textField_1);
		
		JLabel label_2 = new JLabel("user id");
		label_2.setBounds(10, 114, 56, 14);
		frmEditBluenodeEntry.getContentPane().add(label_2);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(76, 111, 75, 20);
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
							lblNewLabel.setText("Please provide a proper number with digits from 0 to 9");
							return;
						}
						
						Queries q;
						try {
							q = new Queries();
							if (type == 0) {			
								q.insertEntryBluenodes(textField_1.getText(), userid);
							} else {
								q.updateEntryBluenodesWithId(bluenodeId, textField_1.getText(), userid);
							}
							q.closeQueries();
						} catch (SQLException e) {							
							if (e.getErrorCode() == 19) { 
								lblNewLabel.setText("The given bluenode name is already taken.");
								return;
						    } else { 
						    	e.printStackTrace();
						    }							
						}										
						
						App.window.updateDatabaseGUI();
						frmEditBluenodeEntry.dispose();
					
					} else {
						lblNewLabel.setText("Please provide a Hostname up to "+App.max_str_len_small_size+" characters and a number up to "+App.max_int_str_len+" digits.");
					}
				} else {
					lblNewLabel.setText("Please fill in all the fields.");
				}
			}
		});
		button.setBounds(281, 228, 143, 23);
		frmEditBluenodeEntry.getContentPane().add(button);
		
		lblNewLabel = new JLabel("Please correct your mistakes");
		lblNewLabel.setForeground(new Color(204, 0, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setBounds(10, 159, 414, 14);
		frmEditBluenodeEntry.getContentPane().add(lblNewLabel);
	}
}
