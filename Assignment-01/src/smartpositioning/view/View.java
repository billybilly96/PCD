package smartpositioning.view;

import smartpositioning.controller.Controller;
import smartpositioning.model.Model;
import smartpositioning.model.particle.Particle;
import smartpositioning.util.Utility;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.*;

public class View extends JFrame implements ActionListener{

    private static final long serialVersionUID = 1L;
    private VisualiserPanel panel;
    private JButton btnStart;
    private JButton btnStop;
    private JTextField stepField;
    private JTextField nThreadField;
    private JTextField timeField;
    private Controller controller;

    public View(Model model) {
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        int size = (w < h) ? w / 8 * 7 : h / 8 * 7;
        setSize(size, size);
        setTitle("Smart Positioning");
        setResizable(false);
        setLocationRelativeTo(null);
        this.panel = new VisualiserPanel(size, size);
        getContentPane().add(this.panel);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(-1);
            }
            public void windowClosed(WindowEvent ev) {
                System.exit(-1);
            }
        });
        this.btnStart = new JButton("Start");
        this.btnStop = new JButton("Stop");
        this.btnStart.addActionListener(this);
        this.btnStop.addActionListener(this);
        JPanel panelNord = new JPanel();
        this.stepField = new JTextField("Step: 0");
        this.stepField.setPreferredSize(new Dimension(size / 8, size / 30));
        this.stepField.setEditable(false);
        this.nThreadField = new JTextField("Thread: " + model.getNumWorkers());
        this.nThreadField.setPreferredSize(new Dimension(size / 8, size / 30));
        this.nThreadField.setEditable(false);
        this.timeField = new JTextField("Time: 0 ms");
        this.timeField.setPreferredSize(new Dimension(size / 7, size / 30));
        this.timeField.setEditable(false);
        panelNord.add(this.btnStart);
        panelNord.add(this.btnStop);
        panelNord.add(this.stepField);
        panelNord.add(this.nThreadField);
        panelNord.add(this.timeField);
        getContentPane().add(panelNord, BorderLayout.NORTH);
        panel.updatePositions(model.getParticles());
    }

    public void update(java.util.List<Particle> particles, int step, long time) {
        this.panel.updatePositions(particles);
        this.stepField.setText("Step: " + (step + 1));
        this.timeField.setText("Time: " + time + " ms");
    }

    public void setTime(long time) {
        this.timeField.setText("Time: " + time + " ms");
    }

    public void setNumThread(int n) {
        this.nThreadField.setText("Thread: " + n);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == this.btnStart) {
            this.controller.start();
        } else {
            this.controller.stop();
        }
    }

    public void addController(Controller c) {
        this.controller = c;
    }

    public static class VisualiserPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private java.util.List<Particle> particles;
        private long dx;
        private long dy;

        public VisualiserPanel(int w, int h) {
            setSize(w, h);
            this.dx = w;
            this.dy = h;
        }

        public void paint(final Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.clearRect(0, 0, getWidth(), getHeight());
            synchronized (this) {
                if (this.particles != null) {
                    this.particles.forEach(p -> {
                        int x0 = (int)(this.dx * (p.getPosition().getX() - Utility.MIN_POS_X) / (Utility.MAX_POS_X - Utility.MIN_POS_X));
                        int y0 = (int)(this.dy * (p.getPosition().getY() - Utility.MIN_POS_Y) / (Utility.MAX_POS_Y - Utility.MIN_POS_Y));
                        double mass = (p.getMass() * 110);
                        int color = (int)(255 * (p.getAlpha() - Utility.MIN_ALPHA) / (Utility.MAX_ALPHA - Utility.MIN_ALPHA));
                        g2.fillOval(x0, y0, (int)mass, (int)mass);
                        g2.setColor(new Color(color));
                    });
                }
            }
        }

        public void updatePositions(final List<Particle> particles) {
            synchronized (this) {
                this.particles = particles;
            }
            repaint();
        }

    }

}
