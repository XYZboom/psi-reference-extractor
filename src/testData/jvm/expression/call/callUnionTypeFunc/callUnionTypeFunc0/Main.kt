interface IA {
    /*<target:funcA>*/fun funcA() {}/*<target:funcA/>*/
}
interface IB {
    /*<target:funcB>*/fun funcB() {}/*<target:funcB/>*/
}
class C: IA, IB
class D: IA, IB

fun func(b: Boolean) {
    val x = if (b) C() else D()
    x./*<source:funcA>*/funcA/*<source:funcA/>*/()
    x./*<source:funcB>*/funcB/*<source:funcB/>*/()
}