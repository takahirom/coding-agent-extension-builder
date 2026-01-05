#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TEMP_DIR="$PROJECT_ROOT/temp"
AGENTSKILLS_DIR="$TEMP_DIR/agentskills"
GENERATED_DIR="$TEMP_DIR/generated-skills"

echo "=== Agent Skills Test ==="

# 1. Clone agentskills repo if not exists
if [ ! -d "$AGENTSKILLS_DIR" ]; then
    echo "Cloning agentskills repo..."
    git clone https://github.com/agentskills/agentskills.git "$AGENTSKILLS_DIR"
else
    echo "agentskills repo already exists, pulling latest..."
    (cd "$AGENTSKILLS_DIR" && git pull)
fi

# 2. Run Kotlin tests that generate skills to temp/generated-skills
echo "Running generation tests..."
"$PROJECT_ROOT/gradlew" -p "$PROJECT_ROOT" :coding-agent-extension-builder:test --tests "*GenerationTest*"

# 3. Validate generated skills with skills-ref
echo "Setting up skills-ref..."
cd "$AGENTSKILLS_DIR/skills-ref"

# Install dependencies with uv (required for Python 3.11+)
if ! command -v uv &> /dev/null; then
    echo "Error: uv is required (skills-ref needs Python 3.11+)"
    echo "Install with: curl -LsSf https://astral.sh/uv/install.sh | sh"
    exit 1
fi

echo "Installing skills-ref with uv..."
uv sync

# 4. Validate each generated skill
echo "Validating generated skills..."
validation_failed=0
if [ -d "$GENERATED_DIR" ]; then
    while IFS= read -r skill_dir; do
        echo "Validating: $skill_dir"
        if ! uv run skills-ref validate "$skill_dir"; then
            echo "  FAILED: $skill_dir"
            validation_failed=1
        fi
    done < <(find "$GENERATED_DIR" -name "SKILL.md" -exec dirname {} \;)
else
    echo "No generated skills found at $GENERATED_DIR"
    echo "Create a test that writes skills to this directory"
    exit 1
fi

if [ $validation_failed -eq 1 ]; then
    echo "=== FAILED: Some validations failed ==="
    exit 1
fi

echo "=== Done ==="
