
import java.util.LinkedList; 
import java.util.Queue; 
import java.util.Scanner; 
// Custom exception for kitchen overload 
class KitchenOverloadException extends Exception { 
    public KitchenOverloadException(String message) { 
        super(message); 
    } 
} 
// Represents one food item 
class FoodOrder { 
    private final String item; 
    public FoodOrder(String item) { 
        this.item = item; 
    } 
    public String getItem() { 
        return item; 
    } 
} 
// Shared kitchen queue 
class KitchenOrderQueue { 
    private final Queue<FoodOrder> queue = new LinkedList<>(); 
    private final int capacity; 
    private boolean finished = false; 
    public KitchenOrderQueue(int capacity) { 
        this.capacity = capacity; 
    } 
    // Producer: waiter places an order 
    public synchronized void placeOrder(FoodOrder order) 
            throws InterruptedException, KitchenOverloadException { 
        while (queue.size() >= capacity && !finished) { 
            System.out.println( 
                    "Kitchen queue full. Waiter is waiting..."); 
            wait(); 
        } 
        if (finished) { 
            throw new KitchenOverloadException( 
                    "Kitchen is closed."); 
        } 
        queue.add(order); 
        System.out.println( 
                "Waiter-1 sent to kitchen: " + order.getItem()); 
        notifyAll(); 
    } 
    // Consumer: chef collects the next order 
    public synchronized FoodOrder cookNextOrder() 
            throws InterruptedException { 
        while (queue.isEmpty() && !finished) { 
            wait(); 
        } 
        if (queue.isEmpty() && finished) { 
 
            return null; 
        } 
        FoodOrder order = queue.poll(); 
        // Notify waiting threads 
        notifyAll(); 
        return order; 
    } 
    public synchronized void finishOrders() { 
        finished = true; 
        notifyAll(); 
    } 
} 
// Waiter thread: Producer 
class OrderTakerThread extends Thread { 
    private final KitchenOrderQueue kitchen; 
    private final Queue<FoodOrder> orders; 
    public OrderTakerThread( 
            KitchenOrderQueue kitchen, 
            Queue<FoodOrder> orders) { 
        super("Waiter-1"); 
        this.kitchen = kitchen; 
        this.orders = orders; 
    } 
    @Override 
    public void run() { 
        try { 
            for (FoodOrder order : orders) { 
                kitchen.placeOrder(order); 
            } 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            System.out.println("Waiter interrupted."); 
        } catch (KitchenOverloadException e) { 
            System.out.println( 
                    "Kitchen error: " + e.getMessage()); 
        } finally { 
            kitchen.finishOrders(); 
        } 
    } 
} 
// Chef thread: Consumer 
class ChefStationThread extends Thread { 
    private final KitchenOrderQueue kitchen; 
    public ChefStationThread(KitchenOrderQueue kitchen) { 
        super("Chef-1"); 
        this.kitchen = kitchen; 
    } 
    @Override 
    public void run() { 
        try { 
            while (true) { 
                FoodOrder order = kitchen.cookNextOrder(); 
 
 
                if (order == null) { 
                    break; 
                } 
                System.out.println( 
                        "Chef-1 is preparing " + order.getItem()); 
                Thread.sleep(300); 
                System.out.println( 
                        "Chef-1 completed " + order.getItem()); 
            } 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            System.out.println("Chef interrupted."); 
        } 
    } 
} 
 
