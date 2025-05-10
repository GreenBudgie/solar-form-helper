import com.solanteq.solar.commons.hibernate.entity.AuditAwareEntity;
import javax.persistence.*;

@Table(name = TestEntity.TABLE_NAME)
public class TestEntity extends AuditAwareEntity {

    public static final String TABLE_NAME = "test_table";

}