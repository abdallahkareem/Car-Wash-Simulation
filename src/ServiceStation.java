import java.util.*;

public class ServiceStation {

	private Queue<Car> queue;
	private Semaphore empty;
	private Semaphore full;
    private Semaphore mutex;
    private Semaphore pumps;

    private List<Pump> pumpThreads;

    private int numPumps;
    private int queueSize;
    

    public ServiceStation(int numPumps, int queueSize) {
        if (numPumps < 1) {
            throw new IllegalArgumentException("Number of Pumps must be greater than or equal 1!");
        }
        if (queueSize < 1 || queueSize > 10) {
            throw new IllegalArgumentException("Queue size must be between 1 and 10!");
        }

        this.queueSize = queueSize;
        this.numPumps = numPumps;

        queue = new LinkedList<>(); 

        empty = new Semaphore(queueSize);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
        pumps = new Semaphore(numPumps);
        
        pumpThreads = new ArrayList<>();
    }


    public void startSimulation() {
        for(int i= 1 ; i <= numPumps ; i++) {
        	Pump pump = new Pump(i,queue,empty,full,mutex,pumps);
        	pumpThreads.add(pump);
        	pump.start();
        }
        
        for(int i= 1 ; i <= queueSize ; i++) {
        	Car car = new Car(i,queue,empty,full,mutex);
        	car.start();
            try{
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        }
        
    }

    // ===== Main Entry Point =====
    public static void main(String[] args) {
    	ServiceStation ss = new ServiceStation(5,10);
    	ss.startSimulation();
    }
}
