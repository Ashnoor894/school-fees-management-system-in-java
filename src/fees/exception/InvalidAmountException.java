package fees.exception;

/**
 * Unit 3 Syllabus Concept: Custom Exception InvalidAmountException
 * Thrown when an invalid payment or fee amount is entered (e.g. <= 0, or amount > pending dues).
 */
public class InvalidAmountException extends Exception {

    private final double attemptedAmount;

    public InvalidAmountException(String message) {
        super(message);
        this.attemptedAmount = 0.0;
    }

    public InvalidAmountException(String message, double attemptedAmount) {
        super(message + " (Attempted: Rs. " + String.format("%.2f", attemptedAmount) + ")");
        this.attemptedAmount = attemptedAmount;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }
}
