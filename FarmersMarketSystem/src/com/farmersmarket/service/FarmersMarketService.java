package com.farmersmarket.service;

import com.farmersmarket.exception.*;
import com.farmersmarket.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service layer: holds all business rules and keeps vendor inventory accurate.
 */
public class FarmersMarketService {

    /** Return policy: returns accepted within this many days of the order date. */
    public static final int RETURN_WINDOW_DAYS = 7;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final String LINE = "-".repeat(105);
    private static final String DLINE = "=".repeat(105);

    private final Map<String, Vendor> vendors = new LinkedHashMap<>();
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final Map<String, Payment> payments = new LinkedHashMap<>();
    private final Map<String, Delivery> deliveries = new LinkedHashMap<>();
    private final Map<String, ReturnRequest> returnRequests = new LinkedHashMap<>();

    private int vendorCounter = 100, productCounter = 200, orderCounter = 1000;
    private int paymentCounter = 5000, deliveryCounter = 7000, returnCounter = 9000;

    public FarmersMarketService() {
        seedInitialData();
    }

    private void seedInitialData() {
        Vendor v1 = addVendor("Green Earth Organic Farm", "Ooty Hills, TN", "9876543210");
        Vendor v2 = addVendor("Valley Sunrise Dairy & Poultry", "Pollachi Plains, TN", "9123456789");
        try {
            addProduct("Organic Apples", "Fruits", 180.00, 25, v1.getVendorId());
            addProduct("Fresh Broccoli", "Vegetables", 65.00, 40, v1.getVendorId());
            addProduct("Wild Forest Honey", "Organic", 320.00, 15, v1.getVendorId());
            addProduct("Farm Fresh Cow Milk (1L)", "Dairy", 55.00, 50, v2.getVendorId());
            addProduct("Country Eggs (Pack of 6)", "Poultry", 75.00, 30, v2.getVendorId());
            addProduct("Artisanal Cow Ghee (500ml)", "Dairy", 420.00, 10, v2.getVendorId());
        } catch (ResourceNotFoundException e) {
            throw new IllegalStateException("Seed data error", e);
        }
    }

    // ------------------------------------------------------------ vendors / products
    public Vendor addVendor(String name, String location, String contact) {
        String id = "VND-" + (++vendorCounter);
        Vendor v = new Vendor(id, name, location, contact);
        vendors.put(id, v);
        return v;
    }

    public Product addProduct(String name, String category, double price, int stock, String vendorId)
            throws ResourceNotFoundException {
        Vendor vendor = vendors.get(vendorId.toUpperCase());
        if (vendor == null)
            throw new ResourceNotFoundException("Vendor with ID '" + vendorId + "' does not exist.");
        String id = "PRD-" + (++productCounter);
        Product p = new Product(id, name, category, price, stock, vendor);
        products.put(id, p);
        return p;
    }

    // ------------------------------------------------------------ orders (Rules 1, 2, 5)
    public Order placeOrder(String custName, String custPhone, Map<String, Integer> cart)
            throws ResourceNotFoundException, InsufficientStockException, InvalidInputException {
        if (cart.isEmpty())
            throw new InvalidInputException("Cart cannot be empty.");

        // Step 1: validate everything first so a failure never leaves stock half-updated.
        for (Map.Entry<String, Integer> e : cart.entrySet()) {
            Product prod = findProduct(e.getKey());
            if (e.getValue() > prod.getAvailableStock())
                throw new InsufficientStockException("Cannot place order: '" + prod.getName()
                        + "' has only " + prod.getAvailableStock() + " units available (Requested: "
                        + e.getValue() + ") from vendor '" + prod.getVendor().getName() + "'.");
        }

        // Step 2: confirm the order and reduce stock (Rule 2).
        String orderId = "ORD-" + (++orderCounter);
        Order order = new Order(orderId, custName, custPhone, LocalDate.now());
        Set<Vendor> affected = new LinkedHashSet<>();
        int seq = 1;
        for (Map.Entry<String, Integer> e : cart.entrySet()) {
            Product prod = findProduct(e.getKey());
            prod.decreaseStock(e.getValue());
            affected.add(prod.getVendor());
            order.addItem(new OrderItem("ITEM-" + (seq++), prod, e.getValue()));
        }
        orders.put(orderId, order);

        displayOrderInvoice(order);
        displayVendorStockUpdates("ORDER CONFIRMATION (" + orderId + ")", affected);
        return order;
    }

