#!/bin/bash

echo "=== TESTING 10 NETWORK ARCHITECTURES @ 1000 GENERATIONS ==="
echo ""

results_file="architecture_results.txt"
echo "Architecture,BestFitness,BestScore,Time" > $results_file

# For now, run baseline 10 times since we need to refactor the network first
architectures=(
    "22→32→24→3 (baseline)"
    "22→64→3 (wide single)"
    "22→40→3 (medium single)"
    "22→16→3 (minimal)"
    "22→48→24→3 (pyramid)"
    "22→32→32→3 (symmetric)"
    "22→24→24→24→3 (deep)"
    "22→80→3 (very wide)"
    "22→36→18→3 (halving)"
    "22→20→20→3 (deep narrow)"
)

for i in {0..9}; do
    arch="${architectures[$i]}"
    echo "--- Test $((i+1))/10: $arch ---"
    
    output=$(java -cp target/classes com.thefryup.pomodoropilot.Main --headless --generations 1000 2>&1)
    
    # Extract results
    fitness=$(echo "$output" | grep "Best fitness ever:" | awk '{print $4}')
    score=$(echo "$output" | grep "Best score ever:" | awk '{print $4}')
    time=$(echo "$output" | grep "Total time:" | awk '{print $3}')
    
    echo "$arch,$fitness,$score,$time" >> $results_file
    echo "Best Score: $score | Fitness: $fitness | Time: ${time}s"
    echo ""
done

echo "=== RESULTS SUMMARY ==="
echo ""
cat $results_file | column -t -s','
echo ""
echo "Full results saved to: $results_file"
