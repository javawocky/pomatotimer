package com.thefryup.pomodoropilot;

import java.util.Random;

/**
 * Recurrent neural network: 27→64(ReLU)→48(tanh,recurrent)→3(sigmoid)
 * Inputs: 18 raycasts + 4 screen edges (N/S/E/W) + vx + vy + sin + cos + angular_vel
 * Elman-style with full self-connection on recurrent layer
 */
public class RecurrentNeuralNetwork {
    private static final int INPUT_SIZE = 27;
    private static final int HIDDEN_SIZE = 64;
    private static final int RECURRENT_SIZE = 48;
    private static final int OUTPUT_SIZE = 3;
    
    // Weights (package-private for batched processing)
    double[][] weightsInputHidden;      // 23x64
    double[] biasHidden;                // 64
    double[][] weightsHiddenRecurrent;  // 64x48
    double[][] weightsRecurrentRecurrent; // 48x48 (self-connection)
    double[] biasRecurrent;             // 48
    double[][] weightsRecurrentOutput;  // 48x3
    double[] biasOutput;                // 3
    
    // State
    private double[] recurrentState;            // 48 units, persists across time
    
    // For visualization
    private double[] lastInputs;
    private double[] lastHidden;
    private double[] lastRecurrent;
    private double[] lastOutput;
    
