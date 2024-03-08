package expression.call.call1

class TestCall1 {
    fun test0() {
        val providerCall1 = ProviderCall1()
        providerCall1./*<source:func0>*/func0/*<source:func0/>*/()
            ./*<source:funcInMiddleType>*/funcInMiddleType/*<source:funcInMiddleType/>*/()
    }
}