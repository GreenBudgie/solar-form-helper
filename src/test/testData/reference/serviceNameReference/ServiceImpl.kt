@Service("test.service")
class ServiceImpl : TestService {

    override fun findData(viewParams: ViewParams?): List<TestData?>? {
        return listOf()
    }

}