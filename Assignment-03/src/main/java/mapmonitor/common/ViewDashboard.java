package mapmonitor.common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import akka.actor.ActorRef;
import assignment.utility.Pair;
import assignment.utility.Point2d;
import mapmonitor.messages.PressedPatchMsg;
import mapmonitor.rmi.DashboardPressed;

public class ViewDashboard extends JFrame {

	private static final long serialVersionUID = 1L;
	private JFrame controlFrame;
	private VisualiserPanel panel;
	private List<VisualiserPanel> panels;
	private List<JLabel> labelsInfoPatch;
	private JPanel mapPanel;
	private int m, n, w, h;
	
	public ViewDashboard(ActorRef actorDashboard, int mVal, int nVal) {
		this(mVal, nVal);
		for (int i = 0; i < m * n; i++) {
			panel = new VisualiserPanel(actorDashboard, i + 1, w / m, h / n, m, n);
			mapPanel.add(panel);
			panels.add(panel);
		}
	}
	
	public ViewDashboard(DashboardPressed pressedDashboard, int mVal, int nVal) {
		this(mVal, nVal);
		for (int i = 0; i < m * n; i++) {
			panel = new VisualiserPanel(pressedDashboard, i + 1, w / m, h / n, m, n);
			mapPanel.add(panel);
			panels.add(panel);
		}
	}
		
	private ViewDashboard(int mVal, int nVal) {
		m = mVal;
		n = nVal;
		panels = new ArrayList<>();
		labelsInfoPatch = new ArrayList<>();
		w = (int) (Toolkit.getDefaultToolkit().getScreenSize().width / 1.5);
		h = (int) (Toolkit.getDefaultToolkit().getScreenSize().height / 1.5);
		setSize(w, h);
		setTitle("Dashboard Monitor");
		setResizable(false);
		controlFrame = new JFrame();
		controlFrame.setSize((int) (w / 3), h);
		controlFrame.setTitle("Dashboard Monitor");
		controlFrame.setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		controlFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		mapPanel = new JPanel();
		mapPanel.setBackground(Color.black);
		mapPanel.setSize(w, h);
		mapPanel.setLayout(new GridLayout(n, m, 1, 1));
		JPanel controlPanel = new JPanel();
		JScrollPane scroll = new JScrollPane(controlPanel);
		controlPanel.setLayout(new GridLayout(m * n, 1, 1, 1));
		controlPanel.setBackground(Color.black);
		int panelWidth = (int) (w / 0.25);
		controlPanel.setSize(panelWidth, h);
		for (int i = 0; i < m * n; i++) {
			JPanel panInfoPatch = new JPanel();
			JLabel labelInfoPatch = new JLabel();
			labelsInfoPatch.add(labelInfoPatch);
			panInfoPatch.add(labelInfoPatch);
			controlPanel.add(panInfoPatch);
		}
		controlFrame.add(scroll);
		getContentPane().add(mapPanel);
	}

	public void updateSensors(List<Pair<Integer, Point2d>> sensors) {
		for (int i = 0; i < m * n; i++) {
			updateSensorInPatch(panels.get(i), sensors, i + 1);
		}
	}
	
	private void updateSensorInPatch(VisualiserPanel panel, List<Pair<Integer, Point2d>> allSensors, int patch) {		
		panel.updatePositions(allSensors.stream().filter(pair -> pair.getX() == patch).map(pair -> pair.getY()).collect(Collectors.toList()));
	}
	
	public void updateGuardians(List<Pair<Integer, StateGuardian>> guardians) {
		for (int i = 0; i < m * n; i++) {
			int patch = i + 1;
			List<StateGuardian> patchGuardians = guardians.stream().filter(g -> g.getX() == patch).map(p -> p.getY()).collect(Collectors.toList());			
			boolean patchInDanger = patchGuardians.stream().anyMatch(g -> g.equals(StateGuardian.DANGER));
			String patchDescription = "<html> Patch: " + patch + "<br>";
			patchDescription += "Numero Totale Guardiani: " + patchGuardians.size() + "<br>";
			patchDescription += "Numero Guardiani Allerta: " + patchGuardians.stream().filter(g -> g.equals(StateGuardian.DANGER)).count() + "<br>";
			patchDescription += "Numero Guardiani Pre-Allerta: " + patchGuardians.stream().filter(g -> g.equals(StateGuardian.WARNING)).count() + "<br>";
			patchDescription += "Numero Guardiani Normali: " + patchGuardians.stream().filter(g -> g.equals(StateGuardian.NORMAL)).count() + "<br>";			
			patchDescription += "Stato del Patch: " + (patchInDanger ? "Allerta" : "Normale") + "<br>";
			patchDescription += "</html>";
			labelsInfoPatch.get(i).setText(patchDescription);
			if (patchInDanger) {
				panels.get(i).setBorder(BorderFactory.createLineBorder(Color.RED, 3));
			} else {
				panels.get(i).setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}

	@Override
	public void setVisible(boolean b) {
		controlFrame.setVisible(b);
		super.setVisible(b);
	}
	
	public static class VisualiserPanel extends JPanel implements MouseListener {

		private static final long serialVersionUID = 1L;
		private Optional<ActorRef> actorDashboard;
		private Optional<DashboardPressed> pressedDashboard;
		private int w, h;
		private int m, n;
		private List<Point2d> sensors;
		private int patch;

		public VisualiserPanel(ActorRef dashboard, int numPatch, int width, int height, int mVal, int nVal) {
			this(numPatch, width, height, mVal, nVal);
			actorDashboard = Optional.of(dashboard);
		}
		
		public VisualiserPanel(DashboardPressed dashboard, int numPatch, int width, int height, int mVal, int nVal) {
			this(numPatch, width, height, mVal, nVal);
			pressedDashboard = Optional.of(dashboard);
		}
		
		private VisualiserPanel(int numPatch, int width, int height, int mVal, int nVal) {
			actorDashboard = Optional.empty();
			pressedDashboard = Optional.empty();
			w = width;
			h = height;
			m = mVal;
			n = nVal;
			patch = numPatch;
			setSize(w, h);
			addMouseListener(this);
		}

		@Override
		public void paintComponent(final Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.clearRect(0, 0, getWidth(), getHeight());
			synchronized (this) {
				if (sensors != null) {
					sensors.forEach(s -> {				
						int x = mapDimensionX(s.getX());
						int y = mapDimensionY(s.getY());
						g2.fillOval(x, y, 8, 8);
					});
				}
			}
		}

		public void updatePositions(List<Point2d> pos) {
			synchronized (this) {
				sensors = pos;
			}
			repaint();
		}

		private int mapDimensionX(double value) {
			double mapped = ((w * m) * (value - UtilityValues.MAP_MIN_X)) / (UtilityValues.MAP_MAX_X - UtilityValues.MAP_MIN_X);			
			int offset = ((patch - 1) % m) * w;
			return (int) (mapped - offset);
		}

		private int mapDimensionY(double value) {
			double mapped = ((h * n) * (value - UtilityValues.MAP_MIN_Y)) / (UtilityValues.MAP_MAX_Y - UtilityValues.MAP_MIN_Y);
			int offset = (int) (((Math.ceil((double) patch / m) - 1)) * h);
			return (int) (mapped - offset);			
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent e) {
			if (actorDashboard.isPresent()) {
				actorDashboard.get().tell(new PressedPatchMsg(patch), ActorRef.noSender());
			}
			if (pressedDashboard.isPresent()) {
				pressedDashboard.get().registerPression(patch);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
	}
}
