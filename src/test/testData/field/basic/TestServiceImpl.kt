import org.springframework.stereotype.Service

@Service("test.testService")
class TestServiceImpl : TestService {

    override fun findData(): DataClass {}

    override fun findDataClassWithSuperClass(): Cls {}

    override fun findDataWithNestedProperty(): DataClassWithNestedProperty

}