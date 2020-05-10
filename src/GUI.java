import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A JFrame designed for the ThermalImageCategorization class
 * 
 * @author Wentao Yang
 */
public class GUI extends JFrame {
	JPanel jp = new JPanel();
	JTextPane jtp = new JTextPane();
	JTextField jt = new JTextField(30);
	JButton jb = new JButton("Enter");

	public GUI() {
		setTitle("GUI");
		setVisible(true);
		setSize(800, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Initial text to ask for directory
		JLabel jl2 = new JLabel("Please enter the thermal images folder directory here: ");
		jp.add(jl2);

		// Text field to enter directory
		jp.add(jt);

		// Button to confirm
		jp.add(jb);
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String directory = jt.getText(); // Directory input
				StringBuilder sb = new StringBuilder(); // String output
				ThermalImageCategorization test = new ThermalImageCategorization(directory);
				sb.append("Images checked: ");
				sb.append(test.returnAllStoredFiles());
				sb.append("\n\n");
				sb.append("Images with spots significantly different than surrounding areas: ");
				sb.append(test.check());
				jtp.setText(sb.toString());
				jtp.setPreferredSize(new Dimension(700, 300));
			}
		});

		// Text pane that prints output
		jp.add(jtp);

		// The JPanel
		add(jp);

	}

	public static void main(String[] args) {
		GUI temp = new GUI();
	}
}