/*
 Debouncing is a very useful tool when dealing with UI inputs.
 Consider a search bar, whose content is used to query an API.
 It wouldn't make sense to perform a request for every character
 the user is typing, because as soon as a new character is entered,
 the result of the previous request has become irrelevant.

 Instead, our code will perform much better if we "debounce" the API
 call, meaning that we will wait until some delay has passed, without
 the input being modified, before actually performing the call.
 */

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
