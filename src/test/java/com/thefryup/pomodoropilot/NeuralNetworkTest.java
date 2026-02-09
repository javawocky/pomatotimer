package com.thefryup.pomodoropilot;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class NeuralNetworkTest {
    
    @Test
    public void testNetworkCreation() {
        NeuralNetwork nn = new NeuralNetwork();
        assertNotNull(nn);
    }
    
    @Test
    public void testPredictReturnsValidOutput() {
        NeuralNetwork nn = new NeuralNetwork();
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        
        double output = nn.predict(inputs);
        
        assertTrue(output >= 0.0 && output <= 1.0, "Output should be between 0 and 1");
    }
    
    @Test
    public void testPredictWithDifferentInputs() {
        NeuralNetwork nn = new NeuralNetwork();
        
        double[] inputs1 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        double[] inputs2 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        
        double output1 = nn.predict(inputs1);
        double output2 = nn.predict(inputs2);
        
        assertTrue(output1 >= 0.0 && output1 <= 1.0, "Output 1 should be valid");
        assertTrue(output2 >= 0.0 && output2 <= 1.0, "Output 2 should be valid");
    }
    
    @Test
    public void testPredictWithWrongInputSize() {
        NeuralNetwork nn = new NeuralNetwork();
        double[] inputs = {0.5, 0.5, 0.5}; // Only 3 inputs instead of 7
        
        assertThrows(IllegalArgumentException.class, () -> nn.predict(inputs));
    }
    
    @Test
    public void testClone() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork clone = original.clone();
        
        assertNotNull(clone);
        assertNotSame(original, clone, "Clone should be different object");
        
        // Same inputs should produce same outputs
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        double output1 = original.predict(inputs);
        double output2 = clone.predict(inputs);
        
        assertEquals(output1, output2, 0.0001, "Clone should produce same output");
    }
    
    @Test
    public void testMutationChangesWeights() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork mutated = original.clone();
        
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        double outputBefore = mutated.predict(inputs);
        
        // Mutate with 100% rate to ensure changes
        mutated.mutate(1.0, new Random(42));
        
        double outputAfter = mutated.predict(inputs);
        
        assertNotEquals(outputBefore, outputAfter, 0.0001, "Mutation should change output");
    }
    
    @Test
    public void testMutationWithZeroRate() {
        NeuralNetwork original = new NeuralNetwork();
        NeuralNetwork mutated = original.clone();
        
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        double outputBefore = mutated.predict(inputs);
        
        // Mutate with 0% rate - should not change
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
        
        // Child should produce valid output
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        double output = child.predict(inputs);
        
        assertTrue(output >= 0.0 && output <= 1.0, "Child output should be valid");
    }
    
    @Test
    public void testCrossoverProducesDifferentChildren() {
        NeuralNetwork parent1 = new NeuralNetwork();
        NeuralNetwork parent2 = new NeuralNetwork();
        
        NeuralNetwork child1 = NeuralNetwork.crossover(parent1, parent2, new Random(42));
        NeuralNetwork child2 = NeuralNetwork.crossover(parent1, parent2, new Random(123));
        
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        double output1 = child1.predict(inputs);
        double output2 = child2.predict(inputs);
        
        // Different random seeds should produce different children
        assertNotEquals(output1, output2, 0.0001, "Different crossovers should produce different outputs");
    }
    
    @Test
    public void testDeterministicBehavior() {
        NeuralNetwork nn = new NeuralNetwork();
        
        double[] inputs = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0};
        
        double output1 = nn.predict(inputs);
        double output2 = nn.predict(inputs);
        double output3 = nn.predict(inputs);
        
        assertEquals(output1, output2, 0.0001, "Same inputs should always produce same output");
        assertEquals(output2, output3, 0.0001, "Same inputs should always produce same output");
    }
    
    @Test
    public void testOutputRangeWithExtremeInputs() {
        NeuralNetwork nn = new NeuralNetwork();
        
        // Test with all max values
        double[] maxInputs = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        double maxOutput = nn.predict(maxInputs);
        assertTrue(maxOutput >= 0.0 && maxOutput <= 1.0, "Max input output should be in range");
        
        // Test with all min values
        double[] minInputs = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0};
        double minOutput = nn.predict(minInputs);
        assertTrue(minOutput >= 0.0 && minOutput <= 1.0, "Min input output should be in range");
        
        // Test with mixed extreme values
        double[] mixedInputs = {1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.5};
        double mixedOutput = nn.predict(mixedInputs);
        assertTrue(mixedOutput >= 0.0 && mixedOutput <= 1.0, "Mixed input output should be in range");
    }
}
