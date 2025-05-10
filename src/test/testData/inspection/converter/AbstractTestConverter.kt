import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter

abstract class AbstractTestConverter : AbstractEntityUpgradeConverter<TestEntity, TestEntity>() {

    override val module = "test"

}