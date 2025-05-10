import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter;

public abstract class AbstractTestConverterDifferentModule extends AbstractEntityUpgradeConverter<TestEntity, TestEntity> {

    public String getModule() {
        return "test2";
    }

}