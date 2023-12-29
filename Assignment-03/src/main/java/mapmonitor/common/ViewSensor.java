package mapmonitor.common;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import assignment.utility.Point2d;

public class ViewSensor extends JFrame {

	private static final long serialVersionUID = 1L;
	private int w, h;
	private JPanel sensorPanel;
	private JLabel numPatch;
	private JLabel coordinate;
	private JLabel value;

	public ViewSensor() {
		w = (int) (Toolkit.getDefaultToolkit().getScreenSize().width / 5);
		h = (int) (Toolkit.getDefaultToolkit().getScreenSize().height / 5);
		setSize(w, h);
		setTitle("Sensor Monitor");
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		sensorPanel = new JPanel();
		sensorPanel.setLayout(new GridLayout(3, 0));
		numPatch = new JLabel();
		coordinate = new JLabel();
		value = new JLabel();
		sensorPanel.add(numPatch);
		sensorPanel.add(coordinate);
		sensorPanel.add(value);
		getContentPane().add(sensorPanel);
	}

	public void update(int patch, Point2d position, double val) {
		numPatch.setText("Patch: " + patch);
		coordinate.setText("Posizione: " + position.toString());
		value.setText("Valore: " + new DecimalFormat("#0.00").format(val));
	}

}
