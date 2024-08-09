---
title: DefaultContentAnimatorScope
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation.animations.scopes](../index.html)/[DefaultContentAnimatorScope](index.html)



# DefaultContentAnimatorScope



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [DefaultContentAnimatorScope](index.html)(initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), animationSpec: [AnimationSpec](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/AnimationSpec.html)&lt;[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)&gt;) : [ContentAnimatorScope](../-content-animator-scope/index.html)



## Constructors


| | |
|---|---|
| [DefaultContentAnimatorScope](-default-content-animator-scope.html) | [common]<br>constructor(initialIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), initialIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), animationSpec: [AnimationSpec](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/AnimationSpec.html)&lt;[Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)&gt;) |


## Properties


| Name | Summary |
|---|---|
| [animationProgress](animation-progress.html) | [common]<br>val [animationProgress](animation-progress.html): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [animationProgressForScope](animation-progress-for-scope.html) | [common]<br>open override val [animationProgressForScope](animation-progress-for-scope.html): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [animationStatus](animation-status.html) | [common]<br>open override var [animationStatus](animation-status.html): [AnimationStatus](../../com.nxoim.decomposite.core.common.navigation.animations/-animation-status/index.html) |
| [backEvent](back-event.html) | [common]<br>var [backEvent](back-event.html): BackEvent |
| [gestureAnimationProgress](gesture-animation-progress.html) | [common]<br>val [gestureAnimationProgress](gesture-animation-progress.html): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [index](--index--.html) | [common]<br>open override var [index](--index--.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [indexFromTop](index-from-top.html) | [common]<br>open override var [indexFromTop](index-from-top.html): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [swipeOffset](swipe-offset.html) | [common]<br>val [swipeOffset](swipe-offset.html): [Offset](https://developer.android.com/reference/kotlin/androidx/compose/ui/geometry/Offset.html) |


## Functions


| Name | Summary |
|---|---|
| [onBackGesture](on-back-gesture.html) | [common]<br>open suspend override fun [onBackGesture](on-back-gesture.html)(backGesture: [BackGestureEvent](../../com.nxoim.decomposite.core.common.ultils/-back-gesture-event/index.html)) |
| [update](update.html) | [common]<br>open suspend override fun [update](update.html)(newIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), newIndexFromTop: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

