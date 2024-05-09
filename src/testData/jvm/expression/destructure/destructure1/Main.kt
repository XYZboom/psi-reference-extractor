class Target {
    /*<target:int>*/fun component1(): Any {
        return 123
    }/*<target:int/>*/

    /*<target:str>*/fun component2(): String {
        return "comp2"
    }/*<target:str/>*/
}
fun func() {
    val (/*<source:int>*/int/*<source:int/>*/, /*<source:str>*/str/*<source:str/>*/) = Target(1, "123")
}