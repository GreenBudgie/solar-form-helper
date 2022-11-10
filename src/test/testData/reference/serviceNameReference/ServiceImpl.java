import org.springframework.stereotype.Service

@Service("test.service")
public class ServiceImpl {

    public List<TestData> findData(ViewParams viewParams) {
        return new ArrayList();
    }

}