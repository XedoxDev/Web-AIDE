#!/bin/bash

echo -n "Enter files to add: "
read -r pattern

if [ -z "$pattern" ]; then
    pattern="."
    echo "Add all files (Pattern: '.')"
fi

if ! git add -- "$pattern"; then
    echo "Error: failed to load files with pattern: '$pattern'!"
    exit 1
fi

echo -n "Enter commit message: "
read -r commit_message

while [ -z "$commit_message" ]; do
    echo "Error: Commit message cannot be empty!"
    echo -n "Enter commit message: "
    read -r commit_message
done

if git commit -m "$commit_message"; then
    echo "Commit created width message: '$commit_message'"
else
    echo "Error: cannot to create commit!"
    exit 1
fi