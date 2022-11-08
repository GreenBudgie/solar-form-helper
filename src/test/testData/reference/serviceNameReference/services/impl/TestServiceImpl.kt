@Service("test.testService")
class TestServiceImpl : TestService {

    override fun findData(viewParams: ViewParams?): List<TestData?>? {
        return listOf()
    }

}