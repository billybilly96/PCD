package smartpositioning.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Application view
 */
public class View extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static List<InputListener> listeners;
	private VisualiserPanel panel;
	private JButton btnStart;
	private JButton btnStep;
	private JButton btnPause;
	private JButton btnStop;
	private JButton btnRemove;
	private JTextField step;
	private JTextField nParticles;
	private int size;

	public View() {
		listeners = new ArrayList<InputListener>();
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		size = (w < h) ? (int) (w / 1.2) : (int) (h / 1.2);
		setSize(size, size);
		setTitle("Smart Positioning");
		setResizable(false);
		panel = new VisualiserPanel(size, size);
		panel.setBackground(Color.WHITE);
		getContentPane().add(panel);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
			}

			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		JPanel panelNord = new JPanel();
		JPanel panelNord2 = new JPanel();
		panelNord.setBackground(Color.WHITE);
		Dimension btnDimension = new Dimension(size / 6, size / 25);
		Dimension textFieldDimension = new Dimension(size / 4, size / 25);
		btnStart = new JButton("Start");
		btnStep = new JButton("OneStep");
		btnPause = new JButton("Pause");
		btnStop = new JButton("Stop");
		btnRemove = new JButton("- Particle");
		btnStart.addActionListener(this);
		btnStep.addActionListener(this);
		btnPause.addActionListener(this);
		btnStop.addActionListener(this);
		btnRemove.addActionListener(this);
		btnStart.setPreferredSize(btnDimension);
		btnStep.setPreferredSize(btnDimension);
		btnPause.setPreferredSize(btnDimension);
		btnStop.setPreferredSize(btnDimension);
		btnRemove.setPreferredSize(btnDimension);
		step = new JTextField();
		step.setPreferredSize(textFieldDimension);
		step.setEditable(false);
		step.setFont(new Font("Tahoma", Font.PLAIN, size / 40));
		step.setHorizontalAlignment(JTextField.CENTER);	
		nParticles = new JTextField();
		nParticles.setPreferredSize(textFieldDimension);
		nParticles.setEditable(false);
		nParticles.setFont(new Font("Tahoma", Font.PLAIN, size / 40));
		nParticles.setHorizontalAlignment(JTextField.CENTER);		
		panelNord.add(btnStart);
		panelNord.add(btnStep);
		panelNord.add(btnPause);
		panelNord.add(btnStop);
		panelNord.add(btnRemove);
		panelNord2.add(step);
		panelNord2.add(nParticles);
		getContentPane().add(panelNord, BorderLayout.NORTH);
		getContentPane().add(panelNord2, BorderLayout.AFTER_LAST_LINE);
	}

	public void addListener(InputListener l) {
		listeners.add(l);
	}

	public void update(List<Particle> particles, int currentStep) {
		panel.updatePositions(particles);
		step.setText("Step: " + currentStep);
		nParticles.setText("N. Particles: " + particles.size());
	}

	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();
		if (cmd.equals("Start")) {
			for (InputListener l : listeners) {
				l.start();
			}
		} else if (cmd.equals("OneStep")) {
			for (InputListener l : listeners) {
				l.step();
			}
		} else if (cmd.equals("Pause")) {
			for (InputListener l : listeners) {
				l.pause();
			}
		} else if (cmd.equals("Stop")) {
			for (InputListener l : listeners) {
				l.stop();
			}
		} else if (cmd.equals("- Particle")) {
			for (InputListener l : listeners) {
				l.remove();
			}
		}
	}

	public static class VisualiserPanel extends JPanel implements MouseListener {

		private static final long serialVersionUID = 1L;
		private List<Particle> particles;
		private int w;
		private int h;

		public VisualiserPanel(int width, int height) {
			particles = new ArrayList<>();
			w = width;
			h = height;
			setSize(w, h);
			addMouseListener(this);
		}

		public void paint(final Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.clearRect(0, 0, getWidth(), getHeight());
			synchronized (this) {
				if (particles != null) {
					particles.forEach(p -> {
						int x0 = mapDimensionX(p.getPosition().getX());
						int y0 = mapDimensionY(p.getPosition().getY());
						g2.fillOval(x0, y0, mapMassIntoDimension(w, p.getMass()), mapMassIntoDimension(h, p.getMass()));
					});
				}
			}
		}

		public void updatePositions(final List<Particle> p) {
			synchronized (this) {
				particles = p;
			}
			repaint();
		}

		private int mapDimensionX(double dimension) {
			return (int) (w * (dimension - ParticlesUtility.MIN_POS_X) / (ParticlesUtility.MAX_POS_X - ParticlesUtility.MIN_POS_X));
		}

		private int mapDimensionY(double dimension) {
			return (int) (h * (dimension - ParticlesUtility.MIN_POS_Y) / (ParticlesUtility.MAX_POS_Y - ParticlesUtility.MIN_POS_Y));
		}

		private int mapMassIntoDimension(double dimension, double mass) {
			double factor = ((80 - 180) * (mass - ParticlesUtility.MIN_MASS) / (ParticlesUtility.MAX_MASS - ParticlesUtility.MIN_MASS)) + 180;
			return (int) (dimension / factor);
		}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			double x = ParticlesUtility.MIN_POS_X + (((ParticlesUtility.MAX_POS_X - ParticlesUtility.MIN_POS_X) * e.getX()) / w);
			double y = ParticlesUtility.MIN_POS_Y + (((ParticlesUtility.MAX_POS_Y - ParticlesUtility.MIN_POS_Y) * e.getY()) / h);
			for (InputListener l : listeners) {
				l.add(x, y);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {}

	}

}