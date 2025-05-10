package com.solanteq.solar.plugin.base

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
enum class SolarDependency(val mavenCoordinates: String) {

    JOBS_UPGRADE_CONVERTER("com.solanteq.solar.job:solar-jobs-entity-upgrade:$JOB_VERSION"),
    HIBERNATE_COMMONS("com.solanteq.solar:solar-hibernate-commons:$PLATFORM_VERSION"),
    ;


}

const val PLATFORM_VERSION = "3.4.9.1.RELEASE"
const val JOB_VERSION = "4.0.4.RELEASE"