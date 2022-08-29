#!/bin/bash

git checkout main

for version in 2.0.x 2.1.x 3.0.x 4.0.x
do
    git checkout release/$version
    mvn -Prelease release:clean release:prepare release:perform
done

git checkout main    
