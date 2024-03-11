package target

import kotlin.reflect.KProperty

class Target {
    /*<target>*/operator fun getValue(any: Any?, property: KProperty<*>): Any {
        return "1"
    }/*<target/>*/

    operator fun setValue(any: Any?, property: KProperty<*>, value: Any?) {

    }
}