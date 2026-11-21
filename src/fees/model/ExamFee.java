package fees.model;

/**
 * Unit 2 Syllabus Concept: Inheritance Fee -> ExamFee
 * Represents examination fee based on registered theory subjects and laboratory practical charges.
 */
public class ExamFee extends Fee {

    private int numberOfSubjects;
    private double ratePerSubject;
    private double practicalAssessmentFee;

    public ExamFee() {
        super();
        this.numberOfSubjects = 0;
        this.ratePerSubject = 0.0;
        this.practicalAssessmentFee = 0.0;
    }

    public ExamFee(int feeId, String studentId, double baseRegistrationFee,
                   int numberOfSubjects, double ratePerSubject, double practicalAssessmentFee, double paidAmount) {
        super(feeId, studentId, baseRegistrationFee, paidAmount);
        this.numberOfSubjects = Math.max(0, numberOfSubjects);
        this.ratePerSubject = roundAmount(ratePerSubject);
        this.practicalAssessmentFee = roundAmount(practicalAssessmentFee);
        this.pendingAmount = getPendingDue();
    }

    @Override
    public String getFeeType() {
        return "Exam Fee";
    }

    @Override
    public double calculateTotalFee() {
        // Exam Fee = Base Registration + (Subjects * RatePerSubject) + Practical Assessment
        double total = baseAmount + (numberOfSubjects * ratePerSubject) + practicalAssessmentFee;
        return roundAmount(total);
    }

    // Getters and Setters
    public int getNumberOfSubjects() { return numberOfSubjects; }
    public void setNumberOfSubjects(int numberOfSubjects) { this.numberOfSubjects = Math.max(0, numberOfSubjects); }

    public double getRatePerSubject() { return ratePerSubject; }
    public void setRatePerSubject(double ratePerSubject) { this.ratePerSubject = roundAmount(ratePerSubject); }

    public double getPracticalAssessmentFee() { return practicalAssessmentFee; }
    public void setPracticalAssessmentFee(double practicalAssessmentFee) {
        this.practicalAssessmentFee = roundAmount(practicalAssessmentFee);
    }
}
