/*
 By capturing a local variable in a returned lambda, it is possible
 to manufacture cache-efficient versions of [pure functions](https://en.wikipedia.org/wiki/Pure_function).
 Be careful though, this trick only works with non-recursive function!
 */

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
