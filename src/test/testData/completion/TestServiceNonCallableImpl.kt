import org.springframework.stereotype.Service

@Service("test.testServiceNonCallable")
class TestServiceNonCallableImpl : TestServiceNonCallable {

    override fun callableMethod1(viewParams: ViewParams) {}

    override fun callableMethod2() {}

    override fun nonCallableMethod(viewParams: ViewParams) {}

}