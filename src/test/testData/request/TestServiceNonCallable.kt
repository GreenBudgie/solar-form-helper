import com.solanteq.solar.commons.annotations.CallableService
import com.solanteq.solar.commons.annotations.Callable

interface TestServiceNonCallable {

    @Callable
    fun callableMethod1(viewParams: ViewParams)

    @Callable
    fun callableMethod2()

    fun nonCallableMethod(viewParams: ViewParams)

}