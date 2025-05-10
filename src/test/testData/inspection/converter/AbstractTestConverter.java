import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter;

public abstract class AbstractTestConverter extends AbstractEntityUpgradeConverter<TestEntity, TestEntity> {

    public String getModule() {
        return "test";
    }

}