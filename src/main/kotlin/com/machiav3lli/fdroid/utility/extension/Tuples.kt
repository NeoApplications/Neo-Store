package com.machiav3lli.fdroid.utility.extension

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

operator fun <A, B, C, D> Quadruple<A, B, C, D>.component1() = first
operator fun <A, B, C, D> Quadruple<A, B, C, D>.component2() = second
operator fun <A, B, C, D> Quadruple<A, B, C, D>.component3() = third
operator fun <A, B, C, D> Quadruple<A, B, C, D>.component4() = fourth

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)

operator fun <A, B, C, D, E> Quintuple<A, B, C, D, E>.component1() = first
operator fun <A, B, C, D, E> Quintuple<A, B, C, D, E>.component2() = second
operator fun <A, B, C, D, E> Quintuple<A, B, C, D, E>.component3() = third
operator fun <A, B, C, D, E> Quintuple<A, B, C, D, E>.component4() = fourth
operator fun <A, B, C, D, E> Quintuple<A, B, C, D, E>.component5() = fifth