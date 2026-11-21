package fees.model;

import fees.exception.InvalidAmountException;

/**
 * Unit 2 Syllabus Concept: Interface Payable
 * Defines contract for fee-bearing components in the school system.
 */
public interface Payable {
    /**
     * Calculates the total computed fee based on fee components and discounts.
     * @return total fee amount
     */
    double calculateTotalFee();

    /**
     * Processes an incoming payment installment.
     * @param amount amount paid
     * @throws InvalidAmountException if amount is negative, zero, or exceeds due balance
     */
    void processPayment(double amount) throws InvalidAmountException;

    /**
     * Returns remaining unpaid fee balance.
     * @return pending due amount
     */
    double getPendingDue();
}
