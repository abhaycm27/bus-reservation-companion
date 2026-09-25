#!/usr/bin/env bash
set -euo pipefail

# Cross-platform build-and-run script for the embedded Java HTTP server.
# Works on macOS / Linux and in Git Bash on Windows. For native Windows cmd
# use the existing run.bat. Requires JDK 17+ on PATH.

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

echo "Checking for JDK (javac/java)..."
if ! command -v javac >/dev/null 2>&1; then
  echo "ERROR: javac not found. Install JDK 17+ and ensure javac is on PATH." >&2
  exit 1
fi
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: java not found. Install JDK 17+ and ensure java is on PATH." >&2
  exit 1
fi

mkdir -p bin
echo "Compiling Java sources into bin/ ..."
JAVA_SRCS=$(find src -name "*.java")
if [ -z "$JAVA_SRCS" ]; then
  echo "No Java sources found under src/." >&2
  exit 1
fi

javac -d bin -sourcepath src $(find src -name "*.java")
echo "Compilation successful."

echo "Launching WebServer (http://localhost:8080/) ..."
# Try to open the browser in a cross-platform way (best-effort)
if command -v xdg-open >/dev/null 2>&1; then
  xdg-open "http://localhost:8080/" >/dev/null 2>&1 || true
elif command -v open >/dev/null 2>&1; then
  open "http://localhost:8080/" || true
elif [ -n "${WSL_DISTRO_NAME-}" ]; then
  powershell.exe start "http://localhost:8080/" >/dev/null 2>&1 || true
fi

java -cp bin com.busreservation.server.WebServer
