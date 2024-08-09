---
title: ViewModelStore
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.viewModel](../index.html)/[ViewModelStore](index.html)



# ViewModelStore



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [ViewModelStore](index.html)



## Constructors


| | |
|---|---|
| [ViewModelStore](-view-model-store.html) | [common]<br>constructor() |


## Properties


| Name | Summary |
|---|---|
| [lazyVmReferences](lazy-vm-references.html) | [common]<br>val [lazyVmReferences](lazy-vm-references.html): [HashMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-hash-map/index.html)&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; |
| [store](store.html) | [common]<br>val [store](store.html): [HashMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-hash-map/index.html)&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; |


## Functions


| Name | Summary |
|---|---|
| [get](get.html) | [common]<br>inline fun &lt;[T](get.html) : [ViewModel](../-view-model/index.html)&gt; [get](get.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): [T](get.html) |
| [getLazyViewModel](get-lazy-view-model.html) | [common]<br>inline fun &lt;[T](get-lazy-view-model.html) : [ViewModel](../-view-model/index.html)&gt; [getLazyViewModel](get-lazy-view-model.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): [T](get-lazy-view-model.html) |
| [getOrCreateViewModel](get-or-create-view-model.html) | [common]<br>inline fun &lt;[T](get-or-create-view-model.html) : [ViewModel](../-view-model/index.html)&gt; [getOrCreateViewModel](get-or-create-view-model.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), crossinline creator: () -&gt; [T](get-or-create-view-model.html)): [T](get-or-create-view-model.html) |
| [prepareLazyViewModel](prepare-lazy-view-model.html) | [common]<br>inline fun &lt;[T](prepare-lazy-view-model.html) : [ViewModel](../-view-model/index.html)&gt; [prepareLazyViewModel](prepare-lazy-view-model.html)(key: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), noinline creator: () -&gt; [T](prepare-lazy-view-model.html)) |
| [remove](remove.html) | [common]<br>fun [remove](remove.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |

