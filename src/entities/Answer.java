package entities;

public class Answer {
    double[][] plan;
    double totalCosts;

    public Answer(double[][] plan, double totalCosts) {
        this.plan = plan;
        this.totalCosts = totalCosts;
    }

    public double[][] getPlan() {
        return plan;
    }

    public double getTotalCosts() {
        return totalCosts;
    }
}
