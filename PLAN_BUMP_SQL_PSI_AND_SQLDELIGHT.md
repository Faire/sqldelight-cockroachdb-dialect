# Plan — Bump `app.cash.sql-psi:core` 0.5.2→0.7.3 and `sqldelight` 2.1.0→2.3.2

## Executive summary

This repo (`Faire/sqldelight-cockroachdb-dialect`) is a CockroachDB dialect built on top of the SQLDelight PostgreSQL dialect, which in turn sits on top of `sql-psi`. We need to bump both core deps together (they are released together).

The unit test surface is `CockroachDBFixturesTest`, a parameterized runner that consumes:
- Local cockroachdb fixtures (`fixtures_cockroachdb`)
- ALL ANSI sql-psi fixtures (`fixtures`) minus an explicit exclusion list
- ALL PostgreSQL fixtures (`fixtures_postgresql`) minus an explicit exclusion list

The dependency upgrades introduce a number of new fixtures and grammar changes upstream that will (a) compile fine, but (b) cause new fixture tests to fail because the cockroach grammar overrides several PostgreSQL rules (`extension_stmt`, `alter_table_rules`) without including newly added postgres syntax (policies, enums, functions, comment-on, row-level security, set-storage-parameter). The integration-testing module needs Docker to run, which is unavailable in this environment, but the core dialect tests run locally.

The strategy is:
1. Bump versions.
2. Build & fix any compile breaks in `CockroachDBDialect` / mixins / grammar.
3. Run `./gradlew :cockroachdb-dialect:test` and triage each failing fixture. For each:
   - Either **exclude** the fixture (preferred for new pg features that are tested elsewhere upstream and whose behavior is irrelevant to cockroachdb — and which would require expanding the grammar to support).
   - Or copy/adapt locally (if cockroachdb has an analogous behavior; e.g. the `multiple-column-where` -> `multiple-column-where-ansi` precedent).
4. Verify tests pass, run spotless, commit.

I am **not** going to expand the cockroachdb grammar to add support for POLICY, ENUM, FUNCTION, COMMENT ON, ROW LEVEL SECURITY, SET STORAGE PARAMETER, etc. in this PR — that's a separate scope expansion. The minimal change is to keep current cockroach feature parity while making the test suite pass on the new dependency versions.

## Phase 1 — Bump versions

### Files to edit

`gradle/libs.versions.toml`:

```toml
sql-psi = "0.5.2"      -> "0.7.3"
sqldelight = "2.1.0"   -> "2.3.2"
```

No other version bumps in this PR. Kotlin (2.3.20), spotless (8.4.0), grammar-kit-composer (0.1.12), idea (231.9392.1), maven-publishing (0.35.0), assertj/postgres-jdbc/slf4j/testcontainers stay the same.

### Acceptance criteria
- `./gradlew --no-daemon dependencies` (or `./gradlew :cockroachdb-dialect:dependencies`) shows the new versions resolved against Maven Central.
- `./gradlew :cockroachdb-dialect:compileKotlin :cockroachdb-dialect:compileTestKotlin :cockroachdb-dialect:compileTestFixturesKotlin` succeeds (after any necessary code/grammar fixes — Phase 2).
- `./gradlew :cockroachdb-dialect:generateGrammarSource` (or whatever the grammar-kit-composer task is called) succeeds; review generated parser/PSI for any compile-blockers.

## Phase 2 — Fix compile / grammar regressions

### Investigation summary (informed by upstream diffs)

I diffed `sql-psi@0.5.2 vs 0.7.3` and `sqldelight@2.1.0 vs 2.3.2`. Relevant API-surface changes:

- `app.cash.sql-psi:core` — the public `SqlCreateIndexStmtImpl` and `SqlDropIndexStmtImpl` constructors are unchanged (`(node: ASTNode)` and `(stub, type)` form), so our cockroachdb `CreateIndexMixin(node: ASTNode?)` and `DropIndexMixin(node: ASTNode?)` extensions still compile.
- The PostgreSQL `CreateIndexMixin` switched from `extends = "...CreateIndexMixin"` to `mixin = ...`, and got a new stub-based constructor; **but cockroachdb’s grammar overrides the rule entirely with its own `extends = "...cockroachdb.grammar.mixins.CreateIndexMixin"`**, so this internal pg refactor doesn’t affect us.
- `PostgreSqlDialect`/`SqlDelightDialect` API: unchanged at the public surface we use.
- `app.cash.sqldelight:compiler-env` and `:postgresql-dialect` test-fixtures jars: still expose `PostgresqlTestFixtures` with the same shape.

