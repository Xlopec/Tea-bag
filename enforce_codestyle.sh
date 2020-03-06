#!/usr/bin/env bash
# checks code style and performs code base analysis
# should reside in Git's hook directory

branch=$(git rev-parse --abbrev-ref HEAD)

die () {
    echo
    echo "$*"
    echo
    exit 1
}

if [[ ${branch} =~ master|^dev.*|^release.* ]]; then

    chmod +x ./gradlew

    echo "performing code style checking and analysis"

    ./gradlew detektAll > /dev/null || die "code analysis has failed!"

    echo "done performing code analysis"

fi