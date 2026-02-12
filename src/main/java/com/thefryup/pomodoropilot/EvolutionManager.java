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
    
    private int bestScoreEver = 0;
    
    public void updateBestScoreEver() {
        int currentBest = getBestScoreThisGen();
        if (currentBest > bestScoreEver) {
            bestScoreEver = currentBest;
        }
    }
    
    public int getBestScoreThisGen() {
        int bestScore = population.stream()
            .mapToInt(p -> p.score)
            .max()
            .orElse(0);
        return bestScore;
    }
    
    public int getBestScoreEver() {
        return bestScoreEver;
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
        
        // Update best score ever before creating new generation
        updateBestScoreEver();
        
        // Add to history
        fitnessHistory.add(bestThisGen);
        if (fitnessHistory.size() > MAX_HISTORY) {
            fitnessHistory.remove(0);
        }
        
        // Generation complete
        
        // Create next generation - standard genetic algorithm
        List<AIPlane> nextGen = new ArrayList<>();
        
        // Keep top 2 elites
        nextGen.add(new AIPlane(0, population.get(0).brain.clone()));
        nextGen.add(new AIPlane(1, population.get(1).brain.clone()));
        
        // Fill rest with crossover from top 5
        for (int i = 2; i < POPULATION_SIZE; i++) {
            int parent1Idx = rand.nextInt(5);
            int parent2Idx = rand.nextInt(5);
            while (parent2Idx == parent1Idx) {
                parent2Idx = rand.nextInt(5);
            }
            
            NeuralNetwork child = NeuralNetwork.crossover(
                population.get(parent1Idx).brain, 
                population.get(parent2Idx).brain, 
                rand
            );
            child.mutate(MUTATION_RATE, rand);
            nextGen.add(new AIPlane(i % 4, child));
        }
        
        population = nextGen;
        generation++;
    }
    
    public AIPlane getBestPlane() {
        return population.stream()
            .max(Comparator.comparingDouble(AIPlane::getFitness))
            .orElse(null);
    }
}
