class OrderQueue {
    private java.util.LinkedList<String> queue = new java.util.LinkedList<>();
    private final int CAPACITY = 5;
    private boolean finished = false;

    public synchronized void addOrder(String order) throws InterruptedException {
        while (queue.size() == CAPACITY) {
            wait();
        }
        queue.add(order);
        System.out.println("Order Added: " + order);
        notifyAll(); // wake consumer
    }

    public synchronized String removeOrder() throws InterruptedException {
        while (queue.isEmpty()) {
            if (finished) return null;
            wait();
        }

        String order = queue.remove();
        System.out.println("Order Processed: " + order);
        notifyAll();
        return order;
    }

    public synchronized void setFinished() {
        finished = true;
        notifyAll();
    }
}


class CustomerOrderProducer extends Thread {
    private OrderQueue queue;
    private int maxOrders;

    public CustomerOrderProducer(OrderQueue queue, int maxOrders) {
        this.queue = queue;
        this.maxOrders = maxOrders;
    }

    public void run() {
        try {
            for (int i = 1; i <= maxOrders; i++) {
                queue.addOrder("Order-" + i);
                Thread.sleep(500);
            }
            queue.setFinished();
        } catch (InterruptedException e) {
            System.out.println("Producer interrupted.");
        }
    }
}


class KitchenOrderConsumer extends Thread {
    private OrderQueue queue;

    public KitchenOrderConsumer(OrderQueue queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            while (true) {
                String order = queue.removeOrder();
                if (order == null) break;
                Thread.sleep(800);
            }
            System.out.println("Consumer completed all orders.");
        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted.");
        }
    }
}


class OrderLogger extends Thread {
    private Thread producer;
    private Thread consumer;

    public OrderLogger(Thread producer, Thread consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public void run() {
        try {
            producer.join();
            consumer.join();
            System.out.println("Logger: All orders processed successfully.");
        } catch (InterruptedException e) {
            System.out.println("Logger interrupted.");
        }
    }
}


public class Main {
    public static void main(String[] args) {
        int totalOrders = 10;
        OrderQueue queue = new OrderQueue();
        CustomerOrderProducer producer = new CustomerOrderProducer(queue, totalOrders);
        KitchenOrderConsumer consumer = new KitchenOrderConsumer(queue);
        OrderLogger logger = new OrderLogger(producer, consumer);

        producer.start();
        consumer.start();
        logger.start();
    }
}
