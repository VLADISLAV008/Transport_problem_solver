package calculation;

import entities.Answer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

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
     * Calculation of transportation plan potentials.
     *
     * @param plan transportation plan
     */
    private void calculationPotentials(double[][] plan) {
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
                if (plan[i][consumerIndex] != 0) {
                    productionPotentials[i] = rates[i][consumerIndex] - consumerPotentials[consumerIndex];
                    for (int j = 0; j < powerConsumption.size(); j++) {
                        if (plan[i][j] != 0 && !usedConsumerIndex[j]) {
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
    private void optimizationPlan(double[][] plan) {
        boolean optimal = false;
        optimization:
        while (!optimal) {
            calculationPotentials(plan);
            int productionIndex = -1;
            int consumerIndex = -1;
            double minValue = 0;

            for (int i = 0; i < productionCapacity.size(); i++) {
                for (int j = 0; j < powerConsumption.size(); j++) {
                    double value = rates[i][j] - productionPotentials[i] - consumerPotentials[j];
                    if (value < 0 & minValue > value) {
                        minValue = value;
                        productionIndex = i;
                        consumerIndex = j;
                    }
                }
            }

            if (productionIndex != -1) {
                for (int i = 0; i < productionCapacity.size(); i++) {
                    if (plan[i][consumerIndex] != 0) {
                        for (int j = 0; j < powerConsumption.size(); j++) {
                            if (plan[i][j] != 0 & plan[productionIndex][j] != 0) {
                                double min = Math.min(plan[i][consumerIndex], plan[productionIndex][j]);
                                plan[i][consumerIndex] -= min;
                                plan[productionIndex][j] -= min;
                                plan[i][j] += min;
                                plan[productionIndex][consumerIndex] += min;
                                continue optimization;
                            }
                        }
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

        double[][] newPlan = new double[numberProduction][numberConsumption];
        for (int i = 0; i < numberProduction; i++) {
            for (int j = 0; j < numberConsumption; j++) {
                newPlan[i][j] = plan[i][j];
            }
        }
        return newPlan;
    }

    /**
     *
     * @return object Answer which consists of the transport matrix and the total cost of transportation
     */
    public Answer getAnswer() {
        reduceToClosedProblem();
        double[][] plan = getInitialPlan();
        optimizationPlan(plan);
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
