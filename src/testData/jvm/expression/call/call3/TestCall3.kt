package expression.call.call3

class TestCall3 {
    fun test0() {
        val providerCall3 = ProviderCall3()
        val str = "${providerCall3./*<source:func0>*/func0/*<source:func0/>*/()}," +
                " ${providerCall3./*<source:func1>*/func1/*<source:func1/>*/(1)}"
        println(str)
    }
}