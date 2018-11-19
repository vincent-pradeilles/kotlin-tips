# Kotlin Tips

The following is a collection of tips I find to be useful when working with the Kotlin language.

# Summary

* [#05 Debouncing a function call](#debouncing-a-function-call)
* [#04 Dealing with expirable values](#dealing-with-expirable-values)
* [#03 Manufacturing cache-efficient versions of pure functions](#manufacturing-cache-efficient-versions-of-pure-functions)
* [#02 Defining a union type](#defining-a-union-type)
* [#01 Solving callback hell with function composition](#solving-callback-hell-with-function-composition)

# Tips

## Debouncing a function call

Debouncing is a very useful tool when dealing with UI inputs. Consider a search bar, whose content is used to query an API. It wouldn't make sense to perform a request for every character the user is typing, because as soon as a new character is entered, the result of the previous request has become irrelevant.

Instead, our code will perform much better if we "debounce" the API call, meaning that we will wait until some delay has passed, without the input being modified, before actually performing the call.

```kotlin
import android.os.Handler
import android.os.Looper

fun debounced(delay: Long, looper: Looper = Looper.getMainLooper(), action: () -> Unit): () -> Unit {
    val handler = Handler(looper)
    val runnable = Runnable { action() }

    return {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, delay)
    }
}

fun main(args: Array<String>) {
    val debouncedPrint = debounced(1000) { println("Action performed!") }

    debouncedPrint()
    debouncedPrint()
    debouncedPrint()

    // After a 1 second delay, this gets
    // printed only once to the console:

    // Action performed!
}
```

## Dealing with expirable values

It might happen that your code has to deal with values that come with an expiration date. In a game, it could be a score multiplier that will only last for 30 seconds. Or it could be an authentication token for an API, with a 15 minutes lifespan. In both instances you can rely on the type `Expirable` to encapsulate the expiration logic.

```kotlin
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
```

## Manufacturing cache-efficient versions of pure functions

By capturing a local variable in a returned lambda, it is possible to manufacture cache-efficient versions of [pure functions](https://en.wikipedia.org/wiki/Pure_function). Be careful though, this trick only works with non-recursive function!

```kotlin
import kotlin.system.measureNanoTime

fun <In, Out> cached(f: (In) -> Out): (In) -> Out {
    val cache = mutableMapOf<In, Out>()

    return { input: In ->
        cache.computeIfAbsent(input, f)
    }
}

fun main(args : Array<String>) {
    val cachedCos = cached { x: Double -> Math.cos(x) }

    println(measureNanoTime { cachedCos(Math.PI * 2) }) // 329378 ns

    /* value of cos for 2π is now cached */

    println(measureNanoTime { cachedCos(Math.PI * 2) }) // 6286 ns
}
```

## Defining a union type

The C language has a construct called `union`, that allows a single variable to hold values from different types. While Kotlin does not provide such a construct, it provides sealed classes, which allows us to define a type called `Either` that implements an `union` of two types.

```kotlin
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
```

## Solving callback hell with function composition

Asynchronous functions are a big part of Networking APIs, and most developers are familiar with the challenge they pose when one needs to sequentially call several asynchronous APIs.

This often results in callbacks being nested into one another, a predicament often referred to as callback hell.

Many third-party frameworks or native language constructs are able to tackle this issue, for instance [RxKotlin](https://github.com/ReactiveX/RxKotlin) or [Promises](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-promise/index.html). Yet, for simple instances of the problem, there is no need to use such big guns, as it can actually be solved with simple function composition.

```kotlin
sealed class Result<out T> {
    data class Value<out T>(val value: T) : Result<T>()
    data class Error(val error: Error) : Result<Nothing>()
}

typealias CompletionHandler<T> = (Result<T>) -> Unit
typealias NoArgService<Output> = (CompletionHandler<Output>) -> Unit
typealias Service<Input, Output> = (Input, CompletionHandler<Output>) -> Unit

infix fun <T, U> NoArgService<T>.then(second: Service<T, U>): (CompletionHandler<U>) -> Unit {
    return { completion: CompletionHandler<U> ->
        this { firstResult ->
            when (firstResult) {
                is Result.Error -> completion(firstResult)
                is Result.Value -> second(firstResult.value) { secondResult ->
                    completion(secondResult)
                }
            }
        }
    }
}

infix fun <T, U> NoArgService<T>.then(transform: (T) -> U): NoArgService<U> {
    return { completion: CompletionHandler<U> ->
        this { firstResult ->
            when (firstResult) {
                is Result.Error -> completion(firstResult)
                is Result.Value -> completion(Result.Value(transform(firstResult.value)))
            }
        }
    }
}

fun fetchInt(completionHandler: CompletionHandler<Int>) {
    completionHandler(Result.Value(42))
}

fun fetchString(arg: String, completionHandler: CompletionHandler<String>) {
    completionHandler(Result.Value("🎉 $arg"))
}

fun main(args: Array<String>) {
    (::fetchInt then { int -> "${int / 2}" } then ::fetchString) {
        print(it)
    }
}
```
