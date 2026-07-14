# Evaluation Environment Setup

This guide sets up a fresh Ubuntu 22.04 machine for running the coding-agent benchmark against Keycloak worktrees.

The benchmark contract is:

- Repository under test: Keycloak from GitHub
- Language/runtime target: Java with JDK 17
- Build tool: Maven
- Coding agents: OpenCode and Codex
- Work layout: one Keycloak git worktree per model per Keycloak-based task under `models/<MODEL_CODE>/<CATEGORY>/<TASK_ID>/workspace/keycloak/`. The `01_algorithm/task_001` task is standalone and uses `workspace/algorithm/`.

## 1. System Packages

Update apt and install common development tools:

```bash
sudo apt update
sudo apt install -y \
  ca-certificates \
  curl \
  wget \
  gnupg \
  unzip \
  zip \
  tar \
  git \
  git-lfs \
  build-essential \
  jq \
  ripgrep \
  tree \
  htop \
  tmux \
  software-properties-common
```

Enable Git LFS:

```bash
git lfs install
```

## 2. JDK 17

Install OpenJDK 17:

```bash
sudo apt install -y openjdk-17-jdk
```

Set `JAVA_HOME` for the current user:

```bash
cat <<'EOF' >> ~/.bashrc

# codingBenchmarks Java runtime
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
EOF

source ~/.bashrc
```

Verify:

```bash
java -version
javac -version
echo "$JAVA_HOME"
```

Expected major version: `17`.

## 3. Maven

Install Maven from Ubuntu packages:

```bash
sudo apt install -y maven
```

Verify:

```bash
mvn -version
```

Confirm Maven reports Java 17.

Optional Maven cache location, useful if the machine has a large data disk:

```bash
mkdir -p ~/.m2/repository
```

## 4. Node.js For Keycloak Frontend Work

Some Keycloak frontend tasks may need Node tooling. Install Node.js 20 LTS through NodeSource:

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

Enable Corepack so package managers requested by the repo can be activated:

```bash
sudo corepack enable
node --version
npm --version
corepack --version
```


## 5. OpenCode

Install OpenCode using the official install script:

```bash
curl -fsSL https://opencode.ai/install | bash
```

Alternative install through npm:

```bash
npm install -g opencode-ai
```

Verify:

```bash
opencode --version
```

Launch OpenCode once and connect your provider/API key:

```bash
opencode
```

Inside the OpenCode TUI, use:

```text
/connect
```

Follow the provider login/API-key flow for the model you want to evaluate.


## 6. Codex CLI

Install Codex CLI using the official macOS/Linux standalone installer:

```bash
curl -fsSL https://chatgpt.com/codex/install.sh | sh
```

If you prefer to inspect the installer before running it:

```bash
curl -fsSL https://chatgpt.com/codex/install.sh -o /tmp/codex-install.sh
less /tmp/codex-install.sh
sh /tmp/codex-install.sh
```

Restart your shell or reload your profile if the installer updates your `PATH`.

Verify:

```bash
codex --version
```

Launch Codex once and sign in with the account that has access to the model you want to evaluate:

```bash
codex
```

Use Codex from inside the model/task worktree, the same way as OpenCode. Keep real model names private in benchmark reports; use only the Greek model code such as `CASSANDRA` or `MEDEA`.

## 7. Clone The Benchmark Repo

Clone this benchmark repository:

```bash
git clone <codingBenchmarks-repo-url> codingBenchmarks
cd codingBenchmarks
```

Check the expected model/task structure:

```bash
tree -L 5 models
```

## 8. Clone Keycloak Once

Keep one canonical Keycloak clone under `upstream/keycloak`. It is used only as the source repository for worktrees.

```bash
mkdir -p upstream
git clone https://github.com/keycloak/keycloak.git upstream/keycloak
cd upstream/keycloak
```

Choose and record the benchmark base branch and base commit:

```bash
git fetch origin
git checkout main
git pull --ff-only origin main
BASE_BRANCH=main
BASE_COMMIT=$(git rev-parse HEAD)
echo "$BASE_BRANCH $BASE_COMMIT"
```

