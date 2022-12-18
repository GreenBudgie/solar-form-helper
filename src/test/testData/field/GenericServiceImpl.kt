abstract class GenericServiceImpl<T : Any> : GenericService<T> {

    override fun findById(): T {
        error("")
    }

    override fun findDataList(): List<T> {}

}