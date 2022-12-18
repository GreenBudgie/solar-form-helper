import org.springframework.stereotype.Service

@Service("test.customService")
class CustomServiceImpl : GenericServiceImpl<DataClass>(), CustomService