For a fair run, every model and task must use the same `BASE_BRANCH` and `BASE_COMMIT`.

## 9. Create Per-Model Per-Task Worktrees

From `upstream/keycloak`, create one worktree per model/task. Example for `CASSANDRA` bug-fix task:

```bash
cd upstream/keycloak
BASE_COMMIT=$(git rev-parse HEAD)

git worktree add \
  ../../models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak \
  -b bench/CASSANDRA/02_bug_fix/task_001 \
  "$BASE_COMMIT"
```

Example for the same bug-fix task for `MEDEA`:

```bash
git worktree add \
  ../../models/MEDEA/02_bug_fix/task_001/workspace/keycloak \
  -b bench/MEDEA/02_bug_fix/task_001 \
  "$BASE_COMMIT"
```

Use this branch pattern for all benchmark worktrees:

```text
bench/<MODEL_CODE>/<CATEGORY>/<TASK_ID>
```

Use this worktree path pattern:

```text
models/<MODEL_CODE>/<CATEGORY>/<TASK_ID>/workspace/keycloak
```

## 10. Run A Coding Agent For A Task

Open a model/task worktree:

```bash
cd models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak
```

Start OpenCode:

```bash
opencode
```

Or start Codex:

```bash
codex
```

Prompt example:

```text
Read ../../prompt.md and complete the task in this Keycloak worktree using Java targeting JDK 17. You may run normal workspace commands if useful. Put your final report in ../../result.md.
```

The coding agent should edit files in `workspace/keycloak/`. The final benchmark report should be written to the sibling task file:

```text
models/CASSANDRA/02_bug_fix/task_001/result.md
```

## 11. Capture Results

After a model finishes a task, inspect:

```bash
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak status --short
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak diff --stat
git -C models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak diff
cat models/CASSANDRA/02_bug_fix/task_001/result.md
```

Score manually using the benchmark's 1.0 to 5.0 half-point scale.

Recommended score file:

```text
models/CASSANDRA/02_bug_fix/task_001/score.md
```

Suggested `score.md` format:

```markdown
# Score

Overall: 4.0 / 5

## Breakdown

- Correctness: 4.0 / 5
- Completeness: 4.0 / 5
- Code Quality: 4.0 / 5
- Reasoning: 4.0 / 5
- Report Quality: 4.0 / 5

## Notes

Short evaluator notes.
```

## 12. Useful Keycloak Commands

The coding agent may choose which commands to run. Useful commands often include:

```bash
./mvnw -version
./mvnw -pl <module> -am test
./mvnw -pl <module> -am -DskipTests compile
```

Keycloak is large, so prefer targeted module builds/tests over full-repo builds unless the task needs broader verification.

## 13. Cleanup Worktrees

When a benchmark run is over and results are saved, remove a worktree from the canonical Keycloak clone:

```bash
cd upstream/keycloak
git worktree remove ../../models/CASSANDRA/02_bug_fix/task_001/workspace/keycloak
```

If Git reports stale metadata:

```bash
git worktree prune
```

## 14. Troubleshooting

Check Java selection:

```bash
update-alternatives --config java
update-alternatives --config javac
```

Clear a corrupted Maven dependency cache only if needed:

```bash
rm -rf ~/.m2/repository/<group-or-artifact-path>
```

Check all Keycloak worktrees:

```bash
git -C upstream/keycloak worktree list
```

Check disk space. Keycloak plus many worktrees and Maven caches can use significant storage:

```bash
df -h
du -sh upstream/keycloak models ~/.m2 2>/dev/null
```

## References

- OpenCode docs: https://opencode.ai/docs/
- OpenCode install script: https://opencode.ai/install
- Codex CLI docs: https://developers.openai.com/codex/cli/
- Codex CLI installer: https://chatgpt.com/codex/install.sh
- Keycloak GitHub repository: https://github.com/keycloak/keycloak


