import com.solanteq.solar.commons.annotations.CallableService
import com.solanteq.solar.commons.annotations.Callable

@CallableService
interface TestService {

    @Callable
    fun findData(): DataClass

    @Callable
    fun findDataWithList(): DataClassWithList

    @Callable
    fun findDataClassWithSuperClass(): Cls

    @Callable
    fun findDataWithNestedProperty(): DataClassWithNestedProperty

    @Callable
    fun findDataList(): List<DataClass>

    @Callable
    fun findDataClsList(): List<Cls>

}