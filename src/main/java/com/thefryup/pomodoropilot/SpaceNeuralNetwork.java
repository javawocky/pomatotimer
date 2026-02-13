package com.thefryup.pomodoropilot;

import java.util.Random;

public class SpaceNeuralNetwork {
    private static final int INPUT_SIZE = 22; // 18 raycasts + 2 velocity + 2 heading
    private static final int HIDDEN_SIZE = 12;
    private static final int OUTPUT_SIZE = 3; // left, right, thrust
    
    private double[][] weightsInputHidden;
    private double[] biasHidden;
    private double[][] weightsHiddenOutput;
    private double[] biasOutput;
    
    // Store last activations for visualization
    private double[] lastInputs;
    private double[] lastHidden;
    private double[] lastOutput;
    
    public SpaceNeuralNetwork() {
        Random rand = new Random();
        
        weightsInputHidden = new double[INPUT_SIZE][HIDDEN_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weightsInputHidden[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasHidden = new double[HIDDEN_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            biasHidden[i] = rand.nextGaussian() * 0.5;
        }
        
        weightsHiddenOutput = new double[HIDDEN_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsHiddenOutput[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            biasOutput[i] = rand.nextGaussian() * 0.5;
        }
    }
    
    private SpaceNeuralNetwork(double[][] wih, double[] bh, double[][] who, double[] bo) {
        this.weightsInputHidden = wih;
        this.biasHidden = bh;
        this.weightsHiddenOutput = who;
        this.biasOutput = bo;
    }
    
    public double[] predict(double[] inputs) {
        if (inputs.length != INPUT_SIZE) {
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " inputs");
        }
        
        lastInputs = inputs.clone();
        
        // Hidden layer
        lastHidden = new double[HIDDEN_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            double sum = biasHidden[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sum += inputs[j] * weightsInputHidden[j][i];
            }
            lastHidden[i] = tanh(sum);
        }
        
        // Output layer
        lastOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            lastOutput[i] = biasOutput[i];
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                lastOutput[i] += lastHidden[j] * weightsHiddenOutput[j][i];
            }
            lastOutput[i] = sigmoid(lastOutput[i]);
        }
        
        return lastOutput;
    }
    
    private double tanh(double x) {
        return Math.tanh(x);
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    public SpaceNeuralNetwork clone() {
        double[][] wih = new double[INPUT_SIZE][HIDDEN_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            System.arraycopy(weightsInputHidden[i], 0, wih[i], 0, HIDDEN_SIZE);
        }
        
        double[] bh = new double[HIDDEN_SIZE];
        System.arraycopy(biasHidden, 0, bh, 0, HIDDEN_SIZE);
        
        double[][] who = new double[HIDDEN_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            System.arraycopy(weightsHiddenOutput[i], 0, who[i], 0, OUTPUT_SIZE);
        }
        
        double[] bo = new double[OUTPUT_SIZE];
        System.arraycopy(biasOutput, 0, bo, 0, OUTPUT_SIZE);
        
        return new SpaceNeuralNetwork(wih, bh, who, bo);
    }
    
    public void mutate(double mutationRate, Random rand) {
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsInputHidden[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden[i] += rand.nextGaussian() * 0.3;
            }
        }
        
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHiddenOutput[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasOutput[i] += rand.nextGaussian() * 0.3;
            }
        }
    }
    
    public static SpaceNeuralNetwork crossover(SpaceNeuralNetwork parent1, SpaceNeuralNetwork parent2, Random rand) {
        double[][] wih = new double[INPUT_SIZE][HIDDEN_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                wih[i][j] = rand.nextBoolean() ? 
                    parent1.weightsInputHidden[i][j] : parent2.weightsInputHidden[i][j];
            }
        }
        
        double[] bh = new double[HIDDEN_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            bh[i] = rand.nextBoolean() ? parent1.biasHidden[i] : parent2.biasHidden[i];
        }
        
        double[][] who = new double[HIDDEN_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                who[i][j] = rand.nextBoolean() ? 
                    parent1.weightsHiddenOutput[i][j] : parent2.weightsHiddenOutput[i][j];
            }
        }
        
        double[] bo = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            bo[i] = rand.nextBoolean() ? parent1.biasOutput[i] : parent2.biasOutput[i];
        }
        
        return new SpaceNeuralNetwork(wih, bh, who, bo);
    }
    
    public double[] getLastInputs() { return lastInputs; }
    public double[] getLastHidden() { return lastHidden; }
    public double[] getLastHidden1() { return lastHidden; }
    public double[] getLastHidden2() { return new double[0]; }
    public double[] getLastOutput() { return lastOutput; }
    public double[] getLastOutputs() { return lastOutput; }
}
