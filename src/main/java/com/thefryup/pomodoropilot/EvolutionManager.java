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
        
        System.out.println("Generation " + generation + " complete. Best fitness: " + bestThisGen);
        
        // Create next generation
        List<AIPlane> nextGen = new ArrayList<>();
        
        // Early generations: more diversity, less champion copying
        // Later generations: more champion copying as quality improves
        boolean earlyGeneration = generation < 20;
        double bestFitness = population.get(0).getFitness();
        boolean lowQualityChampion = bestFitness < 50; // Champion hasn't passed many obstacles
        
        if (earlyGeneration || lowQualityChampion) {
            // Early/low-quality: Keep only 1 elite, more diversity
            nextGen.add(new AIPlane(0, population.get(0).brain.clone()));
            
            // More crossover diversity from top 7
            for (int i = 0; i < 7; i++) {
                int parent1Idx = rand.nextInt(7);
                int parent2Idx = rand.nextInt(7);
                while (parent2Idx == parent1Idx) {
                    parent2Idx = rand.nextInt(7);
                }
                
                NeuralNetwork child = NeuralNetwork.crossover(
                    population.get(parent1Idx).brain, 
                    population.get(parent2Idx).brain, 
                    rand
                );
                child.mutate(MUTATION_RATE * 0.8, rand); // Moderate mutation
                nextGen.add(new AIPlane(i % 4, child));
            }
            
            // Random mutations for exploration
            for (int i = 0; i < 2; i++) {
                int parentIdx = rand.nextInt(8);
                NeuralNetwork brain = population.get(parentIdx).brain.clone();
                brain.mutate(MUTATION_RATE * 1.5, rand);
                nextGen.add(new AIPlane(i % 4, brain));
            }
        } else {
            // Later/high-quality: More champion copying
            nextGen.add(new AIPlane(0, population.get(0).brain.clone()));
            nextGen.add(new AIPlane(1, population.get(1).brain.clone()));
            nextGen.add(new AIPlane(2, population.get(2).brain.clone()));
            
            // Champion clones with light mutation
            for (int i = 0; i < 3; i++) {
                NeuralNetwork brain = population.get(0).brain.clone();
                brain.mutate(MUTATION_RATE * 0.3, rand); // Very light mutation
                nextGen.add(new AIPlane(i % 4, brain));
            }
            
            // Crossover from top 5
            for (int i = 0; i < 3; i++) {
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
                child.mutate(MUTATION_RATE * 0.5, rand);
                nextGen.add(new AIPlane(i % 4, child));
            }
            
            // One explorer
            NeuralNetwork brain = population.get(0).brain.clone();
            brain.mutate(MUTATION_RATE * 2.0, rand);
            nextGen.add(new AIPlane(3, brain));
        }
        
        // Assign colors
        for (int i = 0; i < nextGen.size(); i++) {
            nextGen.get(i).colorIndex = i % 4;
        }
        
        population = nextGen;
        generation++;
    }
}
