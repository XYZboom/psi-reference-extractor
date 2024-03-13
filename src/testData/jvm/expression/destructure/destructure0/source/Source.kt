package source

import target.Target

fun func() {
    val (/*<source:int>*/int/*<source:int/>*/, /*<source:str>*/str/*<source:str/>*/) = Target(1, "123")
}