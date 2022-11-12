import com.solanteq.solar.commons.annotations.CallableService;
import com.solanteq.solar.commons.annotations.Callable;

@CallableService
public interface TestServiceJava {

    @Callable
    public TestData findData();

}