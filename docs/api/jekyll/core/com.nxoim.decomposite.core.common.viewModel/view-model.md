---
title: viewModel
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.viewModel](index.html)/[viewModel](view-model.html)



# viewModel



[common]\




@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)



@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



inline fun &lt;[T](view-model.html) : [ViewModel](-view-model/index.html)&gt; [viewModel](view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;, crossinline viewModel: () -&gt; [T](view-model.html)): [T](view-model.html)



Android-like view model instancer. Will get or create a [ViewModel](-view-model/index.html) instance in the [LocalViewModelStore](-local-view-model-store.html). Provide [key](view-model.html) if you have multiple instances of the same view model, or it will get the first created one. [ViewModel.onDestroy](-view-model/on-destroy.html) method will be called when the component/destination is removed and AFTER the composable gets destroyed.




