#!/bin/bash

echo "=== NEURAL NETWORK ARCHITECTURE EXPERIMENTS ==="
echo "Architecture: 22→32→24→3 (current baseline)"
echo ""

for i in 1 2; do
    echo "--- Run $i/2 ---"
    java -cp target/classes com.thefryup.pomodoropilot.Main --headless --generations 500
    echo ""
done

echo "=== EXPERIMENTS COMPLETE ==="