    public RecurrentNeuralNetwork() {
        Random rand = new Random();
        
        // Xavier initialization for input→hidden
        weightsInputHidden = new double[INPUT_SIZE][HIDDEN_SIZE];
        double limitIH = Math.sqrt(6.0 / (INPUT_SIZE + HIDDEN_SIZE));
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weightsInputHidden[i][j] = (rand.nextDouble() * 2 - 1) * limitIH;
            }
        }
        biasHidden = new double[HIDDEN_SIZE];
        
        // Xavier initialization for hidden→recurrent
        weightsHiddenRecurrent = new double[HIDDEN_SIZE][RECURRENT_SIZE];
        double limitHR = Math.sqrt(6.0 / (HIDDEN_SIZE + RECURRENT_SIZE));
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                weightsHiddenRecurrent[i][j] = (rand.nextDouble() * 2 - 1) * limitHR;
            }
        }
        
        // Smaller initialization for recurrent→recurrent (stability)
        weightsRecurrentRecurrent = new double[RECURRENT_SIZE][RECURRENT_SIZE];
        double limitRR = Math.sqrt(2.0 / RECURRENT_SIZE);
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                weightsRecurrentRecurrent[i][j] = (rand.nextDouble() * 2 - 1) * limitRR;
            }
        }
        biasRecurrent = new double[RECURRENT_SIZE];
        
        // Xavier initialization for recurrent→output
        weightsRecurrentOutput = new double[RECURRENT_SIZE][OUTPUT_SIZE];
        double limitRO = Math.sqrt(6.0 / (RECURRENT_SIZE + OUTPUT_SIZE));
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsRecurrentOutput[i][j] = (rand.nextDouble() * 2 - 1) * limitRO;
            }
        }
        biasOutput = new double[OUTPUT_SIZE];
        
        // Initialize recurrent state to zeros
        recurrentState = new double[RECURRENT_SIZE];
    }
    
    private RecurrentNeuralNetwork(double[][] wih, double[] bh, double[][] whr, double[][] wrr, 
                                   double[] br, double[][] wro, double[] bo) {
        this.weightsInputHidden = wih;
        this.biasHidden = bh;
        this.weightsHiddenRecurrent = whr;
        this.weightsRecurrentRecurrent = wrr;
        this.biasRecurrent = br;
        this.weightsRecurrentOutput = wro;
        this.biasOutput = bo;
        this.recurrentState = new double[RECURRENT_SIZE];
    }
    
    /**
     * Forward pass: input → ReLU → add recurrent → tanh → save state → sigmoid output
     */
    public double[] predict(double[] inputs) {
        if (inputs.length != INPUT_SIZE) {
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " inputs, got " + inputs.length);
        }
        
        lastInputs = inputs.clone();
        
        // Layer 1: Input → Hidden (ReLU)
        lastHidden = new double[HIDDEN_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            double sum = biasHidden[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sum += inputs[j] * weightsInputHidden[j][i];
            }
            lastHidden[i] = relu(sum);
        }
        
        // Layer 2: Hidden + Recurrent → Recurrent (tanh)
        double[] newRecurrent = new double[RECURRENT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            double sum = biasRecurrent[i];
            // Add contribution from hidden layer
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                sum += lastHidden[j] * weightsHiddenRecurrent[j][i];
            }
            // Add contribution from previous recurrent state (memory)
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                sum += recurrentState[j] * weightsRecurrentRecurrent[j][i];
            }
            newRecurrent[i] = tanh(sum);
        }
        
        // Update recurrent state for next time step
        recurrentState = newRecurrent;
        lastRecurrent = newRecurrent;
        
        // Layer 3: Recurrent → Output (sigmoid)
        lastOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            double sum = biasOutput[i];
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                sum += newRecurrent[j] * weightsRecurrentOutput[j][i];
            }
            lastOutput[i] = sigmoid(sum);
        }
        
        return lastOutput;
    }
    
    /**
     * Reset recurrent state (call at start of each life/episode)
     */
    public void resetState() {
        recurrentState = new double[RECURRENT_SIZE];
    }
    
    private double relu(double x) {
        return Math.max(0, x);
    }
    
    private double tanh(double x) {
        return Math.tanh(x);
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    public RecurrentNeuralNetwork clone() {
        // Deep copy all weights
        double[][] wih = new double[INPUT_SIZE][HIDDEN_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            System.arraycopy(weightsInputHidden[i], 0, wih[i], 0, HIDDEN_SIZE);
        }
        
        double[] bh = new double[HIDDEN_SIZE];
        System.arraycopy(biasHidden, 0, bh, 0, HIDDEN_SIZE);
        
        double[][] whr = new double[HIDDEN_SIZE][RECURRENT_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            System.arraycopy(weightsHiddenRecurrent[i], 0, whr[i], 0, RECURRENT_SIZE);
        }
        
        double[][] wrr = new double[RECURRENT_SIZE][RECURRENT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            System.arraycopy(weightsRecurrentRecurrent[i], 0, wrr[i], 0, RECURRENT_SIZE);
        }
        
        double[] br = new double[RECURRENT_SIZE];
        System.arraycopy(biasRecurrent, 0, br, 0, RECURRENT_SIZE);
        
        double[][] wro = new double[RECURRENT_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            System.arraycopy(weightsRecurrentOutput[i], 0, wro[i], 0, OUTPUT_SIZE);
        }
        
        double[] bo = new double[OUTPUT_SIZE];
        System.arraycopy(biasOutput, 0, bo, 0, OUTPUT_SIZE);
        
        return new RecurrentNeuralNetwork(wih, bh, whr, wrr, br, wro, bo);
    }
    
    public void mutate(double mutationRate) {
        Random rand = new Random();
        
        // Mutate all weight matrices
        mutateMatrix(weightsInputHidden, mutationRate, rand);
        mutateArray(biasHidden, mutationRate, rand);
        mutateMatrix(weightsHiddenRecurrent, mutationRate, rand);
        mutateMatrix(weightsRecurrentRecurrent, mutationRate, rand);
        mutateArray(biasRecurrent, mutationRate, rand);
        mutateMatrix(weightsRecurrentOutput, mutationRate, rand);
        mutateArray(biasOutput, mutationRate, rand);
    }
    
    private void mutateMatrix(double[][] matrix, double rate, Random rand) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (rand.nextDouble() < rate) {
                    matrix[i][j] += rand.nextGaussian() * 0.2;
                }
            }
        }
    }
    
    private void mutateArray(double[] array, double rate, Random rand) {
        for (int i = 0; i < array.length; i++) {
            if (rand.nextDouble() < rate) {
                array[i] += rand.nextGaussian() * 0.2;
            }
        }
    }
    
    public static RecurrentNeuralNetwork crossover(RecurrentNeuralNetwork p1, RecurrentNeuralNetwork p2, Random rand) {
        RecurrentNeuralNetwork child = p1.clone();
        
        // Crossover all weights
        crossoverMatrix(child.weightsInputHidden, p2.weightsInputHidden, rand);
        crossoverArray(child.biasHidden, p2.biasHidden, rand);
        crossoverMatrix(child.weightsHiddenRecurrent, p2.weightsHiddenRecurrent, rand);
        crossoverMatrix(child.weightsRecurrentRecurrent, p2.weightsRecurrentRecurrent, rand);
        crossoverArray(child.biasRecurrent, p2.biasRecurrent, rand);
        crossoverMatrix(child.weightsRecurrentOutput, p2.weightsRecurrentOutput, rand);
        crossoverArray(child.biasOutput, p2.biasOutput, rand);
        
        return child;
    }
    
    private static void crossoverMatrix(double[][] child, double[][] parent2, Random rand) {
        for (int i = 0; i < child.length; i++) {
            for (int j = 0; j < child[i].length; j++) {
                if (rand.nextBoolean()) {
                    child[i][j] = parent2[i][j];
                }
            }
        }
    }
    
    private static void crossoverArray(double[] child, double[] parent2, Random rand) {
        for (int i = 0; i < child.length; i++) {
            if (rand.nextBoolean()) {
                child[i] = parent2[i];
            }
        }
    }
    
    // Getters for visualization
    public double[] getLastInputs() { return lastInputs; }
    public double[] getLastHidden1() { return lastHidden; }
    public double[] getLastHidden2() { return lastRecurrent; }
    public double[] getLastOutput() { return lastOutput; }
}
