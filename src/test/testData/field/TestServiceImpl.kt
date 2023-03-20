import org.springframework.stereotype.Service

@Service("test.testService")
class TestServiceImpl : TestService {

    override fun findData(): DataClass {}

    override fun findDataWithList(): DataClassWithList {}

    override fun findDataClassWithSuperClass(): Cls {}

    override fun findDataWithNestedProperty(): DataClassWithNestedProperty

    override fun findDataList(): List<DataClass>

    override fun findDataClsList(): List<Cls>

}