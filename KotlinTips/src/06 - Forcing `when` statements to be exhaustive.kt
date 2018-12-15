
/*
 In Kotlin, `when` can be a tricky construct, because it operates in
 two different ways, depending on whether it is being used as a
  statement or an expression.

 Most notably, if `when` is used as an **expression** (i.e. code that
 returns something), the compiler will check that all possible cases
 are indeed being handled by the `when` expression.
 On the other hand, if `when` is used as a **statement** (i.e. to
 perform side effects), the compiler will not check for such exhaustiveness.

 Yet, there are some instances in which we use `when` as a statement,
 and still would like it to implement this check for exhaustiveness!
 Fortunately, through a clever extension on `Unit`, it's possible to
 turn `when` from a statement to an expression, and indeed get our
 desired behavior.
 */

sealed class Numbers {
    class One: Numbers()
    class Two: Numbers()
    class Three: Numbers()
}

val Unit.exhaustive get() = Unit

fun main(args: Array<String>) {
    val number: Numbers = Numbers.Two()

    // `when` is used as a statement, so the
    // compiler does not check for exhaustiveness
    when (number) {
        is Numbers.One -> println("This is One")
        is Numbers.Two -> println("This is Two")
    }

    // by calling `exhaustive`, we turn `when` from a statement
    // to an expression 👍
    when (number) {
        is Numbers.One -> println("This is One")
        is Numbers.Two -> println("This is Two")
        // 'when' expression must be exhaustive, add necessary 'is Three' branch or 'else' branch instead
    }.exhaustive
}
