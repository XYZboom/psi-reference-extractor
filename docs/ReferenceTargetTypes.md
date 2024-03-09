# Reference Target Types

List of reference types from the perspective of the target element.

- [Class](##Class)
- [Property](##Property)
- [Method](##Method)

## Class

Reference whose target is a class or interface.

### Code Samples

#### Java import Java class

```java
// Target.java
package target;
public class Target {}
//           ^^^^^^
// reference below targets here and has the target type "class"
```

```java
// Source.java
package source;
import target.Target;
//            ^^^^^^
// reference here has the source type "import"
```

## Property

Reference whose target is a Kotlin property

### Code Samples

#### Kotlin access Kotlin property

```kotlin
// Target.kt
package target
class Target {
    val string = "123"
//  ^^^^^^^^^^^^^^^^^^
//  reference below targets here and has the target type "property"
}
```

```kotlin
// Source.kt
package source
import target.Target
fun func() {
    val target = Target()
    target.string
//         ^^^^^^
//  reference here has the source type "property"
}
```

## Method

Reference whose target is a method.

### Code Samples

#### Java call Kotlin method

```kotlin
// Target.kt
package target
  fun func() {}
//^^^^^^^^^^^^^
//reference below targets here and has target type "method"
```

```java
// Source.java
package source;
import target.TargetKt;
//            ^^^^^^^^
// top level class generated by kotlin on jvm. 
// See kotlin programming language at https://kotlinlang.org/docs/home.html
public class Source {
    public void func() {
        TargetKt.func();
//      ^^^^^^^^^^^^^^^
//      reference here has the source type "call"
//      ^^^^^^^^^^^^^
//      In PSI, PsiRefence is always bind to element here.
    }
}
```
