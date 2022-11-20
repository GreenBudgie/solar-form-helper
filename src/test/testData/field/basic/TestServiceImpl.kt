import org.springframework.stereotype.Service

@Service("test.testService")
class TestServiceImpl : TestService {

    override fun findData(): DataClass {}

    override fun findDataWithNestedProperty(): DataClassWithNestedProperty

}