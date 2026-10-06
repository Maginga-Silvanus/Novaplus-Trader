package com.example.novaplustrader.ui.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AnimationSpecs {
    val defaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val fastOutSlowIn = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val quickFade = tween<Float>(
        durationMillis = 150
    )
}
