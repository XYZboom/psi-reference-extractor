package expression.call.call9

class UserCall9 {
    fun test() {
        val providerCall9 = ProviderCall9(ReceiverCall9())
        providerCall9./*<source:func>*/func/*<source:func/>*/ {
            /*<source:receiverFunc>*/receiverFunc/*<source:receiverFunc/>*/()
        }
    }
}