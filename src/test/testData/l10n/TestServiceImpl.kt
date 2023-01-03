import org.springframework.stereotype.Service

@Service("test.testService")
class TestServiceImpl : TestService {

    override fun findById(): DataClass {}

}