import com.solanteq.solar.commons.hibernate.entity.AuditAwareEntity
import javax.persistence.*

@Table(name = TestEntity.TABLE_NAME)
class TestEntity : AuditAwareEntity {

    companion object {

        const val TABLE_NAME = "test_table"

    }

}