---
title: prepareLazyViewModel
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.viewModel](index.html)/[prepareLazyViewModel](prepare-lazy-view-model.html)



# prepareLazyViewModel



[common]\




@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)



@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



inline fun &lt;[T](prepare-lazy-view-model.html) : [ViewModel](-view-model/index.html)&gt; [prepareLazyViewModel](prepare-lazy-view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;, noinline viewModel: () -&gt; [T](prepare-lazy-view-model.html))



Prepares a simple lazy view model. A reference is saved in [ViewModelStore](-view-model-store/index.html) which is later called by [getLazyViewModel](get-lazy-view-model.html)




