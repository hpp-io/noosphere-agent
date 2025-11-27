#!/bin/bash
#
# This script sets the HPP_DOCKER_GROUP environment variable based on the operating system
# and then runs docker-compose.

set -e

OS_NAME=$(uname -s)

if [ "$OS_NAME" = "Linux" ]; then
    # On Linux, find the GID of the 'docker' group.
    if getent group docker > /dev/null 2>&1; then
        DOCKER_GID=$(getent group docker | cut -d: -f3)
        echo "Detected Linux. Using 'docker' group GID: $DOCKER_GID"
    else
        echo "Error: 'docker' group not found on this Linux system."
        echo "Please add the current user to the 'docker' group or run with sudo."
        exit 1
    fi
elif [ "$OS_NAME" = "Darwin" ]; then
    # On macOS, the 'docker' group does not exist. We use the 'staff' group's GID as a fallback.
    DOCKER_GID=$(dscl . -read /Groups/staff PrimaryGroupID | awk '{print $2}')
    echo "Detected macOS. Using 'staff' group GID: $DOCKER_GID"
else
    echo "Unsupported operating system: $OS_NAME"
    exit 1
fi

export HPP_DOCKER_GROUP=$DOCKER_GID

# echo "Starting docker-compose with HPP_DOCKER_GROUP=$HPP_DOCKER_GROUP..."
# docker-compose -f src/main/docker/app.yml up

set +e
