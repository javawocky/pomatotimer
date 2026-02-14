package com.thefryup.pomodoropilot;

import java.util.Random;

public class SpaceNeuralNetwork {
    private static final int INPUT_SIZE = 23; // 18 raycasts + vx + vy + sin(angle) + cos(angle) + angular velocity
    private static final int HIDDEN1_SIZE = 32;
    private static final int HIDDEN2_SIZE = 24;
    private static final int OUTPUT_SIZE = 3; // left, right, thrust
    
    private double[][] weightsInputHidden1;
    private double[] biasHidden1;
    private double[][] weightsHidden1Hidden2;
    private double[] biasHidden2;
    private double[][] weightsHidden2Output;
    private double[] biasOutput;
    
    // Store last activations for visualization
    private double[] lastInputs;
    private double[] lastHidden1;
    private double[] lastHidden2;
    private double[] lastOutput;
    
    public SpaceNeuralNetwork() {
        Random rand = new Random();
        
        // Input to Hidden1 - Xavier initialization
        weightsInputHidden1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        double limit1 = Math.sqrt(6.0 / (INPUT_SIZE + HIDDEN1_SIZE));
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                weightsInputHidden1[i][j] = (rand.nextDouble() * 2 - 1) * limit1;
            }
        }
        
        biasHidden1 = new double[HIDDEN1_SIZE];
        
        // Hidden1 to Hidden2
        weightsHidden1Hidden2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        double limit2 = Math.sqrt(6.0 / (HIDDEN1_SIZE + HIDDEN2_SIZE));
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                weightsHidden1Hidden2[i][j] = (rand.nextDouble() * 2 - 1) * limit2;
            }
        }
        
        biasHidden2 = new double[HIDDEN2_SIZE];
        
        // Hidden2 to Output
        weightsHidden2Output = new double[HIDDEN2_SIZE][OUTPUT_SIZE];
        double limit3 = Math.sqrt(6.0 / (HIDDEN2_SIZE + OUTPUT_SIZE));
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsHidden2Output[i][j] = (rand.nextDouble() * 2 - 1) * limit3;
            }
        }
        
        biasOutput = new double[OUTPUT_SIZE];
    }
    
    private SpaceNeuralNetwork(double[][] wih1, double[] bh1, double[][] wh1h2, double[] bh2, double[][] wh2o, double[] bo) {
        this.weightsInputHidden1 = wih1;
        this.biasHidden1 = bh1;
        this.weightsHidden1Hidden2 = wh1h2;
        this.biasHidden2 = bh2;
        this.weightsHidden2Output = wh2o;
        this.biasOutput = bo;
    }
    
    public double[] predict(double[] inputs) {
        if (inputs.length != INPUT_SIZE) {
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " inputs, got " + inputs.length);
        }
        
        lastInputs = inputs.clone();
        
        // Hidden layer 1
        lastHidden1 = new double[HIDDEN1_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            double sum = biasHidden1[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sum += inputs[j] * weightsInputHidden1[j][i];
            }
            lastHidden1[i] = relu(sum);
        }
        
        // Hidden layer 2
        lastHidden2 = new double[HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            double sum = biasHidden2[i];
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                sum += lastHidden1[j] * weightsHidden1Hidden2[j][i];
            }
            lastHidden2[i] = relu(sum);
        }
        
        // Output layer
        lastOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            lastOutput[i] = biasOutput[i];
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                lastOutput[i] += lastHidden2[j] * weightsHidden2Output[j][i];
            }
            lastOutput[i] = sigmoid(lastOutput[i]);
        }
        
        return lastOutput;
    }
    
    public void resetState() {
        // No-op for feedforward network
    }
    
    private double relu(double x) {
        return Math.max(0, x);
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    public SpaceNeuralNetwork clone() {
        double[][] wih1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            System.arraycopy(weightsInputHidden1[i], 0, wih1[i], 0, HIDDEN1_SIZE);
        }
        
        double[] bh1 = new double[HIDDEN1_SIZE];
        System.arraycopy(biasHidden1, 0, bh1, 0, HIDDEN1_SIZE);
        
        double[][] wh1h2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            System.arraycopy(weightsHidden1Hidden2[i], 0, wh1h2[i], 0, HIDDEN2_SIZE);
        }
        
        double[] bh2 = new double[HIDDEN2_SIZE];
        System.arraycopy(biasHidden2, 0, bh2, 0, HIDDEN2_SIZE);
        
        double[][] wh2o = new double[HIDDEN2_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            System.arraycopy(weightsHidden2Output[i], 0, wh2o[i], 0, OUTPUT_SIZE);
        }
        
        double[] bo = new double[OUTPUT_SIZE];
        System.arraycopy(biasOutput, 0, bo, 0, OUTPUT_SIZE);
        
        return new SpaceNeuralNetwork(wih1, bh1, wh1h2, bh2, wh2o, bo);
    }
    
    public void mutate(double mutationRate) {
        Random rand = new Random();
        
        // Mutate input to hidden1
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsInputHidden1[i][j] += rand.nextGaussian() * 0.2;
                }
            }
        }
        
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden1[i] += rand.nextGaussian() * 0.2;
            }
        }
        
        // Mutate hidden1 to hidden2
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHidden1Hidden2[i][j] += rand.nextGaussian() * 0.2;
                }
            }
        }
        
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden2[i] += rand.nextGaussian() * 0.2;
            }
        }
        
        // Mutate hidden2 to output
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHidden2Output[i][j] += rand.nextGaussian() * 0.2;
                }
            }
        }
        
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasOutput[i] += rand.nextGaussian() * 0.2;
            }
        }
    }
    
    // Getters for visualization
    public double[] getLastInputs() { return lastInputs; }
    public double[] getLastHidden1() { return lastHidden1; }
    public double[] getLastHidden2() { return lastHidden2; }
    public double[] getLastOutput() { return lastOutput; }
    public double[][] getWeightsInputHidden1() { return weightsInputHidden1; }
    public double[][] getWeightsHidden1Hidden2() { return weightsHidden1Hidden2; }
    public double[][] getWeightsHidden2Output() { return weightsHidden2Output; }
    
    public static SpaceNeuralNetwork crossover(SpaceNeuralNetwork parent1, SpaceNeuralNetwork parent2, Random rand) {
        SpaceNeuralNetwork child = parent1.clone();
        
        // Crossover input to hidden1
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsInputHidden1[i][j] = parent2.weightsInputHidden1[i][j];
                }
            }
        }
        
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            if (rand.nextBoolean()) {
                child.biasHidden1[i] = parent2.biasHidden1[i];
            }
        }
        
        // Crossover hidden1 to hidden2
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsHidden1Hidden2[i][j] = parent2.weightsHidden1Hidden2[i][j];
                }
            }
        }
        
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            if (rand.nextBoolean()) {
                child.biasHidden2[i] = parent2.biasHidden2[i];
            }
        }
        
        // Crossover hidden2 to output
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsHidden2Output[i][j] = parent2.weightsHidden2Output[i][j];
                }
            }
        }
        
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            if (rand.nextBoolean()) {
                child.biasOutput[i] = parent2.biasOutput[i];
            }
        }
        
        return child;
    }
}
