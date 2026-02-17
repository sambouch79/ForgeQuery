# 🚀 QUICKSTART - QueryForge

## ⚡ Installation (5 minutes)

### 1. Prérequis

```bash
# Vérifie Java 21
java -version
# Doit afficher: openjdk version "21.x.x"

# Vérifie Maven
mvn -version
# Doit afficher: Apache Maven 3.9.x
```

### 2. Télécharger le projet

Télécharge `queryforge-starter-kit.zip` et dézippe-le :

```bash
unzip queryforge-starter-kit.zip
cd queryforge
```

### 3. Build

```bash
mvn clean install
```

**Résultat attendu:**
```
[INFO] Building queryforge-parent 1.0.0-SNAPSHOT
[INFO] Building queryforge-core 1.0.0-SNAPSHOT
[INFO] Tests run: 11, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS ✅
```

### 4. Lancer les tests

```bash
mvn test
```

---

## 📁 Structure du projet

```
queryforge/
├── pom.xml                                    # POM parent
├── .gitignore
├── README.md
├── QUICKSTART.md
│
└── queryforge-core/                           # Library principale
    ├── pom.xml
    │
    ├── src/main/java/io/github/sambouch79/queryforge/model/
    │   ├── Version.java                       # Versioning sémantique
    │   ├── Mapping.java                       # Modèle principal
    │   ├── Field.java                         # Interface Field
    │   ├── SimpleField.java                   # Champ simple
    │   ├── FunctionField.java                 # Fonctions SQL
    │   ├── RawField.java                      # SQL brut
    │   └── SchemaModel.java                   # Schema, Table, Join
    │
    └── src/test/java/io/github/sambouch79/queryforge/model/
        ├── VersionTest.java                   # 6 tests
        └── FieldTest.java                     # 5 tests
```

---

## ✅ Vérifier que tout fonctionne

### Test 1 : Compilation

```bash
mvn compile
```

### Test 2 : Tests unitaires

```bash
mvn test
```

### Test 3 : Créer un mapping programmatiquement

Crée `queryforge-core/src/test/java/io/github/sambouch79/queryforge/QuickTest.java` :

```java
package io.github.sambouch79.queryforge;

import io.github.sambouch79.queryforge.model.*;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class QuickTest {
    
    @Test
    void shouldBuildCompleteMapping() {
        Mapping mapping = Mapping.builder()
            .model("TEST")
            .version(Version.parse("1.0.0"))
            .schema(Schema.of(Table.of("INDIVIDU", "i")))
            .fields(Map.of(
                "NOM", SimpleField.of("i.NOM"),
                "PRENOM", SimpleField.of("i.PRENOM")
            ))
            .build();
        
        mapping.validate();
        System.out.println("✅ Mapping OK: " + mapping.getModel());
    }
}
```

Lance-le :
```bash
mvn test -Dtest=QuickTest
```

---

## 🎯 Prochaines étapes

### Aujourd'hui
1. ✅ Vérifier que tout compile
2. 📝 Créer ton premier JSON de mapping
3. 🔧 Implémenter MappingLoader

### Cette semaine
4. ✅ JSON Schema validation
5. 🏗️ SQLGenerator basique
6. 🧪 Tests avec PostgreSQL (Testcontainers)

---

## 🐛 Troubleshooting

### Erreur: "Cannot find symbol Version"

**Solution:** Vérifie le package :
```bash
find queryforge-core/src -n "*.java" | grep Version
# Doit afficher: .../io/github/sambouch79/queryforge/model/Version.java
```

### Erreur: "Lombok not found"

**Solution IntelliJ:**
1. File → Settings → Plugins → Install "Lombok"
2. File → Settings → Compiler → Annotation Processors → Enable

### Tests qui échouent

```bash
# Voir les détails
mvn test -X

# Nettoyer et rebuild
mvn clean install
```

---

## 💪 Prêt pour la suite ?

Dis-moi quand:
- ✅ `mvn test` passe
- ✅ QuickTest fonctionne

Et on attaque **MappingLoader** et **SQLGenerator** ! 🚀
