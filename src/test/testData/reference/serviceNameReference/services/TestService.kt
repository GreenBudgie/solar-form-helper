@CallableInterface
interface TestService {

    @Callable
    fun findData(viewParams: ViewParams?): List<TestData?>?

}