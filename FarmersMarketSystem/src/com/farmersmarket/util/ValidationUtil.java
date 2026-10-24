package com.farmersmarket.util;

import java.util.Scanner;

/**
 * Console input helpers. Each method keeps asking until the input is valid,
 * so bad input can never crash the program.
 */
public class ValidationUtil {

    private ValidationUtil() { }

    public static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println(">> Error: Input cannot be empty. Please try again.");
        }
    }

    public static int readPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v > 0) return v;
                System.out.println(">> Error: Value must be a positive whole number (> 0).");
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Invalid input. Enter a valid whole number.");
            }
        }
    }

    public static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(sc.nextLine().trim());
                if (v > 0.0 && !Double.isInfinite(v)) return v;
                System.out.println(">> Error: Amount must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Invalid input. Enter a valid number.");
            }
        }
    }

    public static int readMenuChoice(Scanner sc, int min, int max) {
        while (true) {
            System.out.print("Enter your choice (" + min + " - " + max + "): ");
            try {
                int c = Integer.parseInt(sc.nextLine().trim());
                if (c >= min && c <= max) return c;
                System.out.println(">> Error: Select an option between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Invalid input! Enter a valid number.");
            }
        }
    }

    public static String readPhoneNumber(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String p = sc.nextLine().trim();
            if (p.matches("[6-9]\\d{9}")) return p;
            System.out.println(">> Error: Enter a valid 10-digit mobile number (starts with 6-9).");
        }
    }

    public static String readCardNumber(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String c = sc.nextLine().trim().replaceAll("[\\s-]", "");
            if (c.matches("\\d{16}")) return c;
            System.out.println(">> Error: Card number must be 16 digits.");
        }
    }

    public static String readUpiId(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String u = sc.nextLine().trim();
            if (u.matches("[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}")) return u;
            System.out.println(">> Error: Invalid UPI ID. Example: name@oksbi");
        }
    }

    public static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String a = sc.nextLine().trim();
            if (a.equalsIgnoreCase("yes") || a.equalsIgnoreCase("y")) return true;
            if (a.equalsIgnoreCase("no") || a.equalsIgnoreCase("n")) return false;
            System.out.println(">> Error: Please answer yes or no.");
        }
    }
}
