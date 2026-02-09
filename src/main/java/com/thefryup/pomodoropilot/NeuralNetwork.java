package com.thefryup.pomodoropilot;

import java.util.Random;

public class NeuralNetwork {
    private static final int INPUT_SIZE = 7;
    private static final int HIDDEN_SIZE = 10;
    private static final int OUTPUT_SIZE = 1;
    
    private double[][] weightsInputHidden;  // 7x10
    private double[] biasHidden;            // 10
    private double[][] weightsHiddenOutput; // 10x1
    private double[] biasOutput;            // 1
    
    public NeuralNetwork() {
        Random rand = new Random();
        
        // Initialize weights with small random values
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
    
    // Constructor for cloning with specific weights
    public NeuralNetwork(double[][] wih, double[] bh, double[][] who, double[] bo) {
        this.weightsInputHidden = wih;
        this.biasHidden = bh;
        this.weightsHiddenOutput = who;
        this.biasOutput = bo;
    }
    
    public double predict(double[] inputs) {
        if (inputs.length != INPUT_SIZE) {
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " inputs");
        }
        
        // Hidden layer
        double[] hidden = new double[HIDDEN_SIZE];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            double sum = biasHidden[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sum += inputs[j] * weightsInputHidden[j][i];
            }
            hidden[i] = tanh(sum);
        }
        
        // Output layer
        double output = biasOutput[0];
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            output += hidden[i] * weightsHiddenOutput[i][0];
        }
        
        return sigmoid(output);
    }
    
    private double tanh(double x) {
        return Math.tanh(x);
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    public NeuralNetwork clone() {
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
        
        return new NeuralNetwork(wih, bh, who, bo);
    }
    
    public void mutate(double mutationRate, Random rand) {
        // Mutate input-hidden weights
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsInputHidden[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        // Mutate hidden biases
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden[i] += rand.nextGaussian() * 0.3;
            }
        }
        
        // Mutate hidden-output weights
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHiddenOutput[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        // Mutate output biases
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasOutput[i] += rand.nextGaussian() * 0.3;
            }
        }
    }
    
    public static NeuralNetwork crossover(NeuralNetwork parent1, NeuralNetwork parent2, Random rand) {
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
        
        return new NeuralNetwork(wih, bh, who, bo);
    }
}
