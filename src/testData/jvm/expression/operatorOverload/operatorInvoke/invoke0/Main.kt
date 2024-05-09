class Target: () -> Unit {
    /*<target>*/override fun invoke() {
    }/*<target/>*/
}

fun main() {
    val target = Target()
    /*<source>*/target()/*<source/>*/
}