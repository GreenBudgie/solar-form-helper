import org.springframework.stereotype.Service

@Service("test.service")
class ServiceImpl : SuperServiceImpl {

    override fun findData() {}

}