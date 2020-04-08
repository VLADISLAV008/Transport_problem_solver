package calculation;

import entities.Answer;
import entities.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Solver. A class solving a general transport problem of an arbitrary type.
 *
 * @author V.Shramenko
 */
public class Solver {

    private ArrayList<Double> productionCapacity;
    private double[][] rates;
    private ArrayList<Double> powerConsumption;
    private boolean fictitiousProduction;
    private boolean fictitiousConsumer;
    private double[] productionPotentials;
    private double[] consumerPotentials;

    public Solver(int numberProduction, int numberConsumption) {
        productionCapacity = new ArrayList<>();
        powerConsumption = new ArrayList<>();
        rates = new double[numberProduction + 1][numberConsumption + 1];
    }

    /**
     * The function reduces the transport problem to
     * a closed transport problem.
     */
    private void reduceToClosedProblem() {
        double powerManufacturers = 0;
        for (double power : productionCapacity) {
            powerManufacturers += power;
        }
        double powerConsumers = 0;
        for (double power : powerConsumption) {
            powerConsumers += power;
        }
        double dif = powerManufacturers - powerConsumers;
        if (dif > 0) {
            powerConsumption.add(dif);
            fictitiousConsumer = true;
        }
        if (dif < 0) {
            productionCapacity.add(-dif);
            fictitiousProduction = true;
        }
    }

    /**
     * Determining the initial plan of the transportation problem
     * using the northwest corner method.
     *
     * @return initial transportation plan
     */

    private double[][] getInitialPlan() {
        double[][] plan = new double[productionCapacity.size()][powerConsumption.size()];
        int currentConsumer = 0;
        double currentQuantityGoods = powerConsumption.get(currentConsumer);
        for (int i = 0; i < productionCapacity.size(); i++) {
            double quantityGoods = productionCapacity.get(i);
            while (quantityGoods > 0) {
                if (quantityGoods > currentQuantityGoods) {
                    plan[i][currentConsumer] = currentQuantityGoods;
                    quantityGoods -= currentQuantityGoods;
                    currentConsumer++;
                    currentQuantityGoods = powerConsumption.get(currentConsumer);
                } else {
                    plan[i][currentConsumer] = quantityGoods;
                    currentQuantityGoods -= quantityGoods;
                    quantityGoods = 0;
                }
            }
        }
        return plan;
    }

    /**
     * The function generates an array storing the basicity of the cell of the transportation plan.
     *
     * @param plan initial transportation plan
     * @return Array defining basic cells of the initial plan.
     */
    private boolean[][] baseCellsOfInitialPlan(double[][] plan) {
        boolean[][] base = new boolean[productionCapacity.size()][powerConsumption.size()];
        for (int i = 0; i < productionCapacity.size(); i++) {
            for (int j = 0; j < powerConsumption.size(); j++) {
                if (plan[i][j] != 0) {
                    base[i][j] = true;
                } else {
                    if (j > 0 && plan[i][j - 1] != 0) {
                        double sum = 0;
                        for (int k = i + 1; k < productionCapacity.size(); k++) {
                            sum += plan[k][j - 1];
                        }
                        if (sum == 0) {
                            base[i][j] = true;
                        }
                    }
                }
            }
        }
        return base;
    }

    /**
     * Calculation of transportation plan potentials.
     *
     * @param baseCells Array defining basic cells.
     *                  baseCells[i][j] = true if cell (i,j) is base.
     */
    private void calculationPotentials(boolean[][] baseCells) {
        if (productionPotentials == null) {
            productionPotentials = new double[productionCapacity.size()];
        }
        if (consumerPotentials == null) {
            consumerPotentials = new double[powerConsumption.size()];
        }

        boolean[] usedConsumerIndex = new boolean[powerConsumption.size()];
        int consumerIndex = powerConsumption.size() - 1;
        consumerPotentials[consumerIndex] = 0;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(consumerIndex);
        usedConsumerIndex[consumerIndex] = true;
        while (!queue.isEmpty()) {
            consumerIndex = queue.remove();
            for (int i = 0; i < productionCapacity.size(); i++) {
                if (baseCells[i][consumerIndex]) {
                    productionPotentials[i] = rates[i][consumerIndex] - consumerPotentials[consumerIndex];
                    for (int j = 0; j < powerConsumption.size(); j++) {
                        if (baseCells[i][j] && !usedConsumerIndex[j]) {
                            consumerPotentials[j] = rates[i][j] - productionPotentials[i];
                            queue.add(j);
                            usedConsumerIndex[j] = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Transportation plan optimization
     *
     * @param plan transportation plan
     */
    private void optimizationPlan(double[][] plan, boolean[][] baseCells) {
        boolean optimal = false;
        while (!optimal) {
            calculationPotentials(baseCells);
            int productionIndex = -1;
            int consumerIndex = -1;
            double minValue = 0;

            for (int i = 0; i < productionCapacity.size(); i++) {
                for (int j = 0; j < powerConsumption.size(); j++) {
                    double value = rates[i][j] - productionPotentials[i] - consumerPotentials[j];
                    if (value < 0 && minValue > value) {
                        minValue = value;
                        productionIndex = i;
                        consumerIndex = j;
                    }
                }
            }

            if (productionIndex != -1) {
                ArrayList<Vertex> positive = new ArrayList<>();
                ArrayList<Vertex> negative = new ArrayList<>();

                boolean[][] used = new boolean[productionCapacity.size()][powerConsumption.size()];
                Vertex[][] parent = new Vertex[productionCapacity.size()][powerConsumption.size()];
                Queue<Vertex> queue = new LinkedList<>();

                Vertex startV = new Vertex(productionIndex, consumerIndex);

                queue.add(startV);
                used[productionIndex][consumerIndex] = true;
                while (!queue.isEmpty()) {
                    Vertex v = queue.remove();
                    if (!v.equals(startV)) {
                        for (int i = 0; i < productionCapacity.size(); i++) {
                            if (!used[i][v.getColumn()] && baseCells[i][v.getColumn()] && i != v.getRow()) {
                                Vertex vertexTo = new Vertex(i, v.getColumn());
                                used[i][v.getColumn()] = true;
                                parent[i][v.getColumn()] = v;
                                queue.add(vertexTo);
                            }
                        }
                    }
                    for (int i = 0; i < powerConsumption.size(); i++) {
                        if (!used[v.getRow()][i] && baseCells[v.getRow()][i] && i != v.getColumn()) {
                            Vertex vertexTo = new Vertex(v.getRow(), i);
                            used[v.getRow()][i] = true;
                            parent[v.getRow()][i] = v;
                            queue.add(vertexTo);
                        }
                    }
                }
                positive.add(startV);
                for (int i = 0; i < productionCapacity.size(); i++) {
                    if (baseCells[i][consumerIndex] && parent[i][consumerIndex] != null &&
                            parent[i][consumerIndex].getColumn() != startV.getColumn()) {
                        Vertex currentV = new Vertex(i, consumerIndex);

                        boolean positiveSign = false;
                        while (!parent[currentV.getRow()][currentV.getColumn()].equals(startV)) {
                            if (positiveSign) {
                                positive.add(currentV);
                            } else {
                                negative.add(currentV);
                            }
                            positiveSign = !positiveSign;
                            currentV = parent[currentV.getRow()][currentV.getColumn()];
                        }
                        negative.add(currentV);

                        double min = Double.MAX_VALUE;
                        for (Vertex vertex : negative) {
                            if (min > plan[vertex.getRow()][vertex.getColumn()]) {
                                min = plan[vertex.getRow()][vertex.getColumn()];
                            }
                        }

                        boolean changeBaseCells = false;
                        for (Vertex vertex : negative) {
                            plan[vertex.getRow()][vertex.getColumn()] -= min;
                            if (plan[vertex.getRow()][vertex.getColumn()] == 0 && !changeBaseCells) {
                                baseCells[vertex.getRow()][vertex.getColumn()] = false;
                                changeBaseCells = true;
                            }
                        }
                        for (Vertex vertex : positive) {
                            plan[vertex.getRow()][vertex.getColumn()] += min;
                        }
                        baseCells[startV.getRow()][startV.getColumn()] = true;
                        break;
                    }
                }
            } else {
                optimal = true;
            }
        }
    }

    /**
     * Calculates total shipping costs
     *
     * @param plan transportation plan
     * @return total shipping cost
     */
    private double totalCostCalculation(double[][] plan) {
        double cost = 0;
        for (int i = 0; i < productionCapacity.size(); i++) {
            for (int j = 0; j < powerConsumption.size(); j++) {
                cost += plan[i][j] * rates[i][j];
            }
        }
        return cost;
    }

    /**
     * Function remove fictitious node(if it exists) from transportation plan
     *
     * @param plan transportation plan
     * @return resulting transportation plan without fictitious nodes
     */
    private double[][] removeFictitiousNode(double[][] plan) {
        int numberProduction = productionCapacity.size();
        int numberConsumption = powerConsumption.size();
        if (fictitiousProduction) {
            numberProduction--;
        }
        if (fictitiousConsumer) {
            numberConsumption--;
        }

        if (fictitiousConsumer || fictitiousProduction) {
            double[][] newPlan = new double[numberProduction][numberConsumption];
            for (int i = 0; i < numberProduction; i++) {
                if (numberConsumption >= 0) {
                    System.arraycopy(plan[i], 0, newPlan[i], 0, numberConsumption);
                }
            }
            return newPlan;
        }
        return plan;
    }

    /**
     * @return object Answer which consists of the transport matrix and the total cost of transportation
     */
    public Answer getAnswer() {
        reduceToClosedProblem();
        double[][] plan = getInitialPlan();
        boolean[][] baseCells = baseCellsOfInitialPlan(plan);
        optimizationPlan(plan, baseCells);
        plan = removeFictitiousNode(plan);
        double totalCost = totalCostCalculation(plan);
        return new Answer(plan, totalCost);
    }

    public ArrayList<Double> getProductionCapacity() {
        return productionCapacity;
    }

    public double[][] getRates() {
        return rates;
    }

    public ArrayList<Double> getPowerConsumption() {
        return powerConsumption;
    }
}
