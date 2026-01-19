#!/bin/bash

# Prime Mover JMH Benchmarks Runner
# Convenience script for running benchmarks with common configurations

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
JAR="${SCRIPT_DIR}/target/benchmarks.jar"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

usage() {
    cat <<EOF
Usage: $0 [OPTIONS] [BENCHMARK_PATTERN]

Run Prime Mover JMH benchmarks

OPTIONS:
    -a, --all           Run all benchmarks (default)
    -e, --events        Run event throughput benchmarks only
    -c, --continuations Run continuation throughput benchmarks only
    -t, --tracking      Run tracking overhead benchmarks only
    -s, --spectrum      Run spectrum tracking benchmarks only
    -m, --memory        Run memory overhead benchmarks only
    -p, --prof PROFILER Enable profiler (gc, stack, perfasm)
    -o, --output FILE   Export results to JSON file
    -l, --list          List available benchmarks
    -h, --help          Show this help message

EXAMPLES:
    $0                                  # Run all benchmarks
    $0 -e                               # Run event throughput only
    $0 -m -p gc                         # Run memory benchmarks with GC profiler
    $0 -o results.json                  # Run all and export to JSON
    $0 EventThroughputBenchmark         # Run specific benchmark class
    $0 eventNoPayload                   # Run specific benchmark method

EOF
}

check_jar() {
    if [ ! -f "$JAR" ]; then
        echo -e "${RED}Error: Benchmark JAR not found at $JAR${NC}"
        echo -e "${YELLOW}Building benchmarks...${NC}"
        cd "$PROJECT_DIR"
        ./mvnw clean install -pl benchmarks -am
        if [ $? -ne 0 ]; then
            echo -e "${RED}Build failed${NC}"
            exit 1
        fi
    fi
}

run_benchmark() {
    local pattern="$1"
    local extra_args="${@:2}"

    echo -e "${GREEN}Running benchmark: ${BLUE}$pattern${NC}"
    echo -e "${YELLOW}Extra args: $extra_args${NC}"
    echo ""

    java -jar "$JAR" "$pattern" $extra_args
}

# Parse arguments
PATTERN=""
EXTRA_ARGS=""

while [[ $# -gt 0 ]]; do
    case $1 in
        -a|--all)
            PATTERN=""
            shift
            ;;
        -e|--events)
            PATTERN="EventThroughputBenchmark"
            shift
            ;;
        -c|--continuations)
            PATTERN="ContinuationThroughputBenchmark"
            shift
            ;;
        -t|--tracking)
            PATTERN="TrackingOverheadBenchmark"
            shift
            ;;
        -s|--spectrum)
            PATTERN="SpectrumTrackingBenchmark"
            shift
            ;;
        -m|--memory)
            PATTERN="MemoryOverheadBenchmark"
            shift
            ;;
        -p|--prof)
            EXTRA_ARGS="$EXTRA_ARGS -prof $2"
            shift 2
            ;;
        -o|--output)
            EXTRA_ARGS="$EXTRA_ARGS -rf json -rff $2"
            shift 2
            ;;
        -l|--list)
            check_jar
            java -jar "$JAR" -l
            exit 0
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            PATTERN="$1"
            shift
            ;;
    esac
done

# Check JAR exists
check_jar

# Run benchmark
echo -e "${GREEN}Prime Mover JMH Benchmarks${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

if [ -z "$PATTERN" ]; then
    echo -e "${YELLOW}Running all benchmarks...${NC}"
    echo ""
fi

run_benchmark "$PATTERN" $EXTRA_ARGS

echo ""
echo -e "${GREEN}Benchmark complete!${NC}"
