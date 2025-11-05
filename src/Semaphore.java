public class Semaphore {
    private int value;

    public Semaphore(int initial) {
        this.value = initial;
    }

    public synchronized void waitSemaphore() throws InterruptedException {
        value--;
        if (value < 0) {
            wait();
        }
    }

    public synchronized void signal() {
        value++;
        if (value <= 0) {
            notify();
        }
    }
}
