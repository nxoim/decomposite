---
title: navigate
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation](../index.html)/[NavController](index.html)/[navigate](navigate.html)



# navigate



[common]\
fun [navigate](navigate.html)(destination: [C](index.html), removeIfIsPreceding: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })



Navigates to a destination. If a destination exists already - moves it to the top instead of adding a new entry. If the [removeIfIsPreceding](navigate.html) is enabled (is by default) and the requested [destination](navigate.html) precedes the current one in the stack - navigate back instead.




