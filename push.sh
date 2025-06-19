#!/bin/bash

echo -n "Do you want to commit changes? (y/n): "
read use_commit

if [ "$use_commit" = "y" ]; then
    bash commit.sh
else
    echo "Skipping commit, pushing existing changes..."
fi

git push

if [ "$use_commit" != "y" ]; then
    echo "Done! Changes pushed without new commit."
fi