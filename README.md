# A Reference Extractor Based on PSI

PSI-Reference-Extractor is a reference extractor based on PSI (Jetbrains Program Structure Interface). Currently it supports dependency analysis on Kotlin and Java.

## Usage

### Command Line

The tool provides dependency graph extraction function and supports file formats such as JSON, DOT, and CSV.

Download **PSI-Reference-Extractor** in [release page](https://github.com/XYZboom/psi-reference-extractor/releases).

Run `java -jar cli-runner-1.0.0-SNAPSHOT-all.jar -h`  to see help messages.

```
Usage: RefExtract [-eu] [-o=<output>] [-f=<exporters>[,<exporters>...]]...
                  [-g=<granularity>[,<granularity>...]]... <input>
      <input>             input directory
      -eu, --export-unknown
                          export unknown references
  -f, --format=<exporters>[,<exporters>...]

  -g, --granularity=<granularity>[,<granularity>...]
                          granularity, choose in [file, class, member,
                            expression]
  -o, --output=<output>   output file prefix
```

### API Usage

Due to the experimental version of the Kotlin compiler interface used, this feature is temporarily unstable.

In Kotlin, use `PsiReference?.referenceInfos` to get `ReferenceInfo` from your reference.

```kotlin
val PsiReference?.referenceInfos: List<ReferenceInfo>
```

## Dependency type support

We classify dependency types by reference source, [reference types](docs/ReferenceTargetTypes.md), and [reference target](docs/ReferenceTypes.md).
