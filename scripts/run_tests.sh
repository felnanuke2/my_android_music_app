#!/bin/bash

# Script to run tests and generate coverage report locally
# Usage: ./scripts/run_tests.sh

set -e

echo "🧪 Running Android Music App Tests & Coverage"
echo "============================================="

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Run lint checks
echo "🔍 Running lint checks..."
./gradlew lintDebug

# Run unit tests
echo "🧪 Running unit tests..."
./gradlew testDebugUnitTest

# Generate coverage report
echo "📊 Generating coverage report..."
./gradlew jacocoTestReport

# Check if coverage report was generated
COVERAGE_REPORT="mymusicapp/build/reports/jacoco/jacocoTestReport/html/index.html"
if [ -f "$COVERAGE_REPORT" ]; then
    echo "✅ Coverage report generated successfully!"
    echo "📈 Coverage report location: $COVERAGE_REPORT"
    
    # Extract coverage percentage (if available)
    if command -v grep &> /dev/null; then
        COVERAGE=$(grep -o "Total[^%]*%" "$COVERAGE_REPORT" | head -1 | grep -o "[0-9]*%" || echo "N/A")
        echo "📊 Overall Coverage: $COVERAGE"
    fi
    
    # Open coverage report in default browser (macOS/Linux)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "🌐 Opening coverage report in browser..."
        open "$COVERAGE_REPORT"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "🌐 Opening coverage report in browser..."
        xdg-open "$COVERAGE_REPORT" &> /dev/null || echo "Please open $COVERAGE_REPORT manually"
    fi
else
    echo "❌ Coverage report not found!"
    exit 1
fi

echo ""
echo "🎉 Tests completed successfully!"
echo "📊 View detailed coverage report at: $COVERAGE_REPORT"
