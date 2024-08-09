---
title: BackGestureHandler
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.ultils](index.html)/[BackGestureHandler](-back-gesture-handler.html)



# BackGestureHandler



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



fun [BackGestureHandler](-back-gesture-handler.html)(enabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, backHandler: BackHandler = LocalComponentContext.current.backHandler, onBackStarted: (BackEvent) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBackProgressed: (BackEvent) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBackCancelled: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = {}, onBack: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Handles gestures using the provided back handler, which by default is the chileren's one.




