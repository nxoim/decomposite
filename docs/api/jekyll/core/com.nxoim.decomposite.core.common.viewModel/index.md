---
title: com.nxoim.decomposite.core.common.viewModel
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.viewModel](index.html)



# Package-level declarations



## Types


| Name | Summary |
|---|---|
| [ViewModel](-view-model/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>open class [ViewModel](-view-model/index.html)<br>Basic view model that is similar to the one that's offered by google for android. |
| [ViewModelStore](-view-model-store/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>class [ViewModelStore](-view-model-store/index.html) |


## Properties


| Name | Summary |
|---|---|
| [LocalViewModelStore](-local-view-model-store.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>val [LocalViewModelStore](-local-view-model-store.html): [ProvidableCompositionLocal](https://developer.android.com/reference/kotlin/androidx/compose/runtime/ProvidableCompositionLocal.html)&lt;[ViewModelStore](-view-model-store/index.html)&gt; |


## Functions


| Name | Summary |
|---|---|
| [getExistingViewModel](get-existing-view-model.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>inline fun &lt;[T](get-existing-view-model.html) : [ViewModel](-view-model/index.html)&gt; [getExistingViewModel](get-existing-view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;): [T](get-existing-view-model.html)<br>Gets an existing view model instance. Does not manage the view model's lifecycle. |
| [getLazyViewModel](get-lazy-view-model.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>inline fun &lt;[T](get-lazy-view-model.html) : [ViewModel](-view-model/index.html)&gt; [getLazyViewModel](get-lazy-view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;): [T](get-lazy-view-model.html)<br>Creates a view model instance using the prepared (by [prepareLazyViewModel](prepare-lazy-view-model.html)) reference if an instance does not exist. The first instance manages the view model's lifecycle. |
| [prepareLazyViewModel](prepare-lazy-view-model.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>inline fun &lt;[T](prepare-lazy-view-model.html) : [ViewModel](-view-model/index.html)&gt; [prepareLazyViewModel](prepare-lazy-view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;, noinline viewModel: () -&gt; [T](prepare-lazy-view-model.html))<br>Prepares a simple lazy view model. A reference is saved in [ViewModelStore](-view-model-store/index.html) which is later called by [getLazyViewModel](get-lazy-view-model.html) |
| [viewModel](view-model.html) | [common]<br>@[Stable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Stable.html)<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>inline fun &lt;[T](view-model.html) : [ViewModel](-view-model/index.html)&gt; [viewModel](view-model.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = &quot;&quot;, crossinline viewModel: () -&gt; [T](view-model.html)): [T](view-model.html)<br>Android-like view model instancer. Will get or create a [ViewModel](-view-model/index.html) instance in the [LocalViewModelStore](-local-view-model-store.html). Provide [key](view-model.html) if you have multiple instances of the same view model, or it will get the first created one. [ViewModel.onDestroy](-view-model/on-destroy.html) method will be called when the component/destination is removed and AFTER the composable gets destroyed. |

