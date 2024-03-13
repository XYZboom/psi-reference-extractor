package source
import target.Target

fun func() {
    val t0 = Target()
    val t1 = Target()
    t0 /*<source>*/+/*<source/>*/ t1

}