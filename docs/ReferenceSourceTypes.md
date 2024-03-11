# Reference Source Types

List of reference types from the perspective of the source element.

- [Import](#Import)
- [Call](#Call)
- [Access](#Access)
- [Access Reference](#Access-Reference)
- [Create](#Create)
- [Extend](#Extend)
- [Implement](#Implement)

## Import

Reference whose source located at import list.

### Code Samples

#### Java import Java class

```java
// Source.java
package source;
import target.Target;
//            ^^^^^^
// reference here has the source type "import"
```

```java
// Target.java
package target;
public class Target {}
//           ^^^^^^
// reference above targets here and has the target type "class"
```

## Call

Reference whose source located at call expression.

### Code Samples

#### Java call Java method

```java
// Source.java
package source;
import target.Target;
public class Source {
    public void func() {
        Target target = new Target();
        target.func();
//      ^^^^^^^^^^^^^
//      reference here has the source type "call"
//      ^^^^^^^^^^^
//      In PSI, PsiRefence is always bind to element here.
    }
}
```

```java
// Target.java
package target;
public class Target {
    public void func() {}
//  ^^^^^^^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "method"
//  same situation in kotlin method
}
```

#### Java call Kotlin property

```java
// Source.java
package source;
import target.Target;
public class Source {
    public void func() {
        Target target = new Target();
        target.getString();
//      ^^^^^^^^^^^^^^^^^^
//      reference here has the source type "call"
//      ^^^^^^^^^^^^^^^^
//      In PSI, PsiRefence is always bind to element here.
    }
}
```

```kotlin
// Target.kt
package target
class Target {
    val string = "123"
//  ^^^^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "property"
//  see property.
}
```

## Access

Reference whose source located at property (or field in java) access expression.

Some Java methods are considered as Kotlin properties. At this point, the resolution is "Kotlin access Java method", rather than "Kotlin call Java method".

### Code Samples

#### Kotlin access Kotlin property

```kotlin
// Source.kt
package source
import target.Target
fun func() {
    val target = Target()
    target.string
//         ^^^^^^
//  reference here has the source type "access"
}
```

```kotlin
// Target.kt
package target
class Target {
    val string = "123"
//  ^^^^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "property"
}
```

## Access Reference

Reference whose source located at property or method **reference** access expression.

### Code Samples

#### Kotlin access reference (of) Kotlin method

```kotlin
// Source.kt
package source
import target.targetFunc
fun func(input: List<String>) {
    input.forEach(::targetFunc)
//                ^^^^^^^^^^^^
//  reference here has the source type "access reference"
//                  ^^^^^^^^^^
//  In PSI, PsiRefence is always bind to element here.
}
```

```kotlin
// Target.kt
package target
   fun targetFunc(str: String) {}
// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
// reference above targets here and has the target type "method"
```



## Create

Reference whose source located at an expression that create an object.

### Code Samples

#### Kotlin create Kotlin class

```kotlin
// Source.kt
package source
import target.Target
fun func() {
    val target = Target()
//               ^^^^^^^^
//  reference here has the source type "create"
//               ^^^^^^
//  In PSI, PsiReference is always bind to element here.
}
```

```kotlin
// Target.kt
package target
  class Target {}
//^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "class"
```

## Extend

Reference whose source is class extend.

### Code Samples

#### Kotlin extend Kotlin class

```kotlin
// Source.kt
package source
import target.Target
class Source: Target() {
//            ^^^^^^
//  reference here has the source type "extend"
}
```

```kotlin
// Target.kt
package target
  open class Target
//^^^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "class"
```

## Implement

Reference whose source is implement of an interface.

### Code Samples

##### Kotlin implement Kotlin interface

```kotlin
// Source.kt
package source
import target.Target
class Source: ITarget {
//            ^^^^^^^
//  reference here has the source type "implement"
}
```

```kotlin
// Target.kt
package target
  interface ITarget
//^^^^^^^^^^^^^^^^^
//  reference above targets here and has the target type "interface"
```