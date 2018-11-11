/*
 It might happen that your code has to deal with values that come
 with an expiration date. In a game, it could be a score multiplier
 that will only last for 30 seconds. Or it could be an authentication
 token for an API, with a 15 minutes lifespan. In both instances you
 can rely on the type `Expirable` to encapsulate the expiration logic.
 */

import java.util.*

class Expirable<Value>(var innerValue: Value, var expirationDate: Date) {

    val value: Value?
        get() = if (hasExpired()) null else innerValue

    constructor(value: Value, duration: Long) : this(value, Date(Date().time + duration ))

    fun hasExpired(): Boolean {
        return expirationDate < Date()
    }
}

fun main(args: Array<String>) {
    val expirable = Expirable(42,3000)

    Thread.sleep(2000)
    println(expirable.value) // 42
    Thread.sleep(2000)
    println(expirable.value) // null
}
