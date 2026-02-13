package com.thefryup.pomodoropilot;

import java.util.Random;

public class SpaceNeuralNetwork {
    private static final int INPUT_SIZE = 22; // 18 raycasts + velocity (vx, vy) + rotation (angle, angular velocity)
    private static final int HIDDEN1_SIZE = 16;
    private static final int RECURRENT_SIZE = 6; // Memory layer
    private static final int OUTPUT_SIZE = 3; // left, right, thrust
    
    private double[][] weightsInputHidden1;
    private double[] biasHidden1;
    private double[][] weightsHidden1Recurrent;
    private double[][] weightsRecurrentRecurrent; // Recurrent connections
    private double[] biasRecurrent;
    private double[][] weightsRecurrentOutput;
    private double[] biasOutput;
    
    // Store last activations for visualization
    private double[] lastInputs;
    private double[] lastHidden1;
    private double[] lastRecurrent;
    private double[] lastOutput;
    
    // Recurrent state (persists between frames)
    private double[] recurrentState;
    
    public SpaceNeuralNetwork() {
        Random rand = new Random();
        
        // Input to Hidden1
        weightsInputHidden1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                weightsInputHidden1[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasHidden1 = new double[HIDDEN1_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            biasHidden1[i] = rand.nextGaussian() * 0.5;
        }
        
        // Hidden1 to Recurrent
        weightsHidden1Recurrent = new double[HIDDEN1_SIZE][RECURRENT_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                weightsHidden1Recurrent[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        // Recurrent to Recurrent (memory connections)
        weightsRecurrentRecurrent = new double[RECURRENT_SIZE][RECURRENT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                weightsRecurrentRecurrent[i][j] = rand.nextGaussian() * 0.3; // Smaller weights for stability
            }
        }
        
        biasRecurrent = new double[RECURRENT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            biasRecurrent[i] = rand.nextGaussian() * 0.5;
        }
        
        // Recurrent to Output
        weightsRecurrentOutput = new double[RECURRENT_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsRecurrentOutput[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            biasOutput[i] = rand.nextGaussian() * 0.5;
        }
        
        // Initialize recurrent state to zeros
        recurrentState = new double[RECURRENT_SIZE];
    }
    
    private SpaceNeuralNetwork(double[][] wih1, double[] bh1, double[][] wh1r, double[][] wrr, double[] br, double[][] wro, double[] bo) {
        this.weightsInputHidden1 = wih1;
        this.biasHidden1 = bh1;
        this.weightsHidden1Recurrent = wh1r;
        this.weightsRecurrentRecurrent = wrr;
        this.biasRecurrent = br;
        this.weightsRecurrentOutput = wro;
        this.biasOutput = bo;
        this.recurrentState = new double[RECURRENT_SIZE];
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
            lastHidden1[i] = tanh(sum);
        }
        
        // Recurrent layer (combines hidden1 + previous recurrent state)
        double[] newRecurrent = new double[RECURRENT_SIZE];
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            double sum = biasRecurrent[i];
            // Input from hidden1
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                sum += lastHidden1[j] * weightsHidden1Recurrent[j][i];
            }
            // Input from previous recurrent state (memory)
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                sum += recurrentState[j] * weightsRecurrentRecurrent[j][i];
            }
            newRecurrent[i] = tanh(sum);
        }
        
        // Update recurrent state for next frame
        recurrentState = newRecurrent;
        lastRecurrent = newRecurrent;
        
        // Output layer
        lastOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            lastOutput[i] = biasOutput[i];
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                lastOutput[i] += lastRecurrent[j] * weightsRecurrentOutput[j][i];
            }
            lastOutput[i] = sigmoid(lastOutput[i]);
        }
        
        return lastOutput;
    }
    
    public void resetState() {
        // Reset recurrent state (call when ship dies/respawns)
        recurrentState = new double[RECURRENT_SIZE];
    }
    
    private double tanh(double x) {
        return Math.tanh(x);
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
        
        double[][] wh1r = new double[HIDDEN1_SIZE][RECURRENT_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            System.arraycopy(weightsHidden1Recurrent[i], 0, wh1r[i], 0, RECURRENT_SIZE);
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
        
        return new SpaceNeuralNetwork(wih1, bh1, wh1r, wrr, br, wro, bo);
    }
    
    public void mutate(double mutationRate) {
        Random rand = new Random();
        
        // Mutate input to hidden1
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsInputHidden1[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden1[i] += rand.nextGaussian() * 0.3;
            }
        }
        
        // Mutate hidden1 to recurrent
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHidden1Recurrent[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        // Mutate recurrent to recurrent
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsRecurrentRecurrent[i][j] += rand.nextGaussian() * 0.2; // Smaller mutations for stability
                }
            }
        }
        
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasRecurrent[i] += rand.nextGaussian() * 0.3;
            }
        }
        
        // Mutate recurrent to output
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsRecurrentOutput[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasOutput[i] += rand.nextGaussian() * 0.3;
            }
        }
    }
    
    // Getters for visualization
    public double[] getLastInputs() { return lastInputs; }
    public double[] getLastHidden1() { return lastHidden1; }
    public double[] getLastRecurrent() { return lastRecurrent; }
    public double[] getLastOutput() { return lastOutput; }
    public double[][] getWeightsInputHidden1() { return weightsInputHidden1; }
    public double[][] getWeightsHidden1Recurrent() { return weightsHidden1Recurrent; }
    public double[][] getWeightsRecurrentOutput() { return weightsRecurrentOutput; }
    
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
        
        // Crossover hidden1 to recurrent
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsHidden1Recurrent[i][j] = parent2.weightsHidden1Recurrent[i][j];
                }
            }
        }
        
        // Crossover recurrent to recurrent
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < RECURRENT_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsRecurrentRecurrent[i][j] = parent2.weightsRecurrentRecurrent[i][j];
                }
            }
        }
        
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            if (rand.nextBoolean()) {
                child.biasRecurrent[i] = parent2.biasRecurrent[i];
            }
        }
        
        // Crossover recurrent to output
        for (int i = 0; i < RECURRENT_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextBoolean()) {
                    child.weightsRecurrentOutput[i][j] = parent2.weightsRecurrentOutput[i][j];
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
