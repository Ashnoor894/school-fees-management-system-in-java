package fees.model;

import fees.exception.InvalidAmountException;

/**
 * Unit 1 Syllabus Concept: Class Fee; Math class
 * Unit 2 Syllabus Concept: Inheritance base class; interface Payable; package fees.model
 */
public abstract class Fee implements Payable {

    protected int feeId;
    protected String studentId;
    protected double baseAmount;
    protected double paidAmount;
    protected double pendingAmount;
    protected String lastPaymentDate;

    public Fee() {
        this.baseAmount = 0.0;
        this.paidAmount = 0.0;
        this.pendingAmount = 0.0;
        this.lastPaymentDate = "None";
    }

    public Fee(int feeId, String studentId, double baseAmount, double paidAmount) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.baseAmount = roundAmount(baseAmount);
        this.paidAmount = roundAmount(paidAmount);
        // Unit 1 Math class: Math.max ensures pending fee is never negative
        this.pendingAmount = Math.max(0.0, roundAmount(calculateTotalFee() - this.paidAmount));
        this.lastPaymentDate = "None";
    }

    /**
     * Abstract method implemented by subclasses: TuitionFee, TransportFee, ExamFee.
     */
    public abstract String getFeeType();

    /**
     * Unit 1 Math class concept: Round currency amount to two decimal places using Math.round.
     */
    public static double roundAmount(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * Unit 1 Math class concept: Calculate late fee penalty using Math.min and Math.max.
     */
    public double calculateLateFine(int overdueDays, double dailyRate, double maxPenalty) {
        if (overdueDays <= 0) {
            return 0.0;
        }
        double rawPenalty = overdueDays * dailyRate;
        return roundAmount(Math.min(maxPenalty, Math.max(0.0, rawPenalty)));
    }

    @Override
    public void processPayment(double amount) throws InvalidAmountException {
        if (amount <= 0.0) {
            throw new InvalidAmountException("Payment amount must be greater than zero.", amount);
        }
        double currentDue = getPendingDue();
        if (amount > currentDue + 0.01) {
            throw new InvalidAmountException("Payment amount exceeds current pending dues of Rs. " + currentDue, amount);
        }
        this.paidAmount = roundAmount(this.paidAmount + amount);
        // Using Math.max to avoid negative due
        this.pendingAmount = Math.max(0.0, roundAmount(calculateTotalFee() - this.paidAmount));
    }

    @Override
    public double getPendingDue() {
        return Math.max(0.0, roundAmount(calculateTotalFee() - this.paidAmount));
    }

    // Getters and Setters
    public int getFeeId() { return feeId; }
    public void setFeeId(int feeId) { this.feeId = feeId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = roundAmount(baseAmount); }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = roundAmount(paidAmount); }

    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = roundAmount(pendingAmount); }

    public String getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(String lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
}
