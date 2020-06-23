package com.e16din.sc.processor

import java.util.*

class ScreenContainer(var name: String?, controller: ControllerContainer) {
    var controllers = ArrayList<ControllerContainer>()

    init {
        controllers.add(controller)
    }
}
