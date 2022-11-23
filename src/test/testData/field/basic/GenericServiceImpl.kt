abstract class GenericServiceImpl<T : Any> : GenericService<T> {

    override fun findById(): T {
        error("")
    }

}