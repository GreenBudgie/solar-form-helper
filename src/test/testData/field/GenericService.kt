import com.solanteq.solar.commons.annotations.Callable
import com.solanteq.solar.commons.annotations.CallableService

@CallableService
interface GenericService<T : Any> {

    @Callable
    fun findById(): T

    @Callable
    fun findDataList(): List<T>

}