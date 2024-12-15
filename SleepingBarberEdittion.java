import java.util.concurrent.*;
import java.util.logging.*;

public class SleepingBarberEdittion {
    // Constants
    private static final int NUM_BARBERS = 3;
    private static final int NUM_CHAIRS = 5;
    private static final int MAX_CUSTOMERS = 20;

    // Semaphores for synchronization
    private static final Semaphore customers = new Semaphore(0);
    private static final Semaphore barbers = new Semaphore(0);
    private static final Semaphore accessWaitingChairs = new Semaphore(1);

    // Tracking variables
    private static int waitingCustomers = 0;
    private static volatile int servedCustomers = 0;
    private static volatile int leftCustomers = 0;
    private static final Logger logger = Logger.getLogger(SleepingBarberEdittion.class.getName());

    public static void main(String[] args) {
        logger.info("Barbershop is open!");

        // Create barber threads
        for (int i = 1; i <= NUM_BARBERS; i++) {
            new Thread(new Barber(i)).start();
        }

        // Create customer threads
        for (int i = 1; i <= MAX_CUSTOMERS; i++) {
            try {
                Thread.sleep((int) (Math.random() * 2000)); // Random arrival time
                new Thread(new Customer(i)).start();
            } catch (InterruptedException e) {
                logger.severe("Error in customer arrival: " + e.getMessage());
            }
        }
    }

    static class Barber implements Runnable {
        private final int barberId;

        public Barber(int id) {
            this.barberId = id;
        }

        @Override
        public void run() {
            while (servedCustomers < MAX_CUSTOMERS) {
                try {
                    customers.acquire(); // Wait for a customer
                    accessWaitingChairs.acquire(); // Access waiting chairs
                    if (servedCustomers >= MAX_CUSTOMERS) {
                        accessWaitingChairs.release();
                        break;
                    }
                    waitingCustomers--;
                    servedCustomers++;
                    logger.info("Barber " + barberId + " is taking a customer. Waiting customers: " + waitingCustomers);
                    accessWaitingChairs.release(); // Release access to waiting chairs
                    barbers.release(); // Barber is now busy
                    cutHair();
                } catch (InterruptedException e) {
                    logger.severe("Error in barber thread: " + e.getMessage());
                }
            }
            logger.info("Barber " + barberId + " is done for the day.");
        }

        private void cutHair() {
            try {
                logger.info("Barber " + barberId + " is cutting hair.");
                Thread.sleep((int) (Math.random() * 3000)); // Simulate haircut time
                logger.info("Barber " + barberId + " has finished cutting hair.");
            } catch (InterruptedException e) {
                logger.severe("Error during haircut: " + e.getMessage());
            }
        }
    }

    static class Customer implements Runnable {
        private final int customerId;

        public Customer(int id) {
            this.customerId = id;
        }

        @Override
        public void run() {
            try {
                accessWaitingChairs.acquire(); // Try to get a chair
                if (waitingCustomers < NUM_CHAIRS) {
                    waitingCustomers++;
                    logger.info("Customer " + customerId + " is waiting. Waiting customers: " + waitingCustomers);
                    customers.release(); // Notify a barber
                    accessWaitingChairs.release(); // Release access to waiting chairs
                    barbers.acquire(); // Wait for a barber
                    getHaircut();
                } else {
                    leftCustomers++;
                    logger.warning("Customer " + customerId + " leaves as no chairs are available.");
                    accessWaitingChairs.release(); // Release access to waiting chairs
                }
            } catch (InterruptedException e) {
                logger.severe("Error in customer thread: " + e.getMessage());
            }
        }

        private void getHaircut() {
            logger.info("Customer " + customerId + " is getting a haircut.");
        }
    }
}