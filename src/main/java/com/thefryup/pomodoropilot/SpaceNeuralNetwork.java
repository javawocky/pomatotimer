package com.thefryup.pomodoropilot;

import java.util.Random;

public class SpaceNeuralNetwork {
    private static final int INPUT_SIZE = 24; // 18 raycasts + closest obstacle (x, y, distance, angle) + velocity (vx, vy)
    private static final int HIDDEN1_SIZE = 16;
    private static final int HIDDEN2_SIZE = 12;
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
        
        weightsHidden1Hidden2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                weightsHidden1Hidden2[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasHidden2 = new double[HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            biasHidden2[i] = rand.nextGaussian() * 0.5;
        }
        
        weightsHidden2Output = new double[HIDDEN2_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                weightsHidden2Output[i][j] = rand.nextGaussian() * 0.5;
            }
        }
        
        biasOutput = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            biasOutput[i] = rand.nextGaussian() * 0.5;
        }
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
            throw new IllegalArgumentException("Expected " + INPUT_SIZE + " inputs");
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
        
        // Hidden layer 2
        lastHidden2 = new double[HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            double sum = biasHidden2[i];
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                sum += lastHidden1[j] * weightsHidden1Hidden2[j][i];
            }
            lastHidden2[i] = tanh(sum);
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
    
    public void mutate(double mutationRate, Random rand) {
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
        
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHidden1Hidden2[i][j] += rand.nextGaussian() * 0.3;
                }
            }
        }
        
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            if (rand.nextDouble() < mutationRate) {
                biasHidden2[i] += rand.nextGaussian() * 0.3;
            }
        }
        
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                if (rand.nextDouble() < mutationRate) {
                    weightsHidden2Output[i][j] += rand.nextGaussian() * 0.3;
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
        double[][] wih1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN1_SIZE; j++) {
                wih1[i][j] = rand.nextBoolean() ? 
                    parent1.weightsInputHidden1[i][j] : parent2.weightsInputHidden1[i][j];
            }
        }
        
        double[] bh1 = new double[HIDDEN1_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            bh1[i] = rand.nextBoolean() ? parent1.biasHidden1[i] : parent2.biasHidden1[i];
        }
        
        double[][] wh1h2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN1_SIZE; i++) {
            for (int j = 0; j < HIDDEN2_SIZE; j++) {
                wh1h2[i][j] = rand.nextBoolean() ? 
                    parent1.weightsHidden1Hidden2[i][j] : parent2.weightsHidden1Hidden2[i][j];
            }
        }
        
        double[] bh2 = new double[HIDDEN2_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            bh2[i] = rand.nextBoolean() ? parent1.biasHidden2[i] : parent2.biasHidden2[i];
        }
        
        double[][] wh2o = new double[HIDDEN2_SIZE][OUTPUT_SIZE];
        for (int i = 0; i < HIDDEN2_SIZE; i++) {
            for (int j = 0; j < OUTPUT_SIZE; j++) {
                wh2o[i][j] = rand.nextBoolean() ? 
                    parent1.weightsHidden2Output[i][j] : parent2.weightsHidden2Output[i][j];
            }
        }
        
        double[] bo = new double[OUTPUT_SIZE];
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            bo[i] = rand.nextBoolean() ? parent1.biasOutput[i] : parent2.biasOutput[i];
        }
        
        return new SpaceNeuralNetwork(wih1, bh1, wh1h2, bh2, wh2o, bo);
    }
    
    public double[] getLastInputs() { return lastInputs; }
    public double[] getLastHidden() { return lastHidden1; }
    public double[] getLastHidden1() { return lastHidden1; }
    public double[] getLastHidden2() { return lastHidden2; }
    public double[] getLastOutput() { return lastOutput; }
    public double[] getLastOutputs() { return lastOutput; }
}
