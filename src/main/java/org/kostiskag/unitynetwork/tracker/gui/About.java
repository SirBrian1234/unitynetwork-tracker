package org.kostiskag.unitynetwork.tracker.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;

/**
 * This class builds a gui window to show the about page
 * of the project
 *
 * @author Konstantinos Kagiampakis
 */
public class About {

	private static About ABOUT;
	private JFrame frmAbout;

	/**
	 * This class is a singleton
	 */
	public static About instanceOf() {
		if (About.ABOUT == null) {
			About.ABOUT = new About();
		}
		return About.ABOUT;
	}

	private About() {
		initialize();
		frmAbout.setVisible(true);
	}
	
	public boolean isVisible() {
		return frmAbout.isVisible();
	}

	public void hide() {
		frmAbout.setVisible(false);
	}

	public void show() {
		frmAbout.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAbout = new JFrame();
		frmAbout.setTitle("About");
		frmAbout.setBounds(100, 100, 570, 568);
		frmAbout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmAbout.getContentPane().setLayout(null);
		
		JLabel lblUnityNetwork = new JLabel(""
				+ "<html>"
				+ "<h1>Unity Network</h1>"
				+ "</html>");
		
		lblUnityNetwork.setBounds(10, 11, 534, 60);
		frmAbout.getContentPane().add(lblUnityNetwork);
		
		JLabel lblNewLabel = new JLabel("<html>Unity Network is a virtual network (VPN) capable to be deployed in any kind of IP network as a LAN network or over the Internet."
    + "<ul>"
    + "<li>It is based on a divide and conquer logic with distributed roles, behavior and decupoled network traffic from the network logic which allows it to serve a large number of host-clients from many platforms.</li><br>"
    + "<li>It is A VPN based in software rather than hardware wich provides enchanced resilience and easy deployment to any kind of platform which may support Java.</li><br>"
    + "<li>The software was build as part of my BSc Thesis in order to demonstare a live and tangible example of a better version of today's Internet. Inside the network, users may experience a much more vivid communication, the ability to share any kind of data or services between them and the ability to know each other.</li>"
    + "</ul>"
+"</html>");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setBounds(10, 82, 534, 208);
		frmAbout.getContentPane().add(lblNewLabel);
		
		JLabel lblThe = new JLabel("<html>In order to learn more about the platform or download and use it you may visit the platform's application repositories as shown below:<ul><li>unitynetwork-tracker <a href=\"https://github.com/kostiskag/unitynetwork-tracker\">https://github.com/kostiskag/unitynetwork-tracker</a></li><li>unitynetwork-bluenode <a href=\"https://github.com/kostiskag/unitynetwork-bluenode\">https://github.com/kostiskag/unitynetwork-bluenode</a></li><li>unitynetwork-rednode <a href=\"https://github.com/kostiskag/unitynetwork-rednode\">https://github.com/kostiskag/unitynetwork-rednode</a></li></ul>The present application is a Unity Network Tracker</html>");
		lblThe.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblThe.setBounds(10, 301, 534, 133);
		frmAbout.getContentPane().add(lblThe);
		
		JLabel lblNewLabel_1 = new JLabel("Created by Konstantinos Kagiampakis");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(10, 445, 308, 14);
		frmAbout.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("<html>"
				+ "The project's article and source code are licensed under Creative Commons Atribution 4.0 International: <a href=\"https://creativecommons.org/licenses/by/4.0/\">https://creativecommons.org/licenses/by/4.0/</a>"
				+ "<html>");
		lblNewLabel_2.setBounds(10, 470, 534, 49);
		frmAbout.getContentPane().add(lblNewLabel_2);
	}

//	//  launch the application
//	//  I should move this in a test
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					About window = new About();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
}
