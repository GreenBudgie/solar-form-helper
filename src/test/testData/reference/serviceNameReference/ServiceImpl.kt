import org.springframework.stereotype.Service

@Service("test.service")
class ServiceImpl {

    override fun findData(viewParams: ViewParams?): List<TestData?>? {
        return listOf()
    }

}