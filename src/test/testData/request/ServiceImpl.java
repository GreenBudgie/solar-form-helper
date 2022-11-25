import org.springframework.stereotype.Service

@Service("test.service")
public class ServiceImpl {

    public TestData findData() {
        return new TestData();
    }

}