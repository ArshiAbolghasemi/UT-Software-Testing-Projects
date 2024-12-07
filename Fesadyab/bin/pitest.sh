#!/bin/bash

# Run Maven with PIT Mutation Testing and the `withHistory` option
mvn clean -DwithHistory test-compile org.pitest:pitest-maven:mutationCoverage

# Check if the command succeeded
if [ $? -eq 0 ]; then
    echo "Mutation testing completed successfully."
else
    echo "Mutation testing failed."
    exit 1
fi