### Possible compile breaks (and remedies)

I expect compile to succeed with no code changes in `cockroachdb-dialect` source files. If anything fails:

1. **Grammar generation failure** — `CockroachDB.bnf` references PostgreSQL impl classes (e.g. `PostgreSqlAlterTableRulesImpl`, `PostgreSqlExtensionStmtImpl`). These are generated and their existence depends on the rule still being defined in pg’s bnf. All such referenced rules continue to exist in pg 2.3.2.
   - Action: if generation fails, inspect failed file under `cockroachdb-dialect/build/grammars/` and update the cockroachdb bnf to match new pg base classes.

2. **Possible token deletions**: I will check that all SqlTypes `parserImports` referenced in cockroach bnf still exist (e.g. `SqlTypes.WITHOUT`, `SqlTypes.STRING`, etc.). They do, based on the diff.

3. **The new pg `column_name ::= id | string` (overrides ansi)**: cockroach references `<<columnNameExt <<column_name_real>>>>` from sql-psi (ansi). Pg overrides this. Cockroach uses `ansi_column_name`, not pg's `column_name`. Should not conflict.

### Acceptance criteria
- `./gradlew :cockroachdb-dialect:assemble` succeeds.
- `./gradlew :cockroachdb-dialect:compileTestKotlin :cockroachdb-dialect:compileTestFixturesKotlin` succeed.

## Phase 3 — Triage and fix failing fixture tests

After versions bump, the parameterized fixture test will run two new categories of fixtures (auto-pulled from upstream test-fixtures jars):

### 3a. New ANSI sql-psi fixtures (in `fixtures` JAR)

| Fixture | Expectation | Likely action |
|---|---|---|
| `ansi-string-literal` | Tests `''` escaping in string literals (sql-psi 0.7.3 lexer change). Should work — pg dialect inherits the same lexer. | Run, expect pass. If parse error, consider what's wrong. |
| `fts5-hidden-columns` | FTS5 + `oid` no-column error. FTS5 is `CREATE VIRTUAL TABLE ... USING fts5(...)`. Postgres parser accepts virtual tables (inherited from base ansi). The expected error is `No column found with name oid`. May rely on FTS5-specific schema handling. | Run; if error matches, pass. If not, **add to `excludedAnsiFixtures`** with comment `Excluded since we're a Postgres-derived dialect not validating FTS5 hidden columns`. |
| `fts5-tables` (modified) | Adds 4 new fts5 options (`locale=1`, `columnsize=0`, `contentless_delete=1`, `contentless_unindexed=1`). The `numeric_literal` allowed in `module_argument_def` now means `locale=1` and `columnsize=0` parse. | Should pass. Verify after run. |

### 3b. New / modified PostgreSQL fixtures (in `postgresql-dialect` test-fixtures JAR)

These will fail on cockroachdb because the cockroach grammar overrides `extension_stmt` and `alter_table_rules` and **does not** include the new productions. We will exclude them via `excludedPgSqlFixtures`:

| Fixture | Reason | Action |
|---|---|---|
| `alter-policy` | New: `ALTER POLICY ...` — pg `extension_stmt` adds `alter_policy_stmt`; cockroach `extension_stmt` does not include it. CockroachDB does support policies but adding grammar is out of scope for this PR. | Exclude with comment. |
| `create-policy` | Same as above (`create_policy_stmt`). | Exclude. |
| `drop-policy` | Same (`drop_policy_stmt`). | Exclude. |
| `comment-on` | New: `comment_on_extension_stmt`; not in cockroach `extension_stmt`. | Exclude. |
| `create-enum` | New: `create_enum_type_stmt`, `drop_type_stmt`, `alter_type_stmt`; not in cockroach `extension_stmt`. | Exclude. |
| `drop-function` | New: `drop_function_stmt`; not in cockroach `extension_stmt`. | Exclude. |
| `drop-trigger` | New: `drop_trigger_stmt` is now in pg `stmt` rule directly (not extension_stmt). Cockroach inherits pg `stmt`? — no, cockroach overrides `stmt` indirectly via `overrides`/inheritance. Need to verify. May be picked up automatically. If parse fails, exclude. | Run-then-decide; default to exclude. |
| `create-or-replace-trigger` | Same situation — pg added `create_trigger_stmt`/`create_or_replace_trigger`. Not in cockroach. | Run-then-decide; default to exclude. |
| `for-update-invalid-of` | Tests new `FOR UPDATE OF tbl` validation (compound_select_stmt added `for_locking_clause`). Cockroach inherits pg's `compound_select_stmt`, so should work. | Run; if it parses but error positions/messages differ, may need a `replaceRules` entry or exclusion. Default: keep included. |
| `for-update` (modified) | Same, modified. | Run; default keep. |
| `is_json_expressions` | New `is_json_expression`. pg `extension_expr` added it. Cockroach doesn't override `extension_expr` so should be inherited. | Run; default keep. |
| `postgis` | Geometry types + ST_* functions. pg `extension_expr` added `geometry_setsrid_function_stmt`/`geometry_point_function_stmt` and `geometry_data_type`/`geography_data_type`. Cockroach doesn't override `column_type` for these, but does override `int_data_type`, `string_data_type`, `date_data_type`, `blob_data_type`, `big_int_data_type`, `generated_clause` (each with `override = true`). Pg's `column_type` includes `geometry_data_type` etc. **as new alternatives** which cockroach does not exclude. Should work. | Run; default keep. |
| `cte-materialized` | Tests CTE `MATERIALIZED` / `NOT MATERIALIZED`. pg added `cte_materialized` rule; cockroach inherits. | Run; default keep. |
| `alter-table-row-level-security` | New: `alter_table_row_level_security`; cockroach `alter_table_rules` does NOT include it. Will fail to parse. | Exclude. |
| `alter-table-set-storage-parameter` | New: `alter_set_storage_parameters`; cockroach `alter_table_rules` does NOT include it. (Cockroach has `alter_table_set_storage_options` which is different — it’s for cockroach’s own syntax.) | Exclude. |
| `alter-table-alter-column` (modified) | New `USING <<expr '-1'>>` form replacing column::type. Cockroach delegates `{alter_table_alter_column}` to pg's. Should work. | Run; default keep. |
| `alter-table-drop-column` (modified) | Adds `IF EXISTS`. pg added `if_exists`; rule still defined the same way for cockroach delegation `{alter_table_drop_column}`. Inherited. | Run; default keep. |
| `json_functions` (modified) | Lots of new operators, `jsonb_agg(... FILTER (...))`, `jsonb_object_agg`, etc. Cockroach inherits. | Run; default keep. |
| `set` (modified) | Adds `SET LOCAL test.test = 'test';`. Cockroach uses `{set_stmt}` from pg in `extension_stmt`. Inherited. | Run; default keep. |
| `localtimestamp-literals`, `localtimestamp-with-precission`, `timestamp-with-precission` (modified) | Removed CREATE TRIGGER. Should be lower-burden. | Run; default keep. |

### 3c. Cockroachdb-local fixture changes

Currently, `multiple-column-where-ansi` exists as a local copy of the upstream `multiple-column-where`. Upstream content unchanged in 0.7.3. The replaceRules entry `'(', ')', ',', '.', <binary like operator real>, BETWEEN or IN expected, got ','` -> `'#-', '(', ')', ',', '.', ... <jsona binary operator real> ...` — this rule is **dead code** because the corresponding ansi fixture is excluded. It only exists because upstream `multiple-column-where-ansi` would inherit it; but that local fixture has no `failure.txt`. Leave it alone (no behavior change), or optionally remove it during this PR for cleanliness. **Decision: leave as-is** (out of scope of bump).

### Implementation steps for Phase 3

Edit `cockroachdb-dialect/src/test/kotlin/com/faire/sqldelight/dialects/cockroachdb/CockroachDBFixturesTest.kt`:

1. Append to `excludedPgSqlFixtures` (with explanatory comments):
   - `alter-policy`
   - `create-policy`
   - `drop-policy`
   - `comment-on`
   - `create-enum`
   - `drop-function`
   - `alter-table-row-level-security`
   - `alter-table-set-storage-parameter`

   (And conditionally, after running tests: `drop-trigger`, `create-or-replace-trigger` if they fail.)

2. Run tests; for each remaining failure, decide between:
   - Add to `excludedPgSqlFixtures` if cockroach simply doesn't support the feature.
   - Add a `replaceRules` entry if the failure is due to cockroach having a slightly different error message / token expectation.
   - Add to `excludedAnsiFixtures` if it's an upstream fixture that cockroach can't reasonably support.

### Acceptance criteria for Phase 3
- `./gradlew :cockroachdb-dialect:test` passes (all parameterized fixtures green).
- The exclusion list and comments make it clear *why* each fixture is excluded.

## Phase 4 — Lint, integration-testing module

1. **Spotless**: Run `./gradlew spotlessCheck` and `./gradlew spotlessApply` to keep formatting compliant.

2. **Integration testing**: `:integration-testing:test` needs a Docker daemon (testcontainers + cockroachdb image). **Docker is not available in this environment** (`docker` binary not found, `dockerd` not running). I will:
   - Run `./gradlew :integration-testing:assemble` and `./gradlew :integration-testing:compileKotlin :integration-testing:compileTestKotlin` to verify it compiles against the new sqldelight version (the SQLDelight gradle plugin runs at `assemble` time and could fail if the dialect/runtime contracts changed).
   - **NOT** run `./gradlew :integration-testing:test` (it would error trying to start Docker). The CI on PR will run the full test suite; that's where integration tests will get exercised.
   - If `:integration-testing` compile/assemble fails, address there. Most likely changes:
     - Generated code shape may have changed (sqldelight 2.3.2 simplifies generated default queries using constructor references — should be source-compatible).
     - Insert/update/delete now return Long row counts (was already in 2.1.0 per its release notes — actually the change happened *into* 2.1.0 — so we should not see a delta from 2.1.0 to 2.3.2 here).
   - If the SQLDelight gradle plugin or compiler-env API needs adjustments, those will surface as gradle plugin errors or generated source compile errors.

### Acceptance criteria
- `./gradlew :cockroachdb-dialect:test :cockroachdb-dialect:assemble :integration-testing:assemble :integration-testing:compileTestKotlin spotlessCheck` all pass.
- (Optionally) `./gradlew check -x :integration-testing:test` passes.

## Phase 5 — Commit & push

- One commit titled e.g. `Bump sql-psi to 0.7.3 and sqldelight to 2.3.2`. Body should mention which fixtures are excluded and why.
- Push to a feature branch and open a draft PR.

## Risk / out-of-scope notes

- **Not in scope**: adding cockroach grammar support for new pg features (policies, enums, functions, comments, row-level security, set storage parameter). These are valid CockroachDB SQL but the dialect didn't support them in v0.7.0 and adding them would require new mixins, type resolution updates, and is its own work item.
- **Not in scope**: bumping kotlin/idea versions, even though `idea = 231.9392.1` is older than what newer sqldelight typically pairs with. SQLDelight 2.3.2's Postgres test fixtures are loaded from the `postgresql-dialect:test-fixtures` jar (just .s files), so no IntelliJ runtime upgrade is required in this repo.
- **Risk**: there’s a chance upstream sqldelight bumped its embedded `sql-psi` to a version that conflicts with our explicit `0.7.3`. SQLDelight 2.3.2’s gradle.properties pin sql-psi at 0.7.3 too (verified via the release date alignment 2026-03-13 / 2026-03-16), so they’re compatible.
- **Risk**: Test-fixtures resource layout may shift. I’ll inspect the jars under `~/.gradle/caches/modules-2/files-2.1/app.cash.sql-psi/core/0.7.3/.../*-test-fixtures.jar` and `app.cash.sqldelight/postgresql-dialect/2.3.2/.../*-test-fixtures.jar` if needed.
- **Risk**: ABI break we missed at compile-time that surfaces only at runtime in fixtures (e.g. Schema/PSI tree changes). Triaged during Phase 3 by reading the assertion error and the generated tree dump.

## Test strategy summary

```
# Compile
./gradlew :cockroachdb-dialect:compileKotlin :cockroachdb-dialect:compileTestKotlin :cockroachdb-dialect:compileTestFixturesKotlin
./gradlew :integration-testing:assemble :integration-testing:compileTestKotlin

# Unit tests (fixture parameterized tests)
./gradlew :cockroachdb-dialect:test --info

# Format
./gradlew spotlessCheck
```

Integration tests (`:integration-testing:test`) are not runnable locally without Docker; rely on CI.
