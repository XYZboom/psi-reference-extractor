package target

//class Target {
//    /*<target:int>*/fun component1(): Any {
//        return 123
//    }/*<target:int/>*/
//
//    /*<target:str>*/fun component2(): String {
//        return "comp2"
//    }/*<target:str/>*/
//}
data class Target(/*<target:int>*/val x: Int/*<target:int/>*/, /*<target:str>*/val y: String/*<target:str/>*/)