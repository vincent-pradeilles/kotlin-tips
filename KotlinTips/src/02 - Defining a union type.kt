/*
 The C language has a construct called `union`, that allows a single
 variable to hold values from different types. While Kotlin does not
 provide such a construct, it provides sealed classes, which allows
 us to define a type called `Either` that implements an `union`
 of two types.
 */

import java.util.*

sealed class Either<out E, out V> {
    data class Left<out E>(val left: E) : Either<E, Nothing>()
    data class Right<out V>(val right: V) : Either<Nothing, V>()
}

fun <A, B> Either<A, B>.either(ifLeft: ((A) -> Unit)? = null, ifRight: ((B) -> Unit)? = null) {
    when(this) {
        is Either.Left -> ifLeft?.invoke(this.left)
        is Either.Right -> ifRight?.invoke(this.right)
    }
}

fun main(args : Array<String>) {
    val intOrString: Either<Int, String> = if (Random().nextBoolean()) Either.Left(2) else Either.Right("Foo")

    intOrString.either(
        ifLeft = { print(it + 1) },
        ifRight = { print(it + "Bar") }
    )
}
