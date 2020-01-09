#!/usr/bin/env bash
# checks code style and performs code base analysis
# should reside in Git's hook directory

branch=$(git rev-parse --abbrev-ref HEAD)

if [[ ${branch} =~ master|^dev.*|^stage.* ]]; then

    chmod +x ./gradlew

    echo "performing code style checking and analysis"
    ./gradlew elm-core-api:detekt
fi