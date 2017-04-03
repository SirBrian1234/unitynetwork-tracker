package kostiskag.unitynetwork.tracker.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class editBluenode {

	private JFrame frmEditBluenodeEntry;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private int type;
	private int bluenodeId;

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
		
		JButton button = new JButton("Update entry");
		button.setBounds(327, 228, 97, 23);
		frmEditBluenodeEntry.getContentPane().add(button);
	}

}
