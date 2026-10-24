# Farmers' Market Orders & Payments System

**Course:** 23CSR306 Java Programming – Assignment II (OOP Mini Project)
**Specification 35:** Farmers' Market Orders & Payments

| | |
|---|---|
| **Name** | `<YOUR NAME>` |
| **Register Number** | `<YOUR REGISTER NUMBER>` |
| **Year / Dept / Sem / Section** | II / `<DEPT>` / III / `<SECTION>` |
| **College** | Karpagam College of Engineering |

## Problem Statement
A Java console application for a Farmers' Market that manages **vendors, products, orders,
payments, deliveries and returns**, keeping inventory accurate per vendor.

## Business Rules Implemented
1. Orders can be placed only for available vendor inventory (`InsufficientStockException`).
2. Stock decreases on order confirmation and increases on approved returns.
3. Payment must be settled before delivery can be scheduled (`PaymentPendingException`).
4. Returns must be within policy (7-day window, paid orders only, no change-of-mind returns for
   dairy) and cannot exceed the purchased quantity (`InvalidReturnException`).
5. Every `OrderItem` links directly to a vendor's `Product` (`Product -> Vendor`).

## OOP Concepts Used
| Concept | Where |
|---|---|
| Encapsulation | Private fields + getters/setters in all model classes; read-only collection views |
| Inheritance | `CashPayment`, `CardPayment`, `UPIPayment` extend abstract `Payment`; all exceptions extend `MarketException` |
| Polymorphism | `Payment.processPayment()` / `getPaymentMode()` overridden per payment type; `toString()` overrides |
| Aggregation / Composition | `Vendor` has `Product`s; `Order` owns `OrderItem`s and is associated with `Payment`, `Delivery`, `ReturnRequest`s |
| Abstraction | Abstract `Payment` class; service layer separates business logic from the UI |

## Project Structure
```
src/com/farmersmarket
├── exception   MarketException, InsufficientStockException, PaymentPendingException,
│               InvalidReturnException, ResourceNotFoundException, InvalidInputException
├── model       Vendor, Product, OrderItem, Payment (abstract), CashPayment, CardPayment,
│               UPIPayment, Delivery, ReturnRequest, Order
├── service     FarmersMarketService
├── util        ValidationUtil
└── main        FarmersMarketApp
```

## Menu
1. Add Vendor  2. Add Product  3. Place Order  4. Record Payment  5. Schedule Delivery
6. Request Return  7. Display Inventory  8. Customer Order History & Totals  9. Exit

## How to Run
**Requirement:** JDK 11 or later.

**Command line**
```bash
# from the project root
mkdir out
javac -d out $(find src -name "*.java")      # Windows PowerShell: javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.farmersmarket.main.FarmersMarketApp
```

**Eclipse:** File → Import → Existing Projects into Workspace (or create a Java Project and copy
`src/`) → run `FarmersMarketApp.java` as *Java Application*.

## Sample Data
Two vendors and six products are pre-loaded (e.g. `PRD-201` Organic Apples, `PRD-204` Cow Milk).
Order IDs start at `ORD-1001`.

## Sample Flow
`3` Place Order → `5` Schedule Delivery (blocked: payment pending) → `4` Record Payment →
`5` Schedule Delivery (works) → `6` Request Return → `7` Display Inventory → `8` Order History.
