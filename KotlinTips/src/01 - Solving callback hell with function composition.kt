/*
 Asynchronous functions are a big part of Networking APIs, and most
 developers are familiar with the challenge they pose when one
 needs to sequentially call several asynchronous APIs.

 This often results in callbacks being nested into one another,
 a predicament often referred to as callback hell.

 Many third-party frameworks or native language constructs are able
 to tackle this issue, for instance [RxKotlin](https://github.com/ReactiveX/RxKotlin)
 or [Promises](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-promise/index.html).
 Yet, for simple instances of the problem, there is no need to
 use such big guns, as it can actually be solved with simple function composition.
 */

sealed class Result<out T> {
    data class Value<out T>(val value: T) : Result<T>()
    data class Error(val error: kotlin.Error) : Result<Nothing>()
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
