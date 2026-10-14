import java.util.Scanner;

/**
 * Helper utility class to handle console input reading and validation safely.
 * Prevents common Scanner buffer issues and handles invalid numerical input gracefully.
 */
public class InputValidator {

    /**
     * Reads a non-empty string line from the console.
     *
     * @param scanner Scanner instance
     * @param prompt  Prompt text displayed to user
     * @return Validated non-empty string trimmed
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Error: Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads a valid Student ID. Ensures ID is not empty.
     *
     * @param scanner Scanner instance
     * @param prompt  Prompt text displayed to user
     * @return Validated non-empty Student ID
     */
    public static String readStudentId(Scanner scanner, String prompt) {
        return readNonEmptyString(scanner, prompt);
    }

    /**
     * Reads student marks as a double between 0.0 and 100.0 inclusive.
     * Re-prompts the user if input is non-numeric or outside the valid range.
     *
     * @param scanner Scanner instance
     * @param prompt  Prompt text displayed to user
     * @return Validated marks between 0.0 and 100.0
     */
    public static double readValidMarks(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double marks = Double.parseDouble(input);
                if (marks >= 0.0 && marks <= 100.0) {
                    return marks;
                } else {
                    System.out.println("Error: Marks must be between 0 and 100 inclusive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid numeric format. Please enter a valid number (e.g., 85.5).");
            }
        }
    }

    /**
     * Reads an integer menu option choice within a specified range [min, max].
     *
     * @param scanner Scanner instance
     * @param prompt  Prompt text displayed to user
     * @param min     Minimum valid choice
     * @param max     Maximum valid choice
     * @return Validated choice integer
     */
    public static int readMenuChoice(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.printf("Error: Please select a valid option between %d and %d.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid option format. Please enter a number.");
            }
        }
    }

    /**
     * Asks user for confirmation (Y/N).
     *
     * @param scanner Scanner instance
     * @param prompt  Prompt text displayed to user
     * @return true if user types 'Y' or 'YES' (case-insensitive), false otherwise
     */
    public static boolean readConfirmation(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y") || input.equals("YES")) {
                return true;
            } else if (input.equals("N") || input.equals("NO")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'Y' for Yes or 'N' for No.");
            }
        }
    }
}
