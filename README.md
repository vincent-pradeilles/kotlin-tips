# Kotlin Tips

The following is a collection of tips I find to be useful when working with the Kotlin language.

# Summary

* [#02 Defining a union type](#defining-a-union-type)
* [#01 Solving callback hell with function composition](#solving-callback-hell-with-function-composition)

# Tips

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
