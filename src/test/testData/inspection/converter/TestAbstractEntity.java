import com.solanteq.solar.commons.hibernate.entity.AuditAwareEntity;
import javax.persistence.*;

@Table(name = TestAbstractEntity.TABLE_NAME)
public abstract class TestAbstractEntity extends AuditAwareEntity {

    public static final String TABLE_NAME = "test_table";

}