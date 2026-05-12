#!/bin/sh

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
MAVEN_VERSION="3.9.9"
MAVEN_HOME="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_HOME/bin/mvn"
MAVEN_TGZ="$WRAPPER_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_BIN" ]; then
  mkdir -p "$WRAPPER_DIR"
  echo "Downloading Apache Maven $MAVEN_VERSION..."
  curl -fsSL "$MAVEN_URL" -o "$MAVEN_TGZ" || {
    echo "Failed to download Maven distribution."
    exit 1
  }
  echo "Extracting Maven..."
  tar -xzf "$MAVEN_TGZ" -C "$WRAPPER_DIR" || {
    echo "Failed to extract Maven distribution."
    exit 1
  }
fi

exec "$MAVEN_BIN" "$@"
