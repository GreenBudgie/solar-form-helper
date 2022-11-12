import org.springframework.stereotype.Service;

@Service("test.testServiceJava")
public class TestServiceJavaImpl implements TestServiceJava {

    public TestData findData() {
        return new TestData();
    }

}