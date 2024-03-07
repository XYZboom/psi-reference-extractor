# Reference Source Types

- [Import](##Import)
- [Call](##Call)
- [Property](##Property)

## Import

Reference that located at import list.

### Code Samples

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

Reference that located at call expression.

### Code Samples

#### Java call Java or Kotlin method

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
//  reference above targets here and has the target type "call"
//  same situation in kotlin method
}
```

#### Java call Kotlin property (source call)

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

## Property

Reference located at property access expression.

### Code Samples

#### Kotlin access Kotlin property

```kotlin
// Source.kt
package source
import target.Target;
fun func() {
    val target = Target()
    target.string
//         ^^^^^^
//  reference here has the source type "property"
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

#### Java call Kotlin property (target property)

see at [Java call Kotlin property](####Java call Kotlin property (source call))
