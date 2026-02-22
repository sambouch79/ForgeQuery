# QueryForge

**Dynamic SQL Query Generator from JSON configuration**

QueryForge generates SQL SELECT queries from simple JSON mapping files — no code required. Define your tables, joins, conditions, and fields in JSON, and QueryForge produces clean, validated SQL.

---

## Features

- **Full SQL generation** — SELECT, FROM, JOIN, WHERE, GROUP BY, HAVING, ORDER BY, LIMIT/OFFSET
- **Rich condition support** — simple comparisons, IN/NOT IN, IS NULL, BETWEEN, AND/OR nesting, subqueries
- **Field types** — simple columns, SQL functions, CASE WHEN expressions, raw SQL, scalar subqueries
- **JSON validation** — JSON Schema validation before generation
- **Multiple interfaces** — Java library, CLI tool, REST API
- **Oracle compatible** — designed for enterprise Oracle environments

---

## Project Structure

```
queryforge/
├── queryforge-core/    ← Java library (domain model, generator, validator)
├── queryforge-cli/     ← Command line tool (fat JAR)
└── queryforge-api/     ← REST API (Quarkus)
```

---

## Quick Start

### As a Java Library

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.sambouch79</groupId>
    <artifactId>queryforge-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Generate SQL from a JSON file:

```java
MappingLoader loader = new MappingLoader();
Mapping mapping = loader.loadFromFile(Path.of("mapping.json"));

SQLGenerator generator = new SQLGenerator();
String sql = generator.generate(mapping);

System.out.println(sql);
```

Or build a mapping programmatically:

```java
Mapping mapping = Mapping.builder()
    .model("USER_REPORT")
    .version(Version.parse("1.0.0"))
    .schema(Schema.of(
        Table.of("INDIVIDU", "i"),
        List.of(Join.left(Table.of("DOSSIER", "d"), JoinCondition.eq("i.ID", "d.ID_INDIVIDU"))),
        CompositeCondition.and(List.of(
            SimpleCondition.of("i.STATUT", "=", "ACTIF"),
            IsNullCondition.isNull("i.DATE_SUPPRESSION")
        ))
    ))
    .fields(Map.of(
        "nom",    SimpleField.of("i.NOM_INDIVIDU"),
        "statut", CaseField.of(List.of(
            WhenClause.of(SimpleCondition.of("i.STATUT", "=", "A"), "Actif"),
            WhenClause.of(SimpleCondition.of("i.STATUT", "=", "S"), "Suspendu")
        ), "Inconnu")
    ))
    .build();

String sql = new SQLGenerator().generate(mapping);
```

**Output:**
```sql
SELECT
  i.NOM_INDIVIDU AS nom,
  CASE
    WHEN i.STATUT = 'A' THEN 'Actif'
    WHEN i.STATUT = 'S' THEN 'Suspendu'
    ELSE 'Inconnu'
  END AS statut
FROM INDIVIDU i
LEFT JOIN DOSSIER d ON i.ID = d.ID_INDIVIDU
WHERE (i.STATUT = 'ACTIF' AND i.DATE_SUPPRESSION IS NULL)
```

---

### CLI Tool

Build the fat JAR:

```bash
mvn package -pl queryforge-cli
```

Use it:

```bash
# Generate SQL from a mapping file
java -jar queryforge-cli/target/queryforge-cli.jar generate --file mapping.json

# Generate and save to file
java -jar queryforge-cli/target/queryforge-cli.jar generate --file mapping.json --output result.sql

# Validate a single file
java -jar queryforge-cli/target/queryforge-cli.jar validate --file mapping.json

# Validate all JSON files in a directory
java -jar queryforge-cli/target/queryforge-cli.jar validate --dir ./mappings/

# Help
java -jar queryforge-cli/target/queryforge-cli.jar --help
```

---

### REST API

Start the API:

```bash
cd queryforge-api
mvn quarkus:dev
```

Endpoints:

```bash
# Health check
curl http://localhost:8080/api/health

# Validate a mapping
curl -X POST http://localhost:8080/api/validate \
  -H "Content-Type: application/json" \
  -d @mapping.json

# Generate SQL
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d @mapping.json
```

Swagger UI available at: `http://localhost:8080/swagger`

---

## Mapping JSON Format

A mapping file has 4 sections: `model`, `version`, `schema`, and `fields`.

```json
{
  "model": "MY_REPORT",
  "version": "1.0.0",
  "schema": {
    "baseTable": { "type": "table", "name": "INDIVIDU", "alias": "i" },
    "joins": [
      {
        "type": "LEFT",
        "table": { "name": "DOSSIER", "alias": "d" },
        "conditions": [{ "left": "i.ID", "operator": "=", "right": "d.ID_INDIVIDU" }]
      }
    ],
    "filters": {
      "type": "and",
      "conditions": [
        { "type": "simple",  "field": "i.STATUT",          "op": "=",    "value": "ACTIF" },
        { "type": "is_null", "field": "i.DATE_SUPPRESSION", "negated": false },
        { "type": "in",      "field": "i.TYPE",            "values": ["CDI", "CDD"] },
        { "type": "between", "field": "d.ANNEE",           "from": "2020", "to": "2024" }
      ]
    },
    "groupBy":  ["i.TYPE", "d.ANNEE"],
    "having":   { "type": "simple", "field": "COUNT(*)", "op": ">", "value": "5" },
    "orderBy":  [{ "field": "d.ANNEE", "direction": "DESC" }],
    "limit":    100,
    "offset":   0
  },
  "fields": {
    "nom":    { "type": "field",    "path": "i.NOM_INDIVIDU" },
    "annee":  { "type": "field",    "path": "d.ANNEE" },
    "total":  { "type": "raw",      "sql": "COUNT(*)" },
    "ville":  { "type": "function", "name": "NVL", "args": ["i.VILLE", "Inconnue"] },
    "statut": {
      "type": "case",
      "whens": [
        { "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "A" }, "then": "Actif" },
        { "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "S" }, "then": "Suspendu" }
      ],
      "else": "Inconnu"
    }
  }
}
```

### Field Types

| Type | Description | Example |
|---|---|---|
| `field` | Direct column reference | `{ "type": "field", "path": "i.NOM" }` |
| `function` | SQL function | `{ "type": "function", "name": "NVL", "args": ["i.NOM", "?"] }` |
| `case` | CASE WHEN expression | see above |
| `raw` | Raw SQL expression | `{ "type": "raw", "sql": "COUNT(*)" }` |
| `subquery` | Scalar subquery | `{ "type": "subquery", "query": { ... } }` |

### Condition Types

| Type | Description | Example |
|---|---|---|
| `simple` | Field operator value | `{ "type": "simple", "field": "i.STATUT", "op": "=", "value": "A" }` |
| `and` | Logical AND | `{ "type": "and", "conditions": [...] }` |
| `or` | Logical OR | `{ "type": "or", "conditions": [...] }` |
| `in` | IN / NOT IN | `{ "type": "in", "field": "i.CODE", "values": ["A", "B"], "negated": false }` |
| `is_null` | IS NULL / IS NOT NULL | `{ "type": "is_null", "field": "i.DATE", "negated": false }` |
| `between` | BETWEEN | `{ "type": "between", "field": "d.ANNEE", "from": "2020", "to": "2024" }` |
| `subquery_condition` | EXISTS / IN subquery | `{ "type": "subquery_condition", "op": "EXISTS", "query": { ... } }` |
| `raw` | Raw SQL condition | `{ "type": "raw", "sql": "ROWNUM < 100" }` |

### Bind Parameters

Use `:paramName` syntax for bind parameters — they are passed through without quoting:

```json
{ "type": "simple", "field": "i.ANNEE", "op": ">=", "value": ":anneeMin" }
```

Generates: `i.ANNEE >= :anneeMin`

---

## Building from Source

**Requirements:** Java 21, Maven 3.8+

```bash
git clone https://github.com/sambouch79/queryforge.git
cd queryforge

# Build all modules
mvn install

# Run tests
mvn test

# Build CLI fat JAR
mvn package -pl queryforge-cli

# Start the API in dev mode
cd queryforge-api && mvn quarkus:dev
```

---

## Requirements

- Java 21
- Maven 3.8+
- Quarkus 3.15+ (for the API module only)

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

## Author

[Sam Bouche](https://github.com/sambouch79)