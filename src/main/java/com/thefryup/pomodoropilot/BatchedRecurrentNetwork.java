package com.thefryup.pomodoropilot;

/**
 * Batched recurrent neural network for processing multiple ships simultaneously
 * Reduces memory allocation and improves cache locality
 */
public class BatchedRecurrentNetwork {
    private static final int INPUT_SIZE = 23;
    private static final int HIDDEN_SIZE = 64;
    private static final int RECURRENT_SIZE = 48;
    private static final int OUTPUT_SIZE = 3;
    
    private RecurrentNeuralNetwork network;
    
    // Reusable batch buffers (allocated once, reused every frame)
    private double[][] batchInputs;
    private double[][] batchHidden;
    private double[][] batchRecurrent;
    private double[][] batchOutput;
    private int batchSize;
    
    public BatchedRecurrentNetwork(RecurrentNeuralNetwork network, int maxBatchSize) {
        this.network = network;
        this.batchSize = maxBatchSize;
        
        // Pre-allocate batch buffers
        batchInputs = new double[maxBatchSize][INPUT_SIZE];
        batchHidden = new double[maxBatchSize][HIDDEN_SIZE];
        batchRecurrent = new double[maxBatchSize][RECURRENT_SIZE];
        batchOutput = new double[maxBatchSize][OUTPUT_SIZE];
    }
    
    /**
     * Process a batch of inputs through the network
     * @param inputs Array of input vectors [batchSize][23]
     * @param recurrentStates Array of recurrent states [batchSize][48]
     * @param count Number of items in batch (may be less than maxBatchSize)
     * @return Array of output vectors [batchSize][3]
     */
    public double[][] predictBatch(double[][] inputs, double[][] recurrentStates, int count) {
        // Layer 1: Input → Hidden (ReLU) for all items
        for (int b = 0; b < count; b++) {
            for (int i = 0; i < HIDDEN_SIZE; i++) {
                double sum = network.biasHidden[i];
                for (int j = 0; j < INPUT_SIZE; j++) {
                    sum += inputs[b][j] * network.weightsInputHidden[j][i];
                }
                batchHidden[b][i] = relu(sum);
            }
        }
        
        // Layer 2: Hidden + Recurrent → Recurrent (tanh) for all items
        for (int b = 0; b < count; b++) {
            for (int i = 0; i < RECURRENT_SIZE; i++) {
                double sum = network.biasRecurrent[i];
                // Add contribution from hidden layer
                for (int j = 0; j < HIDDEN_SIZE; j++) {
                    sum += batchHidden[b][j] * network.weightsHiddenRecurrent[j][i];
                }
                // Add contribution from previous recurrent state
                for (int j = 0; j < RECURRENT_SIZE; j++) {
                    sum += recurrentStates[b][j] * network.weightsRecurrentRecurrent[j][i];
                }
                batchRecurrent[b][i] = tanh(sum);
            }
            // Update recurrent state for next time step
            System.arraycopy(batchRecurrent[b], 0, recurrentStates[b], 0, RECURRENT_SIZE);
        }
        
        // Layer 3: Recurrent → Output (sigmoid) for all items
        for (int b = 0; b < count; b++) {
            for (int i = 0; i < OUTPUT_SIZE; i++) {
                double sum = network.biasOutput[i];
                for (int j = 0; j < RECURRENT_SIZE; j++) {
                    sum += batchRecurrent[b][j] * network.weightsRecurrentOutput[j][i];
                }
                batchOutput[b][i] = sigmoid(sum);
            }
        }
        
        return batchOutput;
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
}
