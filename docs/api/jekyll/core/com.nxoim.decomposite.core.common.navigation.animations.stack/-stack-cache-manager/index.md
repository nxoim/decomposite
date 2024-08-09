---
title: StackCacheManager
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](../index.html)/[StackCacheManager](index.html)



# StackCacheManager



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [StackCacheManager](index.html)&lt;[Key](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), [Instance](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(initialStack: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;, itemKey: ([Instance](index.html)) -&gt; [Key](index.html), excludedDestinations: ([Instance](index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), allowBatchRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), onItemBatchRemoved: ([Key](index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))

Manages the stack cache and helper data for the stack animator.



## Constructors


| | |
|---|---|
| [StackCacheManager](-stack-cache-manager.html) | [common]<br>constructor(initialStack: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;, itemKey: ([Instance](index.html)) -&gt; [Key](index.html), excludedDestinations: ([Instance](index.html)) -&gt; [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), allowBatchRemoval: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), onItemBatchRemoved: ([Key](index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)) |


## Properties


| Name | Summary |
|---|---|
| [removingChildren](removing-children.html) | [common]<br>val [removingChildren](removing-children.html): [SnapshotStateList](https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/SnapshotStateList.html)&lt;[Key](index.html)&gt;<br>This is useful for tracking exiting children and their order, which is not possible with sourceStack.contains(something) |
| [sourceStack](source-stack.html) | [common]<br>var [sourceStack](source-stack.html): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;<br>This is basically a duplicate of the raw source stack. It's necessary to control the order of operations for correct animation data calculation. |
| [visibleCachedChildren](visible-cached-children.html) | [common]<br>val [visibleCachedChildren](visible-cached-children.html): [SnapshotStateMap](https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/SnapshotStateMap.html)&lt;[Key](index.html), [Instance](index.html)&gt;<br>Caching all children in an observable manner, so all updates are reflected in the ui. |


## Functions


| Name | Summary |
|---|---|
| [removeAllRelatedToItem](remove-all-related-to-item.html) | [common]<br>fun [removeAllRelatedToItem](remove-all-related-to-item.html)(key: [Key](index.html)) |
| [updateVisibleCachedChildren](update-visible-cached-children.html) | [common]<br>fun [updateVisibleCachedChildren](update-visible-cached-children.html)(newStackRaw: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Instance](index.html)&gt;) |

