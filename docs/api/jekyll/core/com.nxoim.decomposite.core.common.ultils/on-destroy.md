---
title: onDestroy
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.ultils](index.html)/[onDestroy](on-destroy.html)



# onDestroy



[common]\
fun ComponentContext.[onDestroy](on-destroy.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), block: @[DisallowComposableCalls](https://developer.android.com/reference/kotlin/androidx/compose/runtime/DisallowComposableCalls.html)() -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))



Saves the call in a container that executes the call before getting fully destroyed, surviving configuration changes, making sure the block is only executed when a component fully dies,




