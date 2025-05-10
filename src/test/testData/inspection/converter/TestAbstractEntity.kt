import com.solanteq.solar.commons.hibernate.entity.AuditAwareEntity
import javax.persistence.*

@Table(name = TestAbstractEntity.TABLE_NAME)
abstract class TestAbstractEntity : AuditAwareEntity {

    companion object {

        const val TABLE_NAME = "test_table"

    }

}