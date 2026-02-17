# ⚡ QueryForge - Dynamic SQL Query Generator

> Transform JSON configurations into powerful SQL queries

**Status:** 🏗️ Under Development (v1.0.0-SNAPSHOT)  
**Author:** Sam Bouchenafa (@sambouch79)  
**Motivation:** Real-world challenges at CNAV - implementing best practices that were initially prototyped but not fully realized

---

## 🎯 What is QueryForge?

QueryForge is a Java library that generates SQL queries from JSON configuration files, eliminating the need to hardcode SQL in your application.

### The Problem You Had

```java
// Traditional approach at CNAV
public class CourrierOD2Mapper {
    public String generateSQL(int individuId) {
        return "SELECT " +
            "NVL(a.COMPL_DEST, d.COMPL_DEST) AS APO02, " +
            "CASE i.SEXE WHEN 0 THEN 'Monsieur' ELSE 'Madame' END AS DES06, " +
            // ... 200 lines of hardcoded SQL
            "FROM CIVI.INDIVIDU i " +
            "LEFT JOIN CIVI.DOSSIER d ON i.INDEX_DOSSIER = d.INDEX_DOSSIER " +
            // ... 10 more joins
            "WHERE i.INDEX_INDIVIDU = ?";
    }
}
// Repeat for EVERY document type = Maintenance nightmare!
```

### The Solution You Wanted

```json
{
  "model": "OD2",
  "version": "1.0.0",
  "schema": {
    "baseTable": { "n": "CIVI.INDIVIDU", "alias": "i" },
    "joins": [
      {
        "type": "LEFT",
        "table": { "n": "CIVI.DOSSIER", "alias": "d" },
        "conditions": [
          { "left": "i.INDEX_DOSSIER", "operator": "=", "right": "d.INDEX_DOSSIER" }
        ]
      }
    ]
  },
  "fields": {
    "APO02": {
      "type": "function",
      "n": "NVL",
      "args": ["a.COMPL_DEST", "d.COMPL_DEST"]
    },
    "DES06": {
      "type": "raw",
      "sql": "CASE i.SEXE WHEN 0 THEN 'Monsieur' ELSE 'Madame' END"
    }
  }
}
```

**Benefits:**
- ✅ No more SQL in Java code
- ✅ JSON Schema validation (the feature you wanted!)
- ✅ Semantic versioning (the feature you wanted!)
- ✅ New document type = New JSON file, zero deployment
- ✅ Configuration managed by business analysts

---

## 🏗️ Project Structure

```
queryforge/
├── queryforge-core/       # ⚡ Core library (use this!)
├── queryforge-cli/        # 🖥️ Command-line tool
├── queryforge-server/     # 🌐 REST API server
└── docs/                  # 📚 Documentation
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL (for development)

### Installation

```bash
# Clone
git clone https://github.com/sambouch79/queryforge.git
cd queryforge

# Build
mvn clean install

# Run tests
mvn test
```

### Library Usage

```xml
<dependency>
    <groupId>io.github.sambouch79</groupId>
    <artifactId>queryforge-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
// Load mapping from JSON
Mapping mapping = MappingLoader.load("my-mapping.json");

// Generate SQL
SQLGenerator generator = new SQLGenerator();
String sql = generator.generate(mapping, params);

// Execute
List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params);
```

---

## ✨ Features

### ✅ Implemented
- Semantic versioning (`Version` class)
- Field types: `SimpleField`, `FunctionField`, `RawField`
- Schema modeling: `Table`, `Join`, `JoinCondition`
- Comprehensive unit tests (11 tests passing)

### 🚧 In Progress
- **JSON Schema validation** (the feature they ignored!)
- **Version compatibility checks** (the feature they ignored!)
- SQL generation engine
- Query optimization

### 📋 Planned
- Output formatters (XML, PDF, JSON)
- CLI tool
- REST API
- PostgreSQL + Oracle support

---

## 🧪 Running Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=VersionTest

# With coverage
mvn test jacoco:report
```

---

## 🎯 Why This Project Exists

At CNAV, I created a POC for dynamic document generation that solved real production problems. The POC included:
- JSON Schema validation
- Semantic versioning
- Query optimization

**What happened:** The POC was handed to a contractor who implemented it WITHOUT these critical features, despite them being in the specs.

**This project:** Is the CORRECT implementation with ALL the best practices I originally proposed.

**Goal:** Create a production-ready library that showcases proper software engineering:
- Type-safe configuration
- Comprehensive testing
- Clean architecture
- Proper documentation

---

## 📊 Roadmap

| Phase | Timeline | Deliverables |
|-------|----------|--------------|
| **Phase 1** ✅ | Week 1-2 | Project setup, domain model, unit tests |
| **Phase 2** 🚧 | Week 3-4 | JSON Schema validation, SQL generation |
| **Phase 3** | Week 5-6 | Query optimization, PostgreSQL tests |
| **Phase 4** | Week 7-8 | Oracle support, CLI, REST API |
| **Phase 5** | Week 9-10 | Documentation, v1.0.0 release |

---

## 🤝 Contributing

This is a personal project, but feedback and suggestions are welcome!

---

## 📄 License

Apache License 2.0

---

## 👨‍💻 Author

**Sam Bouchenafa**  
Java Developer @ CNAV  
GitHub: [@sambouch79](https://github.com/sambouch79)

> "The difference between a POC and production code is the difference between proving it works and making it actually work." - Sam

---

**Made with ❤️ and the determination to do it right** 🔥
