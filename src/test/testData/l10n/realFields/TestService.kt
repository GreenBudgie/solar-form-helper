import com.solanteq.solar.commons.annotations.CallableService
import com.solanteq.solar.commons.annotations.Callable

@CallableService
interface TestService {

    @Callable
    fun findById(): DataClass

}