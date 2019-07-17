#!/bin/bash

rm -rf Problems/Tournament
mkdir Problems/Tournament
 
python3 WorldGenerator.py 1000 Beginner_world_ Problems/Tournament 8 8 10

python3 WorldGenerator.py 1000 Intermediate_world_ Problems/Tournament 16 16 40

python3 WorldGenerator.py 1000 Expert_world_ Problems/Tournament 16 30 99

java -jar ../bin/mine.jar -f Problems/Tournament ../scores/score_t.txt

echo Finished generating worlds!
