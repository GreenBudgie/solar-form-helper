import org.springframework.stereotype.Service

@Service("test.testService")
class TestServiceImpl : TestService {

    override fun callableMethod1(viewParams: ViewParams) {}

    override fun callableMethod2() {}

    override fun nonCallableMethod(viewParams: ViewParams) {}

}