package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class editUser {

	private JFrame frmEditUserEntry;
	private JTextField textField;
	private JLabel lblUsername;
	private JTextField textField_1;
	private JLabel lblPassword;
	private JTextField textField_2;
	private JLabel lblType;
	private JTextField textField_3;
	private int type;
	private int userId;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					editUser window = new editUser(0,0);
					window.frmEditUserEntry.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public editUser(int type, int userId) {
		//type is 
		//0 for update
		//1 for new entry
		this.type = type;
		this.userId = userId;
		initialize();
		frmEditUserEntry.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmEditUserEntry = new JFrame();
		frmEditUserEntry.setResizable(false);
		frmEditUserEntry.setTitle("Edit user entry");
		frmEditUserEntry.setBounds(100, 100, 450, 300);
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
		lblUsername.setBounds(10, 58, 56, 14);
		frmEditUserEntry.getContentPane().add(lblUsername);
		
		textField_1 = new JTextField();
		textField_1.setBounds(76, 55, 128, 20);
		frmEditUserEntry.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		lblPassword = new JLabel("password");
		lblPassword.setBounds(10, 99, 56, 14);
		frmEditUserEntry.getContentPane().add(lblPassword);
		
		textField_2 = new JTextField();
		textField_2.setBounds(76, 96, 257, 20);
		frmEditUserEntry.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		lblType = new JLabel("type");
		lblType.setBounds(10, 141, 46, 14);
		frmEditUserEntry.getContentPane().add(lblType);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"user", "robot", "organisation/company ", "system"}));
		comboBox.setBounds(76, 138, 134, 20);
		frmEditUserEntry.getContentPane().add(comboBox);
		
		JLabel lblFullName = new JLabel("full name");
		lblFullName.setBounds(10, 183, 56, 14);
		frmEditUserEntry.getContentPane().add(lblFullName);
		
		textField_3 = new JTextField();
		textField_3.setColumns(10);
		textField_3.setBounds(76, 180, 257, 20);
		frmEditUserEntry.getContentPane().add(textField_3);
		
		JButton btnNewButton = new JButton("Update entry");
		btnNewButton.setBounds(327, 228, 97, 23);
		frmEditUserEntry.getContentPane().add(btnNewButton);
	}
}
