package fees.model;

/**
 * Unit 2 Syllabus Concept: Inheritance Fee -> TuitionFee
 * Represents academic tuition fee structure including lab, library, and scholarship concessions.
 */
public class TuitionFee extends Fee {

    private double labCharge;
    private double libraryCharge;
    private double scholarshipConcession;

    public TuitionFee() {
        super();
        this.labCharge = 0.0;
        this.libraryCharge = 0.0;
        this.scholarshipConcession = 0.0;
    }

    public TuitionFee(int feeId, String studentId, double baseTuition, double labCharge,
                      double libraryCharge, double scholarshipConcession, double paidAmount) {
        super(feeId, studentId, baseTuition, paidAmount);
        this.labCharge = roundAmount(labCharge);
        this.libraryCharge = roundAmount(libraryCharge);
        this.scholarshipConcession = roundAmount(scholarshipConcession);
        this.pendingAmount = getPendingDue();
    }

    @Override
    public String getFeeType() {
        return "Tuition Fee";
    }

    @Override
    public double calculateTotalFee() {
        // Total = Base Tuition + Lab + Library - Scholarship (clamped >= 0 using Math.max)
        double total = baseAmount + labCharge + libraryCharge - scholarshipConcession;
        return Math.max(0.0, roundAmount(total));
    }

    // Getters and Setters
    public double getLabCharge() { return labCharge; }
    public void setLabCharge(double labCharge) { this.labCharge = roundAmount(labCharge); }

    public double getLibraryCharge() { return libraryCharge; }
    public void setLibraryCharge(double libraryCharge) { this.libraryCharge = roundAmount(libraryCharge); }

    public double getScholarshipConcession() { return scholarshipConcession; }
    public void setScholarshipConcession(double scholarshipConcession) {
        this.scholarshipConcession = roundAmount(scholarshipConcession);
    }
}
