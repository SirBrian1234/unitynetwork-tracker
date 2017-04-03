package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.omg.PortableServer.ServantRetentionPolicyValue;

import javax.swing.JComboBox;
import javax.swing.JButton;

public class editHostname {

	private JFrame frmEditHostnameEntry;
	private JTextField textField;
	private JTextField textField_2;
	private JTextField textField_1;
	int type;
	int hostnameId;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					editHostname window = new editHostname(0,0);
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
	public editHostname(int type, int hostnameId) {
		this.type = type;
		this.hostnameId = hostnameId;
		initialize();
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
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(76, 62, 257, 20);
		frmEditHostnameEntry.getContentPane().add(textField_2);
		
		JButton button = new JButton("Update entry");
		button.setBounds(327, 228, 97, 23);
		frmEditHostnameEntry.getContentPane().add(button);
		
		JLabel lblUserId = new JLabel("user id");
		lblUserId.setBounds(10, 114, 56, 14);
		frmEditHostnameEntry.getContentPane().add(lblUserId);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(76, 111, 75, 20);
		frmEditHostnameEntry.getContentPane().add(textField_1);
	}

}
