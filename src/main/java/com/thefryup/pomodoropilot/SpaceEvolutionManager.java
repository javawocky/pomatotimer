package com.thefryup.pomodoropilot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SpaceEvolutionManager {
    private static final int POPULATION_SIZE = 20;
    private static final double MUTATION_RATE = 0.3; // Balanced exploration
    private static final int ELITE_COUNT = 5; // Keep top 5 (25% of population)
    private static final int MAX_HISTORY = 30; // Keep last 30 generations
    
    private List<AIShip> population;
    private int generation;
    private double bestFitnessEver;
    private Random rand;
    private List<Double> fitnessHistory;
    private int bestScoreEver;
    
    public SpaceEvolutionManager(double startX, double startY) {
        this.population = new ArrayList<>();
        this.generation = 1;
        this.bestFitnessEver = 0;
        this.bestScoreEver = 0;
        this.rand = new Random();
        this.fitnessHistory = new ArrayList<>();
        
        // Initialize first generation with random brains
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new AIShip(startX, startY, i % 4)); // Cycle through 4 colors
        }
    }
    
    public List<AIShip> getPopulation() {
        return population;
    }
    
    public int getGeneration() {
        return generation;
    }
    
    public int getAliveCount() {
        return (int) population.stream().filter(s -> s.alive).count();
    }
    
    public int getBestScoreThisGen() {
        return population.stream()
            .mapToInt(s -> s.score)
            .max()
            .orElse(0);
    }
    
    public int getBestScoreEver() {
        return bestScoreEver;
    }
    
    public double getBestFitnessThisGen() {
        return population.stream()
            .mapToDouble(AIShip::getFitness)
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
        return population.stream().noneMatch(s -> s.alive);
    }
    
    public AIShip getBestShip() {
        return population.stream()
            .filter(s -> s.alive)
            .max(Comparator.comparingDouble(AIShip::getFitness))
            .orElse(population.get(0));
    }
    
    public void evolveNextGeneration(double startX, double startY) {
        // Sort by fitness
        population.sort(Comparator.comparingDouble(AIShip::getFitness).reversed());
        
        // Update best fitness ever
        double bestThisGen = population.get(0).getFitness();
        if (bestThisGen > bestFitnessEver) {
            bestFitnessEver = bestThisGen;
        }
        
        // Update best score ever
        int bestScore = getBestScoreThisGen();
        if (bestScore > bestScoreEver) {
            bestScoreEver = bestScore;
        }
        
        // Add to history
        fitnessHistory.add(bestThisGen);
        if (fitnessHistory.size() > MAX_HISTORY) {
            fitnessHistory.remove(0);
        }
        
        System.out.println("Generation " + generation + " complete. Best fitness: " + (int)bestThisGen + 
                          ", Best score: " + bestScore);
        
        // Create next generation
        List<AIShip> nextGen = new ArrayList<>();
        
        // Keep top 4 elites (40% of population)
        for (int i = 0; i < ELITE_COUNT; i++) {
            nextGen.add(new AIShip(startX, startY, i % 4, population.get(i).brain.clone()));
        }
        
        // Fill rest with crossover and mutation
        while (nextGen.size() < POPULATION_SIZE) {
            // Select two parents (bias towards better performers)
            AIShip parent1 = selectParent();
            AIShip parent2 = selectParent();
            
            // Crossover
            RecurrentNeuralNetwork childBrain = RecurrentNeuralNetwork.crossover(parent1.brain, parent2.brain, rand);
            
            // Mutate
            childBrain.mutate(MUTATION_RATE);
            
            // Create child
            int colorIndex = nextGen.size() % 4;
            nextGen.add(new AIShip(startX, startY, colorIndex, childBrain));
        }
        
        population = nextGen;
        generation++;
    }
    
    /**
     * Process all ships' AI (simple sequential for now)
     */
    public void thinkBatched(List<Meteor> meteors) {
        // Sequential processing - each ship has different brain weights
        population.stream()
            .filter(ship -> ship.alive)
            .forEach(ship -> ship.think(meteors));
    }
    
    private AIShip selectParent() {
        // Tournament selection: pick best of 3 random ships
        AIShip best = population.get(rand.nextInt(population.size()));
        for (int i = 0; i < 2; i++) {
            AIShip candidate = population.get(rand.nextInt(population.size()));
            if (candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
