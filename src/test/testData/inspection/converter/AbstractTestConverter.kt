import com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter

abstract class AbstractTestConverter : AbstractUpgradeConverter<TestInheritedEntity, TestInheritedEntity> {

    override val module
        get() = "test"

}