import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter

abstract class AbstractTestConverterDifferentModule : AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

    override val module = "test2"

}