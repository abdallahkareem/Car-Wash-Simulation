import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServiceStationGUI
 *
 * Swing-based professional GUI for the ServiceStation simulator.
 *
 * API methods expected by your simulator code:
 *  - ServiceStationGUI(int numPumps)
 *  - void setStartAction(ActionListener)
 *  - void setStopAction(ActionListener)
 *  - void updatePumpStatus(int pumpId, boolean occupied, String text)
 *  - void updateQueueDisplay(Queue<?> queue)
 *  - void appendLog(String message)
 *  - void setServiceTimeMillis(int millis)
 *
 * Notes:
 *  - pumpId is 1-based (1..numPumps) to match your existing code.
 *  - Uses a default animated progress length of 3000 ms; adjust with setServiceTimeMillis.
 */
public class ServiceStationGUI extends JFrame {

    private int numPumps;       // remove final
    private PumpPanel[] pumpPanels;  // remove final
    private final DefaultListModel<String> queueListModel = new DefaultListModel<>();
    private final JList<String> queueList = new JList<>(queueListModel);
    private final JTextArea logArea = new JTextArea();
    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Restart");
    private final JSpinner pumpsSpinner;
    private volatile int serviceTimeMillis = 3000; // default
    private final AtomicInteger logCounter = new AtomicInteger(0);
    private final JSpinner carsSpinner; // <--- ADD THIS
    private JPanel pumpsGrid;

    public ServiceStationGUI(int numPumps) {
        super("Service Station Simulator");
        this.numPumps = Math.max(1, numPumps);
        this.pumpPanels = new PumpPanel[this.numPumps];

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Top control bar
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        topBar.setBorder(new EmptyBorder(8, 8, 0, 8));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        startButton.setPreferredSize(new Dimension(100, 32));
        stopButton.setPreferredSize(new Dimension(100, 32));
        stopButton.setEnabled(false);

        controls.add(startButton);
        controls.add(stopButton);

        // service time control
        JPanel serviceTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        serviceTimePanel.add(new JLabel("Service time (ms):"));
        SpinnerNumberModel serviceModel = new SpinnerNumberModel(serviceTimeMillis, 200, 20000, 100);
        JSpinner serviceSpinner = new JSpinner(serviceModel);
        serviceSpinner.setPreferredSize(new Dimension(100, 26));
        serviceSpinner.addChangeListener(e -> setServiceTimeMillis((Integer) serviceSpinner.getValue()));
        serviceTimePanel.add(serviceSpinner);

        // pumps count display (read-only for display)
        JPanel pumpsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pumpsPanel.add(new JLabel("Pumps:"));
        pumpsSpinner = new JSpinner(new SpinnerNumberModel(this.numPumps, 1, 100, 1));
        pumpsSpinner.setEnabled(true);
        pumpsPanel.add(pumpsSpinner);

        // cars count selector
        JPanel carsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        carsPanel.add(new JLabel("Cars:"));
        carsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1)); // default 10 cars
        carsSpinner.setPreferredSize(new Dimension(80, 26));
        carsPanel.add(carsSpinner);

// Add to topBar under pumps section
        topBar.add(carsPanel, BorderLayout.SOUTH);



        topBar.add(controls, BorderLayout.WEST);
        topBar.add(serviceTimePanel, BorderLayout.CENTER);
        topBar.add(pumpsPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center: pumps grid and queue/log side by side
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(new EmptyBorder(0, 8, 8, 8));

        // Pumps grid
        pumpsGrid = new JPanel();
        int cols = Math.min(4, Math.max(1, this.numPumps));
        int rows = (int) Math.ceil((double) this.numPumps / cols);
        pumpsGrid.setLayout(new GridLayout(rows, cols, 12, 12));
        pumpsGrid.setBorder(BorderFactory.createTitledBorder("Pumps"));

        for (int i = 0; i < this.numPumps; i++) {
            PumpPanel p = new PumpPanel(i + 1);
            pumpPanels[i] = p;
            pumpsGrid.add(p);
        }

        centerPanel.add(pumpsGrid, BorderLayout.CENTER);

        // Right side: queue + log
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(8, 8));
        rightPanel.setPreferredSize(new Dimension(320, 0));

        // Queue visualization
        JPanel queuePanel = new JPanel(new BorderLayout(6, 6));
        queuePanel.setBorder(BorderFactory.createTitledBorder("Queue"));
        queueList.setVisibleRowCount(8);
        queueList.setFixedCellHeight(28);
        queueList.setCellRenderer(new QueueCellRenderer());
        JScrollPane qScroll = new JScrollPane(queueList);
        queuePanel.add(qScroll, BorderLayout.CENTER);

        // Buttons to clear log & queue
        JPanel qBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton clearQueueBtn = new JButton("Clear Queue");
        clearQueueBtn.addActionListener(e -> {
            queueListModel.clear();
            appendLog("Queue cleared by user.");
        });
        qBtns.add(clearQueueBtn);
        queuePanel.add(qBtns, BorderLayout.SOUTH);

        // Log area
        JPanel logPanel = new JPanel(new BorderLayout(6, 6));
        logPanel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        logArea.setEditable(false);
        logArea.setRows(10);
        logArea.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JPanel logBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> {
            logArea.setText("");
        });
        logBtns.add(clearLogBtn);
        logPanel.add(logBtns, BorderLayout.SOUTH);

        rightPanel.add(queuePanel, BorderLayout.NORTH);
        rightPanel.add(logPanel, BorderLayout.CENTER);

        centerPanel.add(rightPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Footer: status bar
        JPanel statusBar = new JPanel(new BorderLayout(6, 6));
        statusBar.setBorder(new EmptyBorder(0, 8, 8, 8));
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);

        // Hook up start/stop enabling
        startButton.addActionListener(e -> {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText("Running");
            if (startAction != null) startAction.actionPerformed(null);
        });
        stopButton.addActionListener(e -> {
            statusLabel.setText("Restarting...");

            // Ensure no clicks break logic state
            startButton.setEnabled(false);
            stopButton.setEnabled(false);

            // Run restart logic
            if (stopAction != null) stopAction.actionPerformed(null);

            // Re-enable buttons in running state
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText("Running");
        });


        // final UI polish
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        setVisible(true);
        appendLog("GUI initialized with " + this.numPumps + " pumps.");
    }

    // ----------------------------
    // Public API used by simulator
    // ----------------------------

    private ActionListener startAction;
    private ActionListener stopAction;

    public void setStartAction(ActionListener a) {
        this.startAction = a;
    }

    public void setStopAction(ActionListener a) {
        this.stopAction = a;
    }

    /**
     * Update pump status. pumpId is 1-based.
     * occupied: true when a car starts being serviced; false when finished.
     * text: short label like "Car 3" or "".
     */
    public void updatePumpStatus(int pumpId, boolean occupied, String text) {
        if (pumpId < 1 || pumpId > numPumps) return;
        PumpPanel p = pumpPanels[pumpId - 1];
        p.setOccupied(occupied, text);
        appendLog(String.format("Pump %d %s %s", pumpId, (occupied ? "occupied by" : "released from"), text));
    }

    /**
     * Update queue display from simulator's queue.
     * Accepts any Queue<?>; uses reflection to try to show getCarId() if available,
     * otherwise uses toString().
     */
    public void updateQueueDisplay(Queue<?> queue) {
        SwingUtilities.invokeLater(() -> {
            queueListModel.clear();
            if (queue == null || queue.isEmpty()) return;
            int idx = 1;
            for (Object o : queue) {
                String id = extractId(o);
                queueListModel.addElement(String.format("%02d - %s", idx++, id));
            }
        });
    }

    /**
     * Append message to activity log (thread-safe).
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String line = String.format("[%02d] %s\n", logCounter.incrementAndGet(), message);
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Adjust the progress animation duration used when a pump becomes occupied.
     */
    public void setServiceTimeMillis(int millis) {
        this.serviceTimeMillis = Math.max(200, millis);
    }

    // ----------------------------
    // Private helpers and UI components
    // ----------------------------

    private String extractId(Object o) {
        if (o == null) return "null";
        try {
            // Try common method names
            Method m = null;
            try { m = o.getClass().getMethod("getCarId"); } catch (NoSuchMethodException ignored) {}
            if (m == null) {
                try { m = o.getClass().getMethod("getId"); } catch (NoSuchMethodException ignored) {}
            }
            if (m != null) {
                Object val = m.invoke(o);
                return String.valueOf(val);
            }
        } catch (Exception ignored) {}
        return o.toString();
    }

    private class PumpPanel extends JPanel {
        private final int pumpId;
        private final JLabel titleLabel = new JLabel();
        private final StatusIcon statusIcon = new StatusIcon(18);
        private final JLabel currentCarLabel = new JLabel("Idle");
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        private Timer progressTimer;
        private long progressStart;
        private volatile boolean busy = false;

        public PumpPanel(int pumpId) {
            this.pumpId = pumpId;
            setLayout(new BorderLayout(8, 8));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            titleLabel.setText("Pump #" + pumpId);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

            // header
            JPanel header = new JPanel(new BorderLayout(6, 6));
            header.add(titleLabel, BorderLayout.WEST);
            header.add(statusIcon, BorderLayout.EAST);

            // center info
            JPanel info = new JPanel(new GridLayout(2, 1, 4, 4));
            currentCarLabel.setFont(currentCarLabel.getFont().deriveFont(12f));
            info.add(currentCarLabel);
            progressBar.setStringPainted(true);
            progressBar.setValue(0);
            info.add(progressBar);

            add(header, BorderLayout.NORTH);
            add(info, BorderLayout.CENTER);

            setOccupied(false, "");
        }

        /**
         * Set occupancy. When occupied==true it starts an animated progress filling up to 100% in serviceTimeMillis.
         */
        public void setOccupied(boolean occupied, String text) {
            SwingUtilities.invokeLater(() -> {
                if (occupied) {
                    busy = true;
                    statusIcon.setStatus(StatusIcon.Status.BUSY);
                    currentCarLabel.setText(text != null && !text.isEmpty() ? text : "Serving...");
                    startProgressAnimation();
                } else {
                    busy = false;
                    statusIcon.setStatus(StatusIcon.Status.FREE);
                    currentCarLabel.setText("Idle");
                    stopProgressAnimation();
                    progressBar.setValue(0);
                }
                repaint();
            });
        }

        private void startProgressAnimation() {
            stopProgressAnimation();
            progressBar.setValue(0);
            progressStart = System.currentTimeMillis();
            progressTimer = new Timer();
            final int updateMs = 40;
            progressTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    long elapsed = System.currentTimeMillis() - progressStart;
                    int pct = (int) Math.min(100, (elapsed * 100) / Math.max(1, serviceTimeMillis));
                    SwingUtilities.invokeLater(() -> progressBar.setValue(pct));
                    if (pct >= 100) {
                        stopProgressAnimation();
                    }
                }
            }, 0, updateMs);
        }

        private void stopProgressAnimation() {
            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer = null;
            }
        }
    }

    // Small circular status icon
    private static class StatusIcon extends JComponent {
        enum Status {FREE, BUSY}
        private Status status = Status.FREE;
        private final int diameter;

        public StatusIcon(int diameter) {
            this.diameter = diameter;
            setPreferredSize(new Dimension(diameter + 6, diameter + 6));
        }

        public void setStatus(Status s) {
            this.status = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = (status == Status.BUSY) ? new Color(0xD9534F) : new Color(0x5CB85C);
            Color border = fill.darker();

            int x = 3, y = 3;
            g2.setColor(fill);
            g2.fillOval(x, y, diameter, diameter);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(border);
            g2.drawOval(x, y, diameter, diameter);
            g2.dispose();
        }
    }

    // Custom queue cell renderer for nicer visuals
    private static class QueueCellRenderer extends JLabel implements ListCellRenderer<String> {
        public QueueCellRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(6, 8, 6, 8));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                      String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setFont(list.getFont());
            if (isSelected) {
                setBackground(new Color(0xE6F2FF));
                setForeground(Color.BLACK);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.DARK_GRAY);
            }
            return this;
        }
    }
    public int getServiceTimeMillis() {
        return serviceTimeMillis;
    }
    public int getSelectedNumPumps() {
        return (Integer) pumpsSpinner.getValue();
    }

    public int getSelectedMaxCars() {
        return (Integer) carsSpinner.getValue();
    }

    public void rebuildPumpPanels(int newPumpCount) {
        // update stored count
        this.numPumps = Math.max(1, newPumpCount);

        // clear existing UI panels
        if (pumpsGrid != null) {
            pumpsGrid.removeAll();
        } else {
            // safety: if pumpsGrid was null for some reason, find/create it
            pumpsGrid = new JPanel();
            // note: the constructor normally creates pumpsGrid, so this branch rarely runs
        }

        // reallocate panel array to the new size
        pumpPanels = new PumpPanel[this.numPumps];

        // compute grid and lay out new panels
        int cols = Math.min(4, Math.max(1, this.numPumps));
        int rows = (int) Math.ceil((double) this.numPumps / cols);
        pumpsGrid.setLayout(new GridLayout(rows, cols, 12, 12));
        pumpsGrid.setBorder(BorderFactory.createTitledBorder("Pumps"));

        for (int i = 0; i < this.numPumps; i++) {
            PumpPanel p = new PumpPanel(i + 1);
            pumpPanels[i] = p;
            pumpsGrid.add(p);
        }

        // refresh UI in EDT
        SwingUtilities.invokeLater(() -> {
            pumpsGrid.revalidate();
            pumpsGrid.repaint();
        });
    }

    public void updatePumpState(int pumpId, String label) {
        if (pumpPanels == null || pumpId < 1 || pumpId > pumpPanels.length) return;
        PumpPanel panel = pumpPanels[pumpId - 1];

        SwingUtilities.invokeLater(() -> {
            if (label == null || label.isEmpty() || label.equals("FREE")) {
                panel.setOccupied(false, "");
            } else {
                panel.setOccupied(true, label);
            }
        });
    }

}
