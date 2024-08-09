---
title: StackAnimatorScope
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](../index.html)/[StackAnimatorScope](index.html)



# StackAnimatorScope



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [StackAnimatorScope](index.html)&lt;[Key](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [Instance](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(stack: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;, onBackstackChange: (stackEmpty: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), val itemKey: ([Instance](index.html)) -&gt; [Key](index.html), excludedDestinations: ([Instance](index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), animations: [DestinationAnimationsConfiguratorScope](../../com.nxoim.decomposite.core.common.navigation.animations/-destination-animations-configurator-scope/index.html)&lt;[Instance](index.html)&gt;.() -&gt; [ContentAnimations](../../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html), allowBatchRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val animationDataRegistry: [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt;)

Manages the children's animation state and modifiers. Creates instances of animator scopes avoiding duplicates.



## Constructors


| | |
|---|---|
| [StackAnimatorScope](-stack-animator-scope.html) | [common]<br>constructor(stack: () -&gt; [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;, onBackstackChange: (stackEmpty: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), itemKey: ([Instance](index.html)) -&gt; [Key](index.html), excludedDestinations: ([Instance](index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), animations: [DestinationAnimationsConfiguratorScope](../../com.nxoim.decomposite.core.common.navigation.animations/-destination-animations-configurator-scope/index.html)&lt;[Instance](index.html)&gt;.() -&gt; [ContentAnimations](../../com.nxoim.decomposite.core.common.navigation.animations/-content-animations/index.html), allowBatchRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), animationDataRegistry: [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt;) |


## Types


| Name | Summary |
|---|---|
| [ItemState](-item-state/index.html) | [common]<br>@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)<br>data class [ItemState](-item-state/index.html)(val index: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val indexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val displaying: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val allowingAnimation: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val inStack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), val animationData: [AnimationData](../-animation-data/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [animationDataHandler](animation-data-handler.html) | [common]<br>val [animationDataHandler](animation-data-handler.html): [AnimationDataHandler](../-animation-data-handler/index.html)&lt;[Key](index.html)&gt; |
| [animationDataRegistry](animation-data-registry.html) | [common]<br>val [animationDataRegistry](animation-data-registry.html): [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt; |
| [itemKey](item-key.html) | [common]<br>val [itemKey](item-key.html): ([Instance](index.html)) -&gt; [Key](index.html) |
| [sourceStack](source-stack.html) | [common]<br>val [sourceStack](source-stack.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt; |
| [visibleCachedChildren](visible-cached-children.html) | [common]<br>val [visibleCachedChildren](visible-cached-children.html): [SnapshotStateMap](https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/SnapshotStateMap.html)&lt;[Key](index.html), [Instance](index.html)&gt; |


## Functions


| Name | Summary |
|---|---|
| [createStackItemState](create-stack-item-state.html) | [common]<br>@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)<br>fun [createStackItemState](create-stack-item-state.html)(key: [Key](index.html)): [State](https://developer.android.com/reference/kotlin/androidx/compose/runtime/State.html)&lt;[StackAnimatorScope.ItemState](-item-state/index.html)&gt; |
| [observeAndUpdateAnimatorData](observe-and-update-animator-data.html) | [common]<br>suspend fun [observeAndUpdateAnimatorData](observe-and-update-animator-data.html)() |
| [updateGestureDataInScopes](update-gesture-data-in-scopes.html) | [common]<br>inline suspend fun [updateGestureDataInScopes](update-gesture-data-in-scopes.html)(backGestureData: [BackGestureEvent](../../com.nxoim.decomposite.core.common.ultils/-back-gesture-event/index.html)) |

