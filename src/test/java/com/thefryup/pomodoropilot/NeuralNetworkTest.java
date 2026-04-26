package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class NeuralNetworkTest {
    
    // 15 inputs: 9 raycasts + 5 target inputs + 1 velocity
    private static final int INPUT_SIZE = 15;
    
    private double[] makeInputs(double value) {
        double[] inputs = new double[INPUT_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) inputs[i] = value;
        return inputs;
    }
    
    private double[] makeSampleInputs() {
        return new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0, 0.5, 0.0, 0.5, 0.0, 0.5};
    }
    
    @Test
    public void testNetworkCreation() {
        NeuralNetwork nn = new NeuralNetwork();
        assertNotNull(nn);
    }
    
    @Test
    public void testPredictReturnsValidOutput() {
        NeuralNetwork nn = new NeuralNetwork();
        double output = nn.predict(makeSampleInputs());
        assertTrue(output >= 0.0 && output <= 1.0, "Output should be between 0 and 1");
    }
    
    @Test
    public void testPredictWithDifferentInputs() {
        NeuralNetwork nn = new NeuralNetwork();
        
        double output1 = nn.predict(makeInputs(1.0));
        double output2 = nn.predict(makeInputs(0.0));
        
        assertTrue(output1 >= 0.0 && output1 <= 1.0, "Output 1 should be valid");
        assertTrue(output2 >= 0.0 && output2 <= 1.0, "Output 2 should be valid");
    }
    
    @Test
    public void testPredictWithWrongInputSize() {
        NeuralNetwork nn = new NeuralNetwork();
        double[] inputs = {0.5, 0.5, 0.5}; // Only 3 inputs instead of 15
        assertThrows(IllegalArgumentException.class, () -> nn.predict(inputs));
    }
    
    @Test
    public void testClone() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork clone = original.clone();
        
        assertNotNull(clone);
        assertNotSame(original, clone, "Clone should be different object");
        
        double[] inputs = makeSampleInputs();
        assertEquals(original.predict(inputs), clone.predict(inputs), 0.0001, "Clone should produce same output");
    }
    
    @Test
    public void testMutationChangesWeights() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork mutated = original.clone();
        
        double[] inputs = makeSampleInputs();
        double outputBefore = mutated.predict(inputs);
        
        mutated.mutate(1.0, new Random(42));
        double outputAfter = mutated.predict(inputs);
        
        assertNotEquals(outputBefore, outputAfter, 0.0001, "Mutation should change output");
    }
    
    @Test
    public void testMutationWithZeroRate() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork mutated = original.clone();
        
        double[] inputs = makeSampleInputs();
        double outputBefore = mutated.predict(inputs);
        
        mutated.mutate(0.0, new Random(42));
        double outputAfter = mutated.predict(inputs);
        
        assertEquals(outputBefore, outputAfter, 0.0001, "Zero mutation rate should not change output");
    }
    
    @Test
    public void testCrossover() {
        NeuralNetwork parent1 = new NeuralNetwork();
        NeuralNetwork parent2 = new NeuralNetwork();
        
        NeuralNetwork child = NeuralNetwork.crossover(parent1, parent2, new Random(42));
        assertNotNull(child);
        
        double output = child.predict(makeSampleInputs());
        assertTrue(output >= 0.0 && output <= 1.0, "Child output should be valid");
    }
    
    @Test
    public void testCrossoverProducesDifferentChildren() {
        NeuralNetwork parent1 = new NeuralNetwork();
        NeuralNetwork parent2 = new NeuralNetwork();
        
        NeuralNetwork child1 = NeuralNetwork.crossover(parent1, parent2, new Random(42));
        NeuralNetwork child2 = NeuralNetwork.crossover(parent1, parent2, new Random(123));
        
        double[] inputs = makeSampleInputs();
        assertNotEquals(child1.predict(inputs), child2.predict(inputs), 0.0001,
            "Different crossovers should produce different outputs");
    }
    
    @Test
    public void testDeterministicBehavior() {
        NeuralNetwork nn = new NeuralNetwork();
        double[] inputs = makeSampleInputs();
        
        double output1 = nn.predict(inputs);
        double output2 = nn.predict(inputs);
        double output3 = nn.predict(inputs);
        
        assertEquals(output1, output2, 0.0001, "Same inputs should always produce same output");
        assertEquals(output2, output3, 0.0001, "Same inputs should always produce same output");
    }
    
    @Test
    public void testOutputRangeWithExtremeInputs() {
        NeuralNetwork nn = new NeuralNetwork();
        
        double maxOutput = nn.predict(makeInputs(1.0));
        assertTrue(maxOutput >= 0.0 && maxOutput <= 1.0, "Max input output should be in range");
        
        double[] minInputs = new double[INPUT_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) minInputs[i] = (i == 9 || i == 11) ? -1.0 : 0.0;
        double minOutput = nn.predict(minInputs);
        assertTrue(minOutput >= 0.0 && minOutput <= 1.0, "Min input output should be in range");
        
        double[] mixedInputs = new double[INPUT_SIZE];
        for (int i = 0; i < INPUT_SIZE; i++) mixedInputs[i] = (i % 2 == 0) ? 1.0 : 0.0;
        double mixedOutput = nn.predict(mixedInputs);
        assertTrue(mixedOutput >= 0.0 && mixedOutput <= 1.0, "Mixed input output should be in range");
    }
}
