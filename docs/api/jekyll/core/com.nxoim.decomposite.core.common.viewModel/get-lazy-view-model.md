---
title: getLazyViewModel
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.viewModel](index.html)/[getLazyViewModel](get-lazy-view-model.html)



# getLazyViewModel



[common]\




@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)



@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



inline fun &lt;[T](get-lazy-view-model.html) : [ViewModel](-view-model/index.html)&gt; [getLazyViewModel](get-lazy-view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;): [T](get-lazy-view-model.html)



Creates a view model instance using the prepared (by [prepareLazyViewModel](prepare-lazy-view-model.html)) reference if an instance does not exist. The first instance manages the view model's lifecycle.




