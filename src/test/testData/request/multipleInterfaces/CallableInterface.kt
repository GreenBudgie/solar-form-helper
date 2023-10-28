import com.solanteq.solar.commons.annotations.CallableService
import com.solanteq.solar.commons.annotations.Callable

@CallableService
interface CallableInterface {

    @Callable
    fun find()

}