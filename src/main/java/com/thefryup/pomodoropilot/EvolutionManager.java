package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class EvolutionManager {
    private static final int POPULATION_SIZE = 10;
    private static final double MUTATION_RATE = 0.15;
    private static final int MAX_HISTORY = 30; // Keep last 30 generations
    
    private List<AIPlane> population;
    private int generation;
    private double bestFitnessEver;
    private Random rand;
    private List<Double> fitnessHistory; // Track fitness over time
    
    public EvolutionManager() {
        this.population = new ArrayList<>();
        this.generation = 1;
        this.bestFitnessEver = 0;
        this.rand = new Random();
        this.fitnessHistory = new ArrayList<>();
        
        // Initialize first generation with random brains
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new AIPlane(i % 4)); // Cycle through 4 colors
        }
    }
    
    public List<AIPlane> getPopulation() {
        return population;
    }
    
    public int getGeneration() {
        return generation;
    }
    
    public int getAliveCount() {
        return (int) population.stream().filter(p -> p.alive).count();
    }
    
    public double getBestFitnessThisGen() {
        return population.stream()
            .mapToDouble(AIPlane::getFitness)
            .max()
            .orElse(0);
    }
    
    public double getBestFitnessEver() {
        return bestFitnessEver;
    }
    
    public List<Double> getFitnessHistory() {
        return fitnessHistory;
    }
    
    public boolean allDead() {
        return population.stream().noneMatch(p -> p.alive);
    }
    
    public void evolveNextGeneration() {
        // Sort by fitness
        population.sort(Comparator.comparingDouble(AIPlane::getFitness).reversed());
        
        // Update best fitness ever
        double bestThisGen = population.get(0).getFitness();
        if (bestThisGen > bestFitnessEver) {
            bestFitnessEver = bestThisGen;
        }
        
        // Add to history
        fitnessHistory.add(bestThisGen);
        if (fitnessHistory.size() > MAX_HISTORY) {
            fitnessHistory.remove(0);
        }
        
        System.out.println("Generation " + generation + " complete. Best fitness: " + bestThisGen);
        
        // Create next generation
        List<AIPlane> nextGen = new ArrayList<>();
        
        // 1. Elite: Clone best plane
        nextGen.add(new AIPlane(0, population.get(0).brain.clone()));
        
        // 2. Crossover: Top 3 parents breed
        NeuralNetwork parent1 = population.get(0).brain;
        NeuralNetwork parent2 = population.get(1).brain;
        NeuralNetwork parent3 = population.get(2).brain;
        
        // Parent 1 + 2 -> 2 children
        nextGen.add(new AIPlane(1, NeuralNetwork.crossover(parent1, parent2, rand)));
        nextGen.add(new AIPlane(2, NeuralNetwork.crossover(parent1, parent2, rand)));
        
        // Parent 1 + 3 -> 2 children
        nextGen.add(new AIPlane(3, NeuralNetwork.crossover(parent1, parent3, rand)));
        nextGen.add(new AIPlane(0, NeuralNetwork.crossover(parent1, parent3, rand)));
        
        // Parent 2 + 3 -> 2 children
        nextGen.add(new AIPlane(1, NeuralNetwork.crossover(parent2, parent3, rand)));
        nextGen.add(new AIPlane(2, NeuralNetwork.crossover(parent2, parent3, rand)));
        
        // 3. Random mutations: 3 offspring
        for (int i = 0; i < 3; i++) {
            int parentIdx = rand.nextInt(3);
            NeuralNetwork brain = population.get(parentIdx).brain.clone();
            brain.mutate(MUTATION_RATE * 2, rand); // Higher mutation for diversity
            nextGen.add(new AIPlane(3, brain));
        }
        
        // Apply small mutations to all except elite
        for (int i = 1; i < nextGen.size(); i++) {
            nextGen.get(i).brain.mutate(MUTATION_RATE, rand);
        }
        
        // Assign colors
        for (int i = 0; i < nextGen.size(); i++) {
            nextGen.get(i).colorIndex = i % 4;
        }
        
        population = nextGen;
        generation++;
    }
}
