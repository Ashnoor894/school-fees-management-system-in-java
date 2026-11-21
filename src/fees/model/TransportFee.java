package fees.model;

/**
 * Unit 2 Syllabus Concept: Inheritance Fee -> TransportFee
 * Represents bus/van transport fee calculated using route distance and per-km slab rate.
 */
public class TransportFee extends Fee {

    private double distanceKm;
    private double ratePerKm;
    private String routeName;

    public TransportFee() {
        super();
        this.distanceKm = 0.0;
        this.ratePerKm = 0.0;
        this.routeName = "N/A";
    }

    public TransportFee(int feeId, String studentId, double baseServiceCharge,
                        double distanceKm, double ratePerKm, String routeName, double paidAmount) {
        super(feeId, studentId, baseServiceCharge, paidAmount);
        this.distanceKm = distanceKm;
        this.ratePerKm = ratePerKm;
        this.routeName = routeName != null ? routeName : "Route 1";
        this.pendingAmount = getPendingDue();
    }

    @Override
    public String getFeeType() {
        return "Transport Fee";
    }

    @Override
    public double calculateTotalFee() {
        // Transport = Base Service Charge + (distance * ratePerKm)
        double total = baseAmount + (distanceKm * ratePerKm);
        return roundAmount(total);
    }

    // Getters and Setters
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getRatePerKm() { return ratePerKm; }
    public void setRatePerKm(double ratePerKm) { this.ratePerKm = ratePerKm; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
}
