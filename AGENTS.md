# AGENTS.md

This repository is a benchmark harness for evaluating coding agents. Follow these instructions whenever you work in this repo or in a task worktree under `models/<MODEL_CODE>/<CATEGORY>/<TASK_ID>/workspace/`.

## Project Purpose

- This project evaluates coding agents across six task categories: algorithm, bug fixing, frontend UI, PR review, code analysis, and test generation.
- Public reports must use anonymized Greek model codes such as `CASSANDRA` and `MEDEA`; do not write real model names in public benchmark outputs.
- Most tasks use Keycloak as the repository under test. The standalone exception is `01_algorithm/task_001`, which uses `workspace/algorithm/`.

## Repository Layout

- Canonical task definitions live under `tasks/<CATEGORY>/task_001/`.
- Model-specific task copies live under `models/<MODEL_CODE>/<CATEGORY>/task_001/`.
- Keycloak task worktrees live under `models/<MODEL_CODE>/<CATEGORY>/task_001/workspace/keycloak/`.
- The algorithm task workspace lives under `models/<MODEL_CODE>/01_algorithm/task_001/workspace/algorithm/`.
- Final task reports must be written to `models/<MODEL_CODE>/<CATEGORY>/task_001/result.md`.
- Optional evaluator scores may be written to `score.md` in the task folder.

## Worktree And Git Rules

- Every Keycloak-based task must start from the same pinned Keycloak base branch and base commit for a benchmark run.
- Use branch names in this pattern: `bench/<MODEL_CODE>/<CATEGORY>/<TASK_ID>`.
- Do not modify files outside the active task directory except through the task worktree.
- Do not revert or overwrite unrelated changes. Treat existing edits as user-owned unless explicitly told otherwise.
- Do not use destructive git commands such as `git reset --hard` or checkout-based reverts unless the user explicitly requests them.

## Runtime And Language Rules

- Backend/runtime Java code targets JDK 17.
- Algorithm, bug-fix, and test-generation implementation code should be Java unless the task prompt says otherwise.
- The frontend UI task may modify Keycloak Admin Console TypeScript/React code because that is the native frontend stack.
- Use the existing project tooling and conventions before introducing new dependencies or patterns.
- This benchmark intentionally does not include repo-provided Python validators or scoring scripts.

## Task Execution

- Always read the task's `prompt.md` before starting work.
- Follow `metadata.yaml` for category, scoring, expected output, worktree path, and special task constraints.
- Agents may run normal workspace commands when useful, including search, build, test, lint, and coverage commands.
- Prefer targeted commands for Keycloak because the repository is large.
- For PR review and code-analysis tasks, do not implement fixes unless the prompt explicitly allows tiny local experiments.
- For test-generation tasks, do not change production behavior merely to make tests pass.

## Reporting Requirements

- Update `result.md` at the end of every task.
- Include status, summary, files changed, commands run, validation results, limitations, and any category-specific sections requested by the prompt.
- Include token usage and estimated cost when available:
  - `input_tokens`
  - `output_tokens`
  - `cache_write_tokens`
  - `cache_hit_tokens`
  - `total_tokens`
  - `estimated_cost_usd`
- If cache token counts are unavailable, mark them `unknown`; do not assume zero.
- Use pricing from `models/<MODEL_CODE>/model.yaml` when estimating cost.

## Scoring And Comparison

- Scores use a 1.0 to 5.0 scale in half-point increments only.
- The final benchmark comparison lives in `REPORT.md`.
- Pricing is part of the comparison. Report both quality and cost-efficiency where data is available.
- Keep real model mappings private.

## Setup Notes

- Ubuntu 22.04 setup instructions live in `SETUP.md`.
- Required tools include JDK 17, Maven, Git, Node.js/Corepack for frontend work, OpenCode, and Codex CLI.
- Do not add Docker, Podman, or other container dependencies unless the user explicitly changes that requirement.

## Editing Guidance

- Keep changes scoped to the requested task or documentation update.
- Prefer `rg` for search.
- Preserve the benchmark structure and mirror canonical task updates into both model folders when the user asks to write down a task.
- When updating model-facing task prompts, update the canonical `tasks/...` file and the matching `models/CASSANDRA/...` and `models/MEDEA/...` copies.
- Keep Markdown plain and practical; avoid leaking private model/provider details.