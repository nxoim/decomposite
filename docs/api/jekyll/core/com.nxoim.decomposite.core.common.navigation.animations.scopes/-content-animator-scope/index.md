---
title: ContentAnimatorScope
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.scopes](../index.html)/[ContentAnimatorScope](index.html)



# ContentAnimatorScope

interface [ContentAnimatorScope](index.html)

Base for the content animator scope implementations. Describes the bare minimum needed. When implementing - keep in mind:



- 
   elements, appearing and disappearing from the stack, update the state and trigger animations
- 
   [onBackGesture](on-back-gesture.html) represents the user's actions and should not be used to manipulate the stack
- 
   [indexFromTop](index-from-top.html) represents the index of the item from the top of the stack, with 0 being the top. Negative numbers represent an item not existing in the stack while being animated. If several items are being removed and all are animated at the same time - [indexFromTop](index-from-top.html) will be represent the order of the items being removed, -1 being the latest item that has been removed. Yes, the number can be less than -1.




[animationProgressForScope](animation-progress-for-scope.html) is a value that is used to provide [AnimatedVisibilityScope](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedVisibilityScope.html)'s to the content for things like modifiers that depend on [SharedTransitionScope](https://developer.android.com/reference/kotlin/androidx/compose/animation/SharedTransitionScope.html). The resulting value provided to the [AnimatedVisibilityScope](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedVisibilityScope.html) is constrained to a range between 0f and 1f. It's expected to mirror [indexFromTop](index-from-top.html), meaning it must be -1 when the item is outside of the stack, 0 when at the top of the stack, and 1 when at the back of the stack. It's also expected to depend on back gestures.



Note: when several animations with different keys are used for a single item - the first scope is used to provide [animationProgressForScope](animation-progress-for-scope.html). Refer to [ContentAnimator](../../com.nxoim.decomposite.core.common.navigation.animations/-content-animator/index.html) and [contentAnimator](../content-animator.html) for more information.



#### Inheritors


| |
|---|
| [DefaultContentAnimatorScope](../-default-content-animator-scope/index.html) |


## Properties


| Name | Summary |
|---|---|
| [animationProgressForScope](animation-progress-for-scope.html) | [common]<br>abstract val [animationProgressForScope](animation-progress-for-scope.html): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [animationStatus](animation-status.html) | [common]<br>abstract val [animationStatus](animation-status.html): [AnimationStatus](../../com.nxoim.decomposite.core.common.navigation.animations/-animation-status/index.html) |
| [index](--index--.html) | [common]<br>abstract val [index](--index--.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [indexFromTop](index-from-top.html) | [common]<br>abstract val [indexFromTop](index-from-top.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |


## Functions


| Name | Summary |
|---|---|
| [onBackGesture](on-back-gesture.html) | [common]<br>abstract suspend fun [onBackGesture](on-back-gesture.html)(backGesture: [BackGestureEvent](../../com.nxoim.decomposite.core.common.ultils/-back-gesture-event/index.html)): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html) |
| [update](update.html) | [common]<br>abstract suspend fun [update](update.html)(newIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), newIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

