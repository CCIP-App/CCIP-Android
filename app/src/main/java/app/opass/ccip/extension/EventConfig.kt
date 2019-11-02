package app.opass.ccip.extension

import app.opass.ccip.model.EventConfig
import app.opass.ccip.model.FeatureType

fun EventConfig.getFastPassUrl(): String? {
    return features.find { it.feature == FeatureType.FAST_PASS }?.url
}
