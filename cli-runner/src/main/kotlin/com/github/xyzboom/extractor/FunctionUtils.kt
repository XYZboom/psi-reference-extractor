package com.github.xyzboom.extractor

fun <T1, T2, R> wrapper(function1: (T1) -> R): (T1, T2) -> R {
    return { t1, _ ->
        function1(t1)
    }
}