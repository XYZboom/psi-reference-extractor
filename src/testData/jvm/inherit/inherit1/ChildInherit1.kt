package inherit.inherit1

class ChildInherit1(
    private val str: String,
) : /*<source:ParentInherit1>*/ParentInherit1/*<source:ParentInherit1/>*/(),
    /*<source:InterfaceInherit1>*/InterfaceInherit1/*<source:InterfaceInherit1/>*/ {
    override fun funcInterfaceInherit1(): String {
        return str
    }
}