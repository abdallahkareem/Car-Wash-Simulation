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
        this.numPumps = numPumps;
        this.queueSize = queueSize;

        queue = new LinkedList<>();
        empty = new Semaphore(queueSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
        pumps = new Semaphore(numPumps);

        pumpThreads = new ArrayList<>();
        gui = new ServiceStationGUI(numPumps);

        gui.setStartAction(e -> startSimulation());
        gui.setStopAction(e -> {
            stopSimulation();
            startSimulation();
        });
    }

    private class CarGenerator extends Thread {
        private int carId = 1;
        private volatile boolean running = true;
        private final int maxCars = 10; // limit total cars

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


    public void startSimulation() {
        if (running) return;
        running = true;

        // reset state
        queue.clear();
        empty = new Semaphore(queueSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
        pumps = new Semaphore(numPumps);

        pumpThreads.clear();

        // start pumps
        for (int i = 1; i <= numPumps; i++) {
            Pump pump = new Pump(i, queue, empty, full, mutex, pumps, gui);
            pumpThreads.add(pump);
            pump.start();
        }

        // start continuous car generator
        generator = new CarGenerator();
        generator.start();

        gui.updateQueueDisplay(queue);
        gui.appendLog("Simulation started.");
    }

    public void stopSimulation() {
        running = false;

        // stop generator
        if (generator != null) {
            generator.stopRunning();
        }

        // stop pumps
        for (Pump pump : pumpThreads) pump.stopRunning();
        pumpThreads.clear();

        queue.clear();
        gui.updateQueueDisplay(queue);
        gui.appendLog("Simulation stopped.");
    }

    public static void main(String[] args) {
        new ServiceStation(5, 10);
    }
}