// Main application 
public class Main { 
    public static void main(String[] args) { 
        Scanner sc = new Scanner(System.in); 
        // Six menu items 
        String[] menu = { 
            "Idli", 
            "Medu Vada", 
            "Masala Dosa", 
            "Poori Masala", 
            "Veg Biryani", 
            "Gulab Jamun" 
        }; 
        // Prices of the six items 
        int[] prices = { 
            40, 35, 70, 60, 160, 40 
        }; 
        int[] quantities = new int[menu.length]; 
 
        System.out.println("=================================="); 
        System.out.println("  SOUTH & NORTH INDIAN RESTAURANT"); 
        System.out.println("=================================="); 
        System.out.print( 
                "Waiter: Welcome! May I know your name? "); 
        String customerName = sc.nextLine().trim(); 
        if (customerName.isEmpty()) { 
            customerName = "Customer"; 
        } 
        System.out.println( 
                "\nWaiter: Hello, " + customerName + "!"); 
        // Customer places orders 
        while (true) { 
            System.out.println("\n========== RESTAURANT MENU =========="); 
            for (int i = 0; i < menu.length; i++) { 
                System.out.printf("%2d. %-20s Rs. %d%n", 

                        i + 1, menu[i], prices[i]); 
            } 
            System.out.printf("%2d. Finish ordering%n", 
                    menu.length + 1); 
            System.out.println("====================================="); 
            System.out.print( 
                    "Waiter: What would you like to order? "); 
            if (!sc.hasNextInt()) { 
                System.out.println( 
                        "Please enter a valid menu number."); 
                sc.next(); 
                continue; 
            } 
            int choice = sc.nextInt(); 
            if (choice == menu.length + 1) { 
                break; 
            } 
            if (choice < 1 || choice > menu.length) { 
                System.out.println( 
                        "Invalid choice. Please try again."); 
                continue; 
            } 
            System.out.print("How many " 
                    + menu[choice - 1] + " would you like? "); 
            if (!sc.hasNextInt()) { 
                System.out.println( 
                        "Please enter a valid quantity."); 
                sc.next(); 
                continue; 
            } 
            int quantity = sc.nextInt(); 
 
            if (quantity <= 0) { 
                System.out.println( 
                        "Quantity must be greater than zero."); 
                continue; 
            } 
            quantities[choice - 1] += quantity; 
            System.out.println("Added " + quantity 
                    + " x " + menu[choice - 1] + "."); 
            System.out.println("Waiter: Anything else?"); 
        } 
        // Generate bill and kitchen orders 
        Queue<FoodOrder> orders = new LinkedList<>(); 
        long totalBill = 0; 
        System.out.println("\n=============================================="); 
        System.out.println("                 CUSTOMER BILL"); 
        System.out.println("=============================================="); 
        System.out.println("Customer: " + customerName); 
        System.out.printf("%-20s %5s %7s %9s%n", 
                "Food Item", "Qty", "Price", "Amount"); 
        System.out.println("----------------------------------------------"); 

        for (int i = 0; i < menu.length; i++) { 
            if (quantities[i] > 0) { 
                long amount = 
                        (long) quantities[i] * prices[i]; 
                totalBill += amount; 
                System.out.printf("%-20s %5d %7d %9d%n", 
                        menu[i], quantities[i], 
                        prices[i], amount); 
                // Create one kitchen ticket per unit 
                for (int j = 0; j < quantities[i]; j++) { 
                    orders.add(new FoodOrder(menu[i])); 
                } 
            } 
        } 
        System.out.println("----------------------------------------------"); 
        System.out.println("TOTAL BILL: Rs. " + totalBill); 
        System.out.println("=============================================="); 
        if (orders.isEmpty()) { 
            System.out.println( 
                    "Waiter: You haven't ordered anything."); 
            System.out.println( 
                    "Thank you for visiting our restaurant!"); 
            sc.close(); 
            return; 
        } 
        System.out.println( 
                "\nWaiter: Thank you, " + customerName + "!"); 
       System.out.println("Waiter: Your order is confirmed."); 
        System.out.println( 
                "Waiter: Sending your order to the kitchen..."); 
        // Create the kitchen queue 
        KitchenOrderQueue kitchen = 
                new KitchenOrderQueue(5); 
        // Create producer and consumer threads 
        ChefStationThread chef = 
                new ChefStationThread(kitchen); 
        OrderTakerThread waiter = 
                new OrderTakerThread(kitchen, orders); 
        // Start both threads 
        chef.start(); 
        waiter.start(); 
        try { 
            waiter.join(); 
            chef.join(); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            System.out.println( 
                    "Order processing interrupted."); 
        } 
        System.out.println("\n=================================="); 
        System.out.println( 
                "All orders processed successfully!"); 

        System.out.println( 
                "Thank you for visiting our restaurant!"); 
        System.out.println("=================================="); 
        sc.close(); 
    } 
} 
