#!/bin/bash

echo "Adding all files to commit..."
git add .

echo -n "Enter commit message: "
read commit_message

if [ -z "$commit_message" ]; then
    echo "Error: Commit message cannot be empty!"
    exit 1
fi

git commit -m "$commit_message"
git push

echo "Done! Changes pushed with message: '$commit_message'"