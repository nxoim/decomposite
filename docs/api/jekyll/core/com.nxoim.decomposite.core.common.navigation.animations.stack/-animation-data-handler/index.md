---
title: AnimationDataHandler
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.stack](../index.html)/[AnimationDataHandler](index.html)



# AnimationDataHandler



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [AnimationDataHandler](index.html)&lt;[Key](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(val animationDataRegistry: [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt;)

Manages the animation data cache.



## Constructors


| | |
|---|---|
| [AnimationDataHandler](-animation-data-handler.html) | [common]<br>constructor(animationDataRegistry: [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt;) |


## Properties


| Name | Summary |
|---|---|
| [animationDataRegistry](animation-data-registry.html) | [common]<br>val [animationDataRegistry](animation-data-registry.html): [AnimationDataRegistry](../-animation-data-registry/index.html)&lt;[Key](index.html)&gt; |
| [childAnimPrerequisites](child-anim-prerequisites.html) | [common]<br>val [childAnimPrerequisites](child-anim-prerequisites.html): [HashMap](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-hash-map/index.html)&lt;[Key](index.html), [ChildAnimPrerequisites](../-child-anim-prerequisites/index.html)&gt;<br>Cache for child anim prerequisites so they arent recalculated hundreds of times per second during gestures |


## Functions


| Name | Summary |
|---|---|
| [removeAnimationDataFromCache](remove-animation-data-from-cache.html) | [common]<br>fun [removeAnimationDataFromCache](remove-animation-data-from-cache.html)(target: [Key](index.html)) |
| [removeStaleAnimationDataCache](remove-stale-animation-data-cache.html) | [common]<br>fun [removeStaleAnimationDataCache](remove-stale-animation-data-cache.html)(nonStale: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Key](index.html)&gt;) |
| [updateChildAnimPrerequisites](update-child-anim-prerequisites.html) | [common]<br>fun [updateChildAnimPrerequisites](update-child-anim-prerequisites.html)(key: [Key](index.html), allowAnimation: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), inStack: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |
| [updateGestureDataInScopes](update-gesture-data-in-scopes.html) | [common]<br>inline suspend fun [updateGestureDataInScopes](update-gesture-data-in-scopes.html)(backGestureData: [BackGestureEvent](../../com.nxoim.decomposite.core.common.ultils/-back-gesture-event/index.html)) |

