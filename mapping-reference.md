# QueryForge — Mapping Reference

Complete reference for the JSON mapping configuration format.

---

## Structure globale

```json
{
  "model":   "MY_REPORT",
  "version": "1.0.0",
  "schema":  { ... },
  "fields":  { ... }
}
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `model` | string | ✅ | Identifiant du mapping (`[A-Z0-9_]+`) |
| `version` | string | ✅ | Version sémantique (`MAJOR.MINOR.PATCH`) |
| `schema` | object | ✅ | Définition des tables, joins, et clauses SQL |
| `fields` | object | ✅ | Champs à sélectionner (au moins 1) |

---

## schema

```json
"schema": {
  "baseTable": { ... },
  "joins":     [ ... ],
  "filters":   { ... },
  "groupBy":   [ ... ],
  "having":    { ... },
  "orderBy":   [ ... ],
  "limit":     100,
  "offset":    0
}
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `baseTable` | Table ou DerivedTable | ✅ | Table principale du FROM |
| `joins` | Join[] | ❌ | Jointures |
| `filters` | Condition | ❌ | Clause WHERE |
| `groupBy` | string[] | ❌ | Colonnes du GROUP BY |
| `having` | Condition | ❌ | Clause HAVING |
| `orderBy` | OrderByField[] | ❌ | Clause ORDER BY |
| `limit` | integer | ❌ | Nombre maximum de lignes |
| `offset` | integer | ❌ | Décalage (pagination) |

---

## Table

Table simple dans FROM ou dans un JOIN.

```json
{ "type": "table", "name": "INDIVIDU", "alias": "i" }
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `type` | `"table"` | ❌ | Type de source (défaut: table) |
| `name` | string | ✅ | Nom de la table |
| `alias` | string | ✅ | Alias SQL (1-5 caractères, minuscules) |

**SQL généré :** `INDIVIDU i`

---

## DerivedTable

Sous-requête utilisée comme table dans le FROM.

```json
{
  "type": "derived",
  "alias": "sub",
  "query": { <Mapping> }
}
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `type` | `"derived"` | ✅ | Indique une derived table |
| `alias` | string | ✅ | Alias de la sous-requête |
| `query` | Mapping | ✅ | Mapping imbriqué |

**SQL généré :**
```sql
FROM (
  SELECT ...
  FROM ...
) sub
```

---

## Join

```json
{
  "type": "LEFT",
  "table": { "name": "DOSSIER", "alias": "d" },
  "conditions": [
    { "left": "i.ID_INDIVIDU", "operator": "=", "right": "d.ID_INDIVIDU" }
  ]
}
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `type` | enum | ✅ | `INNER`, `LEFT`, `RIGHT`, `FULL` |
| `table` | Table | ✅ | Table à joindre |
| `conditions` | JoinCondition[] | ✅ | Conditions ON (au moins 1) |

### JoinCondition

```json
{ "left": "i.ID", "operator": "=", "right": "d.ID_INDIVIDU" }
```

| Propriété | Valeurs | Description |
|---|---|---|
| `operator` | `=` `!=` `<>` `<` `>` `<=` `>=` | Opérateur de comparaison |

**SQL généré :** `LEFT JOIN DOSSIER d ON i.ID_INDIVIDU = d.ID_INDIVIDU`

---

## Fields

Les champs définissent ce qui sera dans le SELECT. La clé est l'alias SQL.

```json
"fields": {
  "mon_alias": { "type": "...", ... }
}
```

### SimpleField — Référence de colonne

```json
{ "type": "field", "path": "i.NOM_INDIVIDU" }
```

**SQL généré :** `i.NOM_INDIVIDU AS mon_alias`

---

### FunctionField — Fonction SQL

```json
{ "type": "function", "name": "NVL", "args": ["i.NOM", "Inconnu"] }
```

| Propriété | Description |
|---|---|
| `name` | Nom de la fonction SQL |
| `args` | Arguments (colonnes ou littéraux) |

Les arguments contenant un `.` sont traités comme des références de colonnes, les autres sont quotés automatiquement.

**SQL généré :** `NVL(i.NOM, 'Inconnu') AS mon_alias`

Fonctions supportées dans la validation JSON Schema : `COALESCE`, `NVL`, `UPPER`, `LOWER`, `TRIM`, `COUNT`, `SUM`, `AVG`, `MAX`, `MIN`, `CONCAT`

---

### RawField — SQL brut

```json
{ "type": "raw", "sql": "COUNT(*)" }
```

Aucune transformation appliquée — utiliser avec précaution.

**SQL généré :** `COUNT(*) AS mon_alias`

---

### SubqueryField — Sous-requête scalaire

```json
{
  "type": "subquery",
  "query": {
    "model": "inner",
    "version": "1.0.0",
    "schema": {
      "baseTable": { "type": "table", "name": "DOSSIER", "alias": "d" },
      "filters": { "type": "simple", "field": "d.ID_INDIVIDU", "op": "=", "value": "i.ID_INDIVIDU" }
    },
    "fields": { "val": { "type": "raw", "sql": "MAX(d.ANNEE)" } }
  }
}
```

**SQL généré :**
```sql
(
  SELECT MAX(d.ANNEE) AS val
  FROM DOSSIER d
  WHERE d.ID_INDIVIDU = i.ID_INDIVIDU
) AS mon_alias
```

---

### CaseField — Expression CASE WHEN

```json
{
  "type": "case",
  "whens": [
    {
      "when": { "type": "simple", "field": "i.STATUT", "op": "=", "value": "A" },
      "then": "Actif"
    },
    {
      "when": { "type": "is_null", "field": "i.STATUT", "negated": false },
      "then": "Non défini"
    }
  ],
  "else": "Inconnu"
}
```

| Propriété | Type | Requis | Description |
|---|---|---|---|
| `whens` | WhenClause[] | ✅ | Branches WHEN/THEN (au moins 1) |
| `else` | string | ❌ | Valeur ELSE (omis si absent) |

**Valeurs de `then` et `else` :**
- `:param` → bind parameter (pas de quotes)
- `"42"` → numérique (pas de quotes)
- `"i.NOM"` → référence colonne (contient un `.`, pas de quotes)
- `"Actif"` → littéral string (quoté automatiquement)

**SQL généré :**
```sql
CASE
  WHEN i.STATUT = 'A' THEN 'Actif'
  WHEN i.STATUT IS NULL THEN 'Non défini'
  ELSE 'Inconnu'
END AS mon_alias
```

---

## Conditions (WHERE / HAVING)

### SimpleCondition

```json
{ "type": "simple", "field": "i.STATUT", "op": "=", "value": "ACTIF" }
```

| Propriété | Valeurs | Description |
|---|---|---|
| `op` | `=` `!=` `<>` `<` `>` `<=` `>=` | Opérateur |
| `value` | string | Valeur (formatée automatiquement) |

**Formatage automatique des valeurs :**
- `:anneeMin` → `:anneeMin` (bind parameter)
- `"2024"` → `2024` (numérique)
- `"ACTIF"` → `'ACTIF'` (littéral string)

---

### CompositeCondition — AND / OR

```json
{
  "type": "and",
  "conditions": [
    { "type": "simple", "field": "i.STATUT", "op": "=", "value": "ACTIF" },
    { "type": "is_null", "field": "i.DATE_SUPPRESSION", "negated": false }
  ]
}
```

Supporte le nesting — `AND` peut contenir des `OR` et vice-versa :

```json
{
  "type": "and",
  "conditions": [
    { "type": "simple", "field": "i.STATUT", "op": "=", "value": "ACTIF" },
    {
      "type": "or",
      "conditions": [
        { "type": "simple", "field": "i.TYPE", "op": "=", "value": "CDI" },
        { "type": "simple", "field": "i.TYPE", "op": "=", "value": "CDD" }
      ]
    }
  ]
}
```

**SQL généré :** `(i.STATUT = 'ACTIF' AND (i.TYPE = 'CDI' OR i.TYPE = 'CDD'))`

---

### InCondition — IN / NOT IN

```json
{ "type": "in", "field": "i.TYPE_CONTRAT", "values": ["CDI", "CDD"], "negated": false }
```

| Propriété | Description |
|---|---|
| `values` | Liste de valeurs (au moins 1) |
| `negated` | `true` → NOT IN, `false` → IN (défaut: false) |

**SQL généré :** `i.TYPE_CONTRAT IN ('CDI', 'CDD')`

---

### IsNullCondition — IS NULL / IS NOT NULL

```json
{ "type": "is_null", "field": "i.DATE_SUPPRESSION", "negated": false }
```

| `negated` | SQL généré |
|---|---|
| `false` | `i.DATE_SUPPRESSION IS NULL` |
| `true` | `i.DATE_SUPPRESSION IS NOT NULL` |

---

### BetweenCondition — BETWEEN

```json
{ "type": "between", "field": "d.ANNEE", "from": "2020", "to": "2024" }
```

Supporte les bind parameters : `"from": ":anneeMin", "to": ":anneeMax"`

**SQL généré :** `d.ANNEE BETWEEN 2020 AND 2024`

---

### SubqueryCondition — EXISTS / IN subquery

```json
{
  "type": "subquery_condition",
  "op": "EXISTS",
  "query": { <Mapping> }
}
```

| `op` | SQL généré |
|---|---|
| `EXISTS` | `EXISTS (SELECT ...)` |
| `NOT EXISTS` | `NOT EXISTS (SELECT ...)` |
| `IN` | `field IN (SELECT ...)` |
| `NOT IN` | `field NOT IN (SELECT ...)` |
| `=`, `>`, `<` ... | `field = (SELECT ...)` |

Pour `IN`, `NOT IN` et les comparaisons scalaires, ajouter `"field"` :

```json
{
  "type": "subquery_condition",
  "field": "i.ID_INDIVIDU",
  "op": "IN",
  "query": { <Mapping> }
}
```

---

### RawCondition — SQL brut

```json
{ "type": "raw", "sql": "ROWNUM < 100" }
```

Aucune transformation — utiliser avec précaution.

---

## OrderByField

```json
{ "field": "d.ANNEE", "direction": "DESC" }
```

| `direction` | Description |
|---|---|
| `ASC` | Ordre croissant (défaut) |
| `DESC` | Ordre décroissant |

---

## Bind Parameters

Les bind parameters sont préfixés par `:` et passent sans transformation dans le SQL généré — compatibles Oracle/JPA.

```json
{ "type": "simple", "field": "i.ANNEE_NAISSANCE", "op": ">=", "value": ":anneeMin" }
```

**SQL généré :** `i.ANNEE_NAISSANCE >= :anneeMin`

---

## Exemple complet

```json
{
  "model": "STATS_CONTRATS",
  "version": "1.0.0",
  "schema": {
    "baseTable": { "type": "table", "name": "INDIVIDU", "alias": "i" },
    "joins": [
      {
        "type": "INNER",
        "table": { "name": "DOSSIER", "alias": "d" },
        "conditions": [{ "left": "i.ID_INDIVIDU", "operator": "=", "right": "d.ID_INDIVIDU" }]
      }
    ],
    "filters": {
      "type": "and",
      "conditions": [
        { "type": "simple",  "field": "i.STATUT",           "op": "=",    "value": "ACTIF" },
        { "type": "is_null", "field": "i.DATE_SUPPRESSION",  "negated": false },
        { "type": "in",      "field": "i.TYPE_CONTRAT",     "values": ["CDI", "CDD"] },
        { "type": "between", "field": "d.ANNEE",            "from": ":anneeMin", "to": ":anneeMax" }
      ]
    },
    "groupBy": ["i.TYPE_CONTRAT", "d.ANNEE"],
    "having":  { "type": "simple", "field": "COUNT(*)", "op": ">", "value": "5" },
    "orderBy": [
      { "field": "d.ANNEE",        "direction": "DESC" },
      { "field": "i.TYPE_CONTRAT", "direction": "ASC" }
    ],
    "limit":  100,
    "offset": 0
  },
  "fields": {
    "typeContrat": { "type": "field",    "path": "i.TYPE_CONTRAT" },
    "annee":       { "type": "field",    "path": "d.ANNEE" },
    "total":       { "type": "raw",      "sql": "COUNT(*)" },
    "nomComplet":  { "type": "function", "name": "NVL", "args": ["i.NOM", "Inconnu"] },
    "statutLib": {
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

**SQL généré :**
```sql
SELECT
  i.TYPE_CONTRAT AS typeContrat,
  d.ANNEE AS annee,
  COUNT(*) AS total,
  NVL(i.NOM, 'Inconnu') AS nomComplet,
  CASE
    WHEN i.STATUT = 'A' THEN 'Actif'
    WHEN i.STATUT = 'S' THEN 'Suspendu'
    ELSE 'Inconnu'
  END AS statutLib
FROM INDIVIDU i
INNER JOIN DOSSIER d ON i.ID_INDIVIDU = d.ID_INDIVIDU
WHERE (i.STATUT = 'ACTIF' AND i.DATE_SUPPRESSION IS NULL AND i.TYPE_CONTRAT IN ('CDI', 'CDD') AND d.ANNEE BETWEEN :anneeMin AND :anneeMax)
GROUP BY i.TYPE_CONTRAT, d.ANNEE
HAVING COUNT(*) > 5
ORDER BY d.ANNEE DESC, i.TYPE_CONTRAT ASC
LIMIT 100 OFFSET 0
```