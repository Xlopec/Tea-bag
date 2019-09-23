#!/usr/bin/env bash
# enforces code style by formatting the whole codebase
# if current branch is either dev or stage. After that
# performs code style checking and analysis

#branch=$(git rev-parse --abbrev-ref HEAD)
branch="stage"

if [[ ${branch} =~ master|dev|^stage.* ]]; then

    chmod +x gradlew

    if [[ ${branch} != "master" ]]; then
        echo "performing code formatting"
        ./gradlew port_sdk:formatCode
    fi

    echo "performing code style checking and analysis"
    ./gradlew port_sdk:detekt
fi