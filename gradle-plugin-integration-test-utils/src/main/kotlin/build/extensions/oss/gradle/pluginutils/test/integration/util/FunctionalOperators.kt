package build.extensions.oss.gradle.pluginutils.test.integration.util

import java.util.function.BiConsumer
import java.util.function.UnaryOperator


internal operator fun <T : Any> UnaryOperator<T>?.plus(other: UnaryOperator<T>?): UnaryOperator<T>? =
    when {
        this == null -> other
        other == null -> this
        else -> UnaryOperator { this.apply(it).let(other::apply) }
    }


internal operator fun <T, U> BiConsumer<T, U>?.plus(other: BiConsumer<T, U>?): BiConsumer<T, U>? =
    when {
        this == null -> other
        other == null -> this
        else -> BiConsumer { t, u -> this.accept(t, u); other.accept(t, u) }
    }
