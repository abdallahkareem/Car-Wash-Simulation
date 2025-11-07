import java.util.*;

public class ServiceStation {

    private Queue<Car> queue;
    private Semaphore empty, full, mutex, pumps;
    private List<Pump> pumpThreads;
    private ServiceStationGUI gui;

    private int numPumps, queueSize;
    private boolean running = false;
    private CarGenerator generator;

    public ServiceStation(int numPumps, int queueSize) {
        this.numPumps = Math.max(1, numPumps);
        this.queueSize = Math.max(1, queueSize);

        queue = new LinkedList<>();
        empty = new Semaphore(this.queueSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
        pumps = new Semaphore(this.numPumps);

        pumpThreads = new ArrayList<>();

        // Create ONE GUI instance here and reuse it
        gui = new ServiceStationGUI(this.numPumps);

        // Hook start/stop to simulation methods
        gui.setStartAction(e -> startSimulation());
        gui.setStopAction(e -> {
            stopSimulation();
            // Restart behavior in GUI already implemented as "Restart" button calling stopAction then startAction
            // We simply stop; startSimulation will be invoked by the GUI flow if required by GUI logic.
        });
    }

    private class CarGenerator extends Thread {
        private int carId = 1;
        private volatile boolean running = true;
        private final int maxCars;

        public CarGenerator(int maxCars) {
            this.maxCars = Math.max(1, maxCars);
        }

        public void stopRunning() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            while (running && carId <= maxCars) {
                Car car = new Car(carId++, queue, empty, full, mutex, gui);
                car.start();
                try {
                    Thread.sleep(600); // arrival interval
                } catch (InterruptedException e) {
                    break;
                }
            }
            gui.appendLog("All " + maxCars + " cars have been generated.");
        }
    }

    public synchronized void startSimulation() {
        if (running) return;
        running = true;

        // Read GUI-selected values (user may have changed them before clicking Start)
        int selectedPumps = gui.getSelectedNumPumps();
        int selectedCars = gui.getSelectedMaxCars();

        // Update count and rebuild the pump display in the SAME GUI (no new window)
        this.numPumps = Math.max(1, selectedPumps);
        gui.rebuildPumpPanels(this.numPumps); // this only updates UI; does not create a new ServiceStationGUI

        // reset and rebuild semaphores/state
        pumps = new Semaphore(numPumps);
        empty = new Semaphore(queueSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);

        // reset queue and threads
        queue.clear();
        pumpThreads.clear();

        // start pump threads
        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, queue, empty, full, mutex, pumps, gui);
            pumpThreads.add(pump);
            pump.start();
        }

        // start car generator with selectedCars
        generator = new CarGenerator(selectedCars);
        generator.start();

        gui.updateQueueDisplay(queue);
        gui.appendLog("Simulation started with " + numPumps + " pumps and " + selectedCars + " cars.");
    }

    public synchronized void stopSimulation() {
        if (!running) return;
        running = false;

        // stop generator
        if (generator != null) {
            generator.stopRunning();
            generator = null;
        }

        // stop pumps
        for (Pump pump : pumpThreads) {
            if (pump != null) pump.stopRunning();
        }
        pumpThreads.clear();

        // clear queue and update GUI
        queue.clear();
        gui.updateQueueDisplay(queue);
        gui.appendLog("Simulation stopped.");
    }

    public static void main(String[] args) {
        // Keep behavior unchanged: create ServiceStation which creates its GUI once
        new ServiceStation(5, 10);
    }
}