    // ------------------------------------------------------------ payment
    public Payment recordPayment(String orderId, Payment payment)
            throws ResourceNotFoundException, MarketException {
        Order order = findOrder(orderId);
        if (order.isPaymentSettled())
            throw new MarketException("Payment already settled for " + order.getOrderId() + ".");
        if (!payment.processPayment())
            throw new MarketException("Payment failed: amount tendered is less than Rs."
                    + String.format("%.2f", payment.getAmount()) + ".");
        order.setPayment(payment);
        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    // ------------------------------------------------------------ delivery (Rule 3)
    public Delivery scheduleDelivery(String orderId, String address, String phone, String date)
            throws ResourceNotFoundException, PaymentPendingException, InvalidInputException {
        Order order = findOrder(orderId);
        if (!order.isPaymentSettled())
            throw new PaymentPendingException("Payment Pending! Order " + order.getOrderId()
                    + " is not settled. Settle payment before scheduling delivery.");
        if (order.getDelivery() != null)
            throw new InvalidInputException("Delivery already scheduled for " + order.getOrderId() + ".");
        String id = "DEL-" + (++deliveryCounter);
        Delivery d = new Delivery(id, order.getOrderId(), address, phone, date);
        order.setDelivery(d);
        deliveries.put(id, d);
        return d;
    }

    // ------------------------------------------------------------ returns (Rules 2, 4)
    public ReturnRequest requestReturn(String orderId, String productId, int qty, String reason)
            throws ResourceNotFoundException, InvalidReturnException {
        Order order = findOrder(orderId);
        if (!order.isPaymentSettled())
            throw new InvalidReturnException("Return rejected! Order " + order.getOrderId()
                    + " has no settled payment.");

        long days = ChronoUnit.DAYS.between(order.getOrderLocalDate(), LocalDate.now());
        if (days > RETURN_WINDOW_DAYS)
            throw new InvalidReturnException("Return rejected! Return window of "
                    + RETURN_WINDOW_DAYS + " days has expired (" + days + " days since order).");

        OrderItem item = order.findItemByProductId(productId);
        if (item == null)
            throw new InvalidReturnException("Product '" + productId + "' is not part of order "
                    + order.getOrderId() + ".");

        int returned = order.getReturnedQuantityForProduct(productId);
        int eligible = item.getQuantity() - returned;
        if (qty > eligible)
            throw new InvalidReturnException("Return rejected! Requested quantity (" + qty
                    + ") exceeds eligible quantity (" + eligible + "). Purchased: "
                    + item.getQuantity() + ", already returned: " + returned + ".");

        Product product = item.getProduct();
        String id = "RET-" + (++returnCounter);
        ReturnRequest req = new ReturnRequest(id, order.getOrderId(), product.getProductId(), qty,
                reason, LocalDate.now().format(FMT));

        boolean perishableChangeOfMind = "Dairy".equalsIgnoreCase(product.getCategory())
                && reason.toLowerCase().contains("change of mind");
        if (perishableChangeOfMind) {
            req.decide(false, "REJECTED: Perishable dairy cannot be returned for change of mind.", 0);
        } else {
            req.decide(true, "APPROVED: Meets return policy. Quality check passed.",
                    qty * item.getUnitPrice());
        }
        order.addReturnRequest(req);
        returnRequests.put(id, req);

        if (ReturnRequest.APPROVED.equals(req.getStatus())) {
            product.increaseStock(qty); // Rule 2
            displayVendorStockUpdates("APPROVED RETURN RESTOCK (" + id + ")",
                    Collections.singleton(product.getVendor()));
        }
        return req;
    }

    // ------------------------------------------------------------ lookups
    private Product findProduct(String id) throws ResourceNotFoundException {
        Product p = products.get(id.toUpperCase());
        if (p == null) throw new ResourceNotFoundException("Product '" + id + "' not found.");
        return p;
    }

    public Order findOrder(String id) throws ResourceNotFoundException {
        Order o = orders.get(id.toUpperCase());
        if (o == null) throw new ResourceNotFoundException("Order '" + id + "' does not exist.");
        return o;
    }

    public Product getProduct(String id) throws ResourceNotFoundException { return findProduct(id); }
    public Map<String, Vendor> getVendors() { return Collections.unmodifiableMap(vendors); }
    public Map<String, Product> getProducts() { return Collections.unmodifiableMap(products); }
    public Map<String, Order> getOrders() { return Collections.unmodifiableMap(orders); }
    public String generateNextPaymentId() { return "PAY-" + (++paymentCounter); }

    // ------------------------------------------------------------ displays
    public void displayInventory() {
        System.out.println("\n" + DLINE);
        System.out.println("                      FARMERS' MARKET: VENDOR-WISE INVENTORY");
        System.out.println(DLINE);
        for (Vendor v : vendors.values()) {
            System.out.println("\n" + LINE);
            System.out.printf("Vendor ID   : %-10s | Name    : %s%n", v.getVendorId(), v.getName());
            System.out.printf("Farm Origin : %-10s | Contact : %s%n", v.getFarmLocation(), v.getContactNumber());
            System.out.println(LINE);
            System.out.printf("%-10s %-30s %-12s %12s %14s%n",
                    "PROD ID", "PRODUCT NAME", "CATEGORY", "UNIT PRICE", "STOCK IN HAND");
            System.out.println(LINE);
            if (v.getProducts().isEmpty()) {
                System.out.println("  No products for this vendor.");
            }
            for (Product p : v.getProducts()) {
                System.out.printf("%-10s %-30s %-12s Rs.%9.2f %14d%n", p.getProductId(),
                        p.getName(), p.getCategory(), p.getUnitPrice(), p.getAvailableStock());
            }
        }
        System.out.println(DLINE + "\n");
    }

    public void displayVendorStockUpdates(String title, Collection<Vendor> affected) {
        System.out.println("\n>>> [EVENT] VENDOR-WISE INVENTORY UPDATE AFTER " + title);
        System.out.println(LINE);
        System.out.printf("%-10s %-30s %-10s %-30s %8s%n",
                "VENDOR ID", "VENDOR NAME", "PROD ID", "PRODUCT NAME", "STOCK");
        System.out.println(LINE);
        for (Vendor v : affected)
            for (Product p : v.getProducts())
                System.out.printf("%-10s %-30s %-10s %-30s %8d%n", v.getVendorId(), v.getName(),
                        p.getProductId(), p.getName(), p.getAvailableStock());
        System.out.println(LINE);
    }

    public void displayOrderInvoice(Order order) {
        System.out.println("\n" + DLINE);
        System.out.println("                         FARMERS' MARKET ORDER INVOICE");
        System.out.println(DLINE);
        System.out.printf("Invoice No   : INV-%s      Date    : %s%n", order.getOrderId(), order.getOrderDate());
        System.out.printf("Customer Name: %-28s Contact : %s%n", order.getCustomerName(), order.getCustomerContact());
        System.out.println(LINE);
        System.out.printf("%-9s %-28s %-30s %4s %10s %12s%n",
                "PROD ID", "PRODUCT NAME", "VENDOR", "QTY", "PRICE(Rs.)", "SUBTOTAL(Rs.)");
        System.out.println(LINE);
        for (OrderItem it : order.getItems()) System.out.println(it);
        System.out.println(LINE);
        System.out.printf("%70s TOTAL AMOUNT   : Rs.%.2f%n", "", order.calculateTotal());
        System.out.printf("%70s PAYMENT STATUS : %s%n", "",
                order.getPayment() != null ? order.getPayment().getPaymentStatus() : "PENDING");
        System.out.printf("%70s DELIVERY STATUS: %s%n", "",
                order.getDelivery() != null ? order.getDelivery().getDeliveryStatus() : "NOT SCHEDULED");
        System.out.println(DLINE);
    }

    public void displayCustomerOrderHistory() {
        System.out.println("\n" + DLINE);
        System.out.println("                      CUSTOMER ORDER HISTORY & TOTALS");
        System.out.println(DLINE);
        if (orders.isEmpty()) {
            System.out.println("No orders placed yet.");
            System.out.println(DLINE);
            return;
        }
        // Group by customer phone so each customer's total is shown.
        Map<String, List<Order>> byCustomer = new LinkedHashMap<>();
        for (Order o : orders.values())
            byCustomer.computeIfAbsent(o.getCustomerContact(), k -> new ArrayList<>()).add(o);

        double grossAll = 0, refundAll = 0;
        for (List<Order> list : byCustomer.values()) {
            Order first = list.get(0);
            System.out.printf("%nCUSTOMER: %s (Phone: %s)%n", first.getCustomerName(), first.getCustomerContact());
            System.out.println(LINE);
            double gross = 0, refund = 0;
            for (Order o : list) {
                String pay = o.getPayment() != null
                        ? o.getPayment().getPaymentStatus() + " (" + o.getPayment().getPaymentMode() + ")"
                        : "UNPAID";
                String del = o.getDelivery() != null ? o.getDelivery().getDeliveryStatus() : "NOT SCHEDULED";
                System.out.printf("Order %s | Date: %s | Total: Rs.%.2f | Payment: %s | Delivery: %s%n",
                        o.getOrderId(), o.getOrderDate(), o.calculateTotal(), pay, del);
                for (OrderItem it : o.getItems())
                    System.out.printf("   - %-28s x %2d @ Rs.%7.2f = Rs.%.2f (Vendor: %s)%n",
                            it.getProduct().getName(), it.getQuantity(), it.getUnitPrice(),
                            it.getSubtotal(), it.getProduct().getVendor().getName());
                for (ReturnRequest r : o.getReturnRequests())
                    System.out.printf("   * [%s] %s Qty: %d, %s, Refund: Rs.%.2f (Reason: %s)%n",
                            r.getReturnId(), r.getProductId(), r.getRequestedQuantity(),
                            r.getStatus(), r.getRefundAmount(), r.getReason());
                gross += o.calculateTotal();
                refund += o.getRefundedAmount();
            }
            System.out.println(LINE);
            System.out.printf("Customer total: Rs.%.2f | Refunds: Rs.%.2f | Net: Rs.%.2f%n", gross, refund, gross - refund);
            grossAll += gross;
            refundAll += refund;
        }
        System.out.println("\n" + DLINE);
        System.out.printf("MARKET TOTAL: Rs.%.2f gross - Rs.%.2f refunds = Rs.%.2f net across %d order(s)%n",
                grossAll, refundAll, grossAll - refundAll, orders.size());
        System.out.println(DLINE + "\n");
    }
}
