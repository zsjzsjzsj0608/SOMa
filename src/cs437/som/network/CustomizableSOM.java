package cs437.som.network;

import cs437.som.*;
import cs437.som.distancemetrics.EuclideanDistanceMetric;
import cs437.som.learningrate.ConstantLearningRateFunction;
import cs437.som.neighborhood.LinearDecayNeighborhoodWidthFunction;
import cs437.som.topology.SquareGrid;

import java.security.SecureRandom;
import java.util.Random;

/**
 * A fully customizable self-organizing map.
 */
public class CustomizableSOM implements TrainableSelfOrganizingMap {
    private final int neuronCount;
    private final int inputSize;
    private final int expectedIterations;
    private final Dimension gridSize;

    private int iterations = 0;
    private double[][] weightMatrix;

    DistanceMetric distanceMetricStrategy = null;
    LearningRateFunction learningRateFunctionStrategy = null;
    NeighborhoodWidthFunction neighborhoodWidthFunctionStrategy = null;
    GridType gridTypeStrategy = null;

    public CustomizableSOM(Dimension gridSize, int inputSize, int expectedIterations) {
        this.gridSize = gridSize;
        this.neuronCount = gridSize.area;
        this.inputSize = inputSize;
        this.expectedIterations = expectedIterations;

        weightMatrix = new double[neuronCount][inputSize];
        initialize();


        setDistanceMetricStrategy(new EuclideanDistanceMetric());
        setLearningRateFunctionStrategy(new ConstantLearningRateFunction(0.1));
        setGridTypeStrategy(new SquareGrid());

        // This calculation is based on a recommendation from Dr. Kohonen in
        // Kohonen, Teuvo, 1990: The Self-organizing Map. Proc. of the IEEE,
        // Vol. 78, 1469.
        int gridRadius = Math.min(gridSize.x, gridSize.y) / 2;
        setNeighborhoodWidthFunctionStrategy(
                new LinearDecayNeighborhoodWidthFunction(gridRadius));
    }

    public void setDistanceMetricStrategy(DistanceMetric strategy) {
        if (iterations == 0) {
            distanceMetricStrategy = strategy;
        } else {
            throw new SOMError("Cannot change distance strategy after training has begun.");
        }
    }

    public void setLearningRateFunctionStrategy(LearningRateFunction strategy) {
        if (iterations == 0) {
            learningRateFunctionStrategy = strategy;
            learningRateFunctionStrategy.setExpectedIterations(expectedIterations);
        } else {
            throw new SOMError("Cannot change learning rate strategy after training has begun.");
        }
    }

    public void setNeighborhoodWidthFunctionStrategy(NeighborhoodWidthFunction strategy) {
        if (iterations == 0) {
            neighborhoodWidthFunctionStrategy = strategy;
            neighborhoodWidthFunctionStrategy.setExpectedIterations(expectedIterations);
        } else {
            throw new SOMError("Cannot change neighborhood width strategy after training has begun.");
        }
    }

    public void setGridTypeStrategy(GridType strategy) {
        if (iterations == 0) {
            gridTypeStrategy = strategy;
            gridTypeStrategy.setNeuronCount(gridSize);
        } else {
            throw new SOMError("Cannot change grid type strategy after training has begun.");
        }
    }

    public int getNeuronCount() {
        return neuronCount;
    }

    public Dimension getGridSize() {
        return gridSize;
    }

    public int getExpectedIterations() {
        return expectedIterations;
    }

    public int getInputLength() {
        return inputSize;
    }

    public double getWeight(int neuron, int weightIndex) {
        return weightMatrix[neuron][weightIndex];
    }

    public int getBestMatchingNeuron(double[] input) {
        checkInput(input);

        int bestMatch = 0;
        double lowestDistance2 = distanceMetricStrategy.distance(weightMatrix[0], input);
        for (int i = 1; i < neuronCount; i++) {
            double distance2temp = distanceMetricStrategy.distance(weightMatrix[i], input);
            if (distance2temp < lowestDistance2) {
                lowestDistance2 = distance2temp;
                bestMatch = i;
            }
        }
        return bestMatch;
    }

    public int getBestMatchingNeuron(int[] input) {
        double[] dbls = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            dbls[i] = input[i];
        }
        return getBestMatchingNeuron(dbls);
    }

    public void trainWith(double[] data) {
        checkInput(data);

        int best = getBestMatchingNeuron(data);
        adjustNeuronWeights(data, best);
        adjustNeighborsOf(best, data);
        iterations++;
    }

    public void trainWith(int[] data) {
        double[] dbls = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            dbls[i] = data[i];
        }
        trainWith(dbls);
    }

    private void checkInput(double[] input) {
        if (input.length != inputSize) {
            throw new IllegalArgumentException("Input vector length does not match network input size.");
        }
    }

    private void initialize() {
        Random r = new SecureRandom();
        for (int i = 0; i < neuronCount; i++) {
            for (int j = 0; j < inputSize; j++) {
                weightMatrix[i][j] = r.nextDouble();
            }
        }
    }

    // TODO add option to make dependent on the distance from the BMU
    private void adjustNeuronWeights(double[] input, int neuron) {
        for (int i = 0; i < weightMatrix[neuron].length; i++) {
            double delta = input[i] - weightMatrix[neuron][i];
            weightMatrix[neuron][i] += learningRateFunctionStrategy.learningRate(iterations) * delta;
        }
    }

    private void adjustNeighborsOf(int neuron, double[] input) {
        for (int i = 0; i < neuronCount; i++) {
            if (i != neuron && inNeighborhoodOf(neuron, i)) {
                adjustNeuronWeights(input, i);
            }
        }
    }

    private boolean inNeighborhoodOf(int winingestNeuron, int testNeuron) {
        return gridTypeStrategy.gridDistance(winingestNeuron, testNeuron)
                < neighborhoodWidthFunctionStrategy.neighborhoodWidth(iterations);
    }

    @Override
    public String toString() {
        return "CustomizableSOM{neuronCount=" + neuronCount +
                ", gridSize=" + gridSize +
                ", inputSize=" + inputSize +
                ", iterations=" + iterations +
                ", expectedIterations=" + expectedIterations +
                ", distanceMetricStrategy=" + distanceMetricStrategy +
                ", learningRateFunctionStrategy=" + learningRateFunctionStrategy +
                ", neighborhoodWidthFunctionStrategy=" + neighborhoodWidthFunctionStrategy +
                '}';
    }
}
