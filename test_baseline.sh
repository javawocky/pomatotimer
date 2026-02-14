#!/bin/bash

echo "=== BASELINE ARCHITECTURE TEST ==="
echo "Architecture: 22→32→24→3"
echo "Running 20 iterations of 500 generations each"
echo ""

results_file="baseline_results.txt"
echo "Run,BestFitness,BestScore,Time" > $results_file

for i in {1..20}; do
    echo "--- Run $i/20 ---"
    output=$(java -cp target/classes com.thefryup.pomodoropilot.Main --headless --generations 500 2>&1)
    
    # Extract results
    fitness=$(echo "$output" | grep "Best fitness ever:" | awk '{print $4}')
    score=$(echo "$output" | grep "Best score ever:" | awk '{print $4}')
    time=$(echo "$output" | grep "Total time:" | awk '{print $3}')
    
    echo "$i,$fitness,$score,$time" >> $results_file
    echo "Best Score: $score | Fitness: $fitness | Time: ${time}s"
    echo ""
done

echo "=== RESULTS SUMMARY ==="
echo ""
awk -F',' 'NR>1 {sum+=$3; if($3>max){max=$3} if(NR==2 || $3<min){min=$3}} END {print "Best Score: " max "\nWorst Score: " min "\nAverage Score: " sum/(NR-1)}' $results_file
echo ""
echo "Full results saved to: $results_file"
