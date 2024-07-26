package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterExitState.PostExit
import androidx.compose.animation.EnterExitState.PreEnter
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.InternalAnimationApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.createChildTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.value.Value
import com.nxoim.decomposite.core.common.navigation.DecomposeChildInstance
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.ultils.ImmutableThingHolder
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import kotlinx.coroutines.launch

/**
 * Animates the stack. Caches the children for the time of animation.
 */
@OptIn(InternalDecomposeApi::class, ExperimentalTransitionApi::class)
@Composable
fun <C : Any, T : DecomposeChildInstance> StackAnimator(
	stackValue: ImmutableThingHolder<Value<ChildStack<C, T>>>,
	stackAnimatorScope: StackAnimatorScope<C>,
	modifier: Modifier = Modifier,
	onBackstackChange: (stackEmpty: Boolean) -> Unit,
	excludeStartingDestination: Boolean = false,
	allowBatchRemoval: Boolean = true,
	animations: DestinationAnimationsConfiguratorScope<C>.() -> ContentAnimations,
	content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
) = with(stackAnimatorScope) {
	key(stackAnimatorScope.key) {
		val holder = rememberSaveableStateHolder()
		var sourceStack by remember { mutableStateOf(stackValue.thing.value) }
		val removingChildren = remember { mutableStateListOf<C>() }
		val cachedChildrenInstances = remember {
			mutableStateMapOf<C, Child.Created<C, T>>().apply {
				putAll(
					stackValue.thing.items.subList(
						if (excludeStartingDestination) 1 else 0,
						stackValue.thing.items.size
					).associateBy { it.configuration }
				)
			}
		}

		LaunchedEffect(Unit) {
			// check on startup if there's animation data left for nonexistent children, which
			// can happen during a configuration change
			launch {
				removeStaleAnimationDataCache(nonStale = sourceStack.items.fastMap { it.configuration })
			}

			stackValue.thing.subscribe { newStackRaw ->
				onBackstackChange(newStackRaw.items.size <= 1)
				val oldStack = sourceStack.items
				val newStack = newStackRaw.items.subList(
					if (excludeStartingDestination) 1 else 0,
					stackValue.thing.items.size
				)

				val childrenToRemove =
					oldStack.filter { it !in newStack && it.configuration !in removingChildren }
				val batchRemoval = childrenToRemove.size > 1 && allowBatchRemoval

				// cancel removal of items that appeared again in the stack
				removingChildren.removeAll(newStackRaw.items.map { it.configuration })

				if (batchRemoval) {
					// remove from cache and everything all children, except the last one,
					// which will be animated
					val itemsToRemoveImmediately =
						childrenToRemove.subList(0, childrenToRemove.size - 1)
					itemsToRemoveImmediately.forEach { (configuration, _) ->
						cachedChildrenInstances.remove(configuration)
					}
					removingChildren.add(childrenToRemove.last().configuration)
				} else {
					childrenToRemove.forEach {
						removingChildren.add(it.configuration)
					}
				}

				sourceStack = newStackRaw

				cachedChildrenInstances.putAll(newStack.associateBy { it.configuration })
			}
		}

		Box(modifier) {
			val seekableTransitionState = remember {
				SeekableTransitionState(sourceStack.active)
			}
			val rootTransition = rememberTransition(seekableTransitionState)

			cachedChildrenInstances.forEach { (child, cachedInstance) ->
				key(child) {
					val inStack = !removingChildren.contains(child)
					val instance by remember {
						derivedStateOf {
							sourceStack.items.find { it.configuration == child } ?: cachedInstance
						}
					}

					val index = if (inStack)
						sourceStack.items.indexOf(instance)
					else
						-(removingChildren.indexOf(child) + 1)

					val indexFromTop = if (inStack)
						sourceStack.items.size - index - 1
					else
						-(removingChildren.indexOf(child) + 1)

					val allAnimations = animations(
						DestinationAnimationsConfiguratorScope(
							sourceStack.items.elementAt(index - 1).configuration,
							child,
							sourceStack.items.elementAt(index + 1).configuration,
							LocalNavigationRoot.current.screenInformation
						)
					)
					val animData = remember(allAnimations) {
						getOrCreateAnimationData(
							key = child,
							source = allAnimations,
							initialIndex = index,
							initialIndexFromTop = if (indexFromTop == 0 && index != 0)
								-1
							else
								indexFromTop
						)
					}

					val allowingAnimation = indexFromTop <= (animData.renderUntils.min())

					val animating by remember {
						derivedStateOf {
							animData.scopes.any { it.value.animationStatus.animating }
						}
					}

					val displaying = remember(animating, allowingAnimation) {
						val requireVisibilityInBack =
							animData.requireVisibilityInBackstacks.fastAny { it }
						val renderingBack = allowingAnimation && animating
						val renderTopAndAnimatedBack = indexFromTop < 1 || renderingBack
						if (requireVisibilityInBack) allowingAnimation else renderTopAndAnimatedBack
					}

					val firstAnimData = animData.scopes.values.first()
//					val animStatus = firstAnimData.animationStatus


//					val animatedVisibilityScope = remember {
//						AnimatedVisibilityScopeImpl()
//					}
//
//					val exiting = animStatus.fromTopToOutside
//							|| animStatus.fromBackToOutside
//							|| animStatus.fromTopIntoBack

//					val entering = animStatus.fromOutsideIntoTop
//							|| animStatus.fromBackIntoTop


//					val enterEnded = animStatus.location.top && !animStatus.animating

//					val targetForSeeking = when {
//						animStatus.animationType.passiveCancelling
//								|| exiting -> EnterExitState.PostExit
//
//						entering || enterEnded -> EnterExitState.Visible
//
//						else -> EnterExitState.PreEnter
//
//					}
					val progress = firstAnimData.animationProgressForScope.value

					LaunchedEffect(progress) {
						val previousInstance = sourceStack.backStack.lastOrNull()

						if (indexFromTop == 0) {
							previousInstance?.let {
								seekableTransitionState.seekTo(
									(-progress).coerceIn(0f, 1f),
									it
								)
							}
						}
					}

					LaunchedEffect(indexFromTop) {
						if (indexFromTop == 0) {
							seekableTransitionState.animateTo(instance)
						}
					}

					LaunchedEffect(allowingAnimation, inStack) {
						stackAnimatorScope.updateChildAnimPrerequisites(
							child,
							allowingAnimation,
							inStack
						)
					}

					// launch animations if there's changes
					LaunchedEffect(indexFromTop, index) {
						animData.scopes.forEach { (_, scope) ->
							launch {
								scope.update(
									newIndex = index,
									newIndexFromTop = indexFromTop,
									animate = scope.indexFromTop != indexFromTop || indexFromTop < 1
								)

//								if (indexFromTop == 0) seekableTransitionState.snapTo(
//									instance
//								)

								// after animating, if is not in stack
								if (!inStack) cachedChildrenInstances.remove(child)
							}
						}
					}

					// will get triggered upon removal
					OnDestinationDisposeEffect(
						instance.configuration.hashString() + stackAnimatorScope.key + "OnDestinationDisposeEffect",
						waitForCompositionRemoval = true,
						componentContext = instance.instance.componentContext
					) {
						removingChildren.remove(child)
						removeAnimationDataFromCache(child)
						holder.removeState(childHolderKey(child))
					}

					AnimatedVisibilityScopeProvider(
						rootTransition,
						visible = { it == instance },
						Modifier.accumulate(animData.modifiers).zIndex((-indexFromTop).toFloat()),
						displaying = { displaying },
						shouldDisposeBlock = { _, _ -> false }
					) {
						holder.SaveableStateProvider(childHolderKey(child)) {
							content(instance)
						}

						val text = """
							from ${this.transition.currentState} to ${this.transition.targetState}
							
							${this.transition}
						""".trimIndent()

						BasicText(
							text,
							color = { Color.White },
							modifier = Modifier.offset(y = 60.dp),
							style = TextStyle(
								textAlign = TextAlign.Center,
								fontSize = 8.sp
							)
						)
					}
				}
			}
		}
	}
}

@OptIn(InternalDecomposeApi::class)
private fun <C : Any> childHolderKey(child: C) =
	child.hashString() + " StackAnimator SaveableStateHolder"

/**
 * Provides data helpful for the configuration of animations.
 */
data class DestinationAnimationsConfiguratorScope<C : Any>(
	val previousChild: C?,
	val currentChild: C,
	val nextChild: C?,
	val screenInformation: ScreenInformation
)

// This converts Boolean visible to EnterExitState
@Composable
private fun <T> Transition<T>.targetEnterExit(
	visible: (T) -> Boolean,
	targetState: T
): EnterExitState = key(this) {
	if (this.isSeeking) {
		if (visible(targetState)) {
			Visible
		} else {
			if (visible(this.currentState)) {
				PostExit
			} else {
				PreEnter
			}
		}
	} else {
		val hasBeenVisible = remember { mutableStateOf(false) }
		if (visible(currentState)) {
			hasBeenVisible.value = true
		}
		if (visible(targetState)) {
			EnterExitState.Visible
		} else {
			// If never been visible, visible = false means PreEnter, otherwise PostExit
			if (hasBeenVisible.value) {
				EnterExitState.PostExit
			} else {
				EnterExitState.PreEnter
			}
		}
	}
}


private val Transition<EnterExitState>.exitFinished
	get() = currentState == PostExit && targetState == PostExit

//fun <T> Transition<T>.isEnteringAnew() =
//	currentState == EnterExitState.PreEnter
//			&& targetState == EnterExitState.Visible
//
//fun <T> Transition<T>.isExitingOrCancelling() =
//	currentState == EnterExitState.Visible
//			&& targetState == EnterExitState.PostExit
//
//fun <T> Transition<T>.isCancelled() =
//	currentState == EnterExitState.PostExit
//			&& targetState == EnterExitState.Visible
//
//fun <T> Transition<T>.isIllegalState() =
//	(currentState == targetState)
//			|| (currentState == EnterExitState.PreEnter && targetState == EnterExitState.PostExit)
//			|| (currentState == EnterExitState.PostExit && targetState == EnterExitState.PreEnter)

private class AnimatedVisibilityScopeImpl(
	_transition: Transition<EnterExitState>
) : AnimatedVisibilityScope {
	override var transition = _transition
	val targetSize = mutableStateOf(IntSize.Zero)
}

//private fun AnimationStatus.toTargetEnterExitState() = when {
//	fromOutsideIntoTop -> EnterExitState.Visible
//	fromTopToOutside -> EnterExitState.PostExit
//
//	fromTopIntoBack -> EnterExitState.PostExit
//	fromBackIntoTop -> EnterExitState.Visible
//
//	location.top && animationType.passiveCancelling -> EnterExitState.PostExit
//	location.back && animationType.passiveCancelling -> EnterExitState.Visible
//
//	location.top && !animating -> EnterExitState.Visible
//	(location.back && !animating) || fromBackToBack -> EnterExitState.PreEnter
//
//	else -> error("function toTargetEnterExitState. $this")
//}

private class AnimatedEnterExitMeasurePolicy(
	val scope: AnimatedVisibilityScopeImpl
) : MeasurePolicy {
	var hasLookaheadOccurred = false
	override fun MeasureScope.measure(
		measurables: List<Measurable>,
		constraints: Constraints
	): MeasureResult {
		val placeables = measurables.fastMap { it.measure(constraints) }
		val maxWidth: Int = placeables.fastMaxBy { it.width }?.width ?: 0
		val maxHeight = placeables.fastMaxBy { it.height }?.height ?: 0
		// Position the children.
		if (isLookingAhead) {
			hasLookaheadOccurred = true
			scope.targetSize.value = IntSize(maxWidth, maxHeight)
		} else if (!hasLookaheadOccurred) {
			// Not in lookahead scope.
			scope.targetSize.value = IntSize(maxWidth, maxHeight)
		}
		return layout(maxWidth, maxHeight) {
			placeables.fastForEach {
				it.place(0, 0)
			}
		}
	}

	override fun IntrinsicMeasureScope.minIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int
	) = measurables.fastMaxOfOrNull { it.minIntrinsicWidth(height) } ?: 0

	override fun IntrinsicMeasureScope.minIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int
	) = measurables.fastMaxOfOrNull { it.minIntrinsicHeight(width) } ?: 0

	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
		measurables: List<IntrinsicMeasurable>,
		height: Int
	) = measurables.fastMaxOfOrNull { it.maxIntrinsicWidth(height) } ?: 0

	override fun IntrinsicMeasureScope.maxIntrinsicHeight(
		measurables: List<IntrinsicMeasurable>,
		width: Int
	) = measurables.fastMaxOfOrNull { it.maxIntrinsicHeight(width) } ?: 0
}

//enum class Thing { Visible, Hidden }

//@Composable
//internal fun Transition<EnterExitState>.trackActiveEnter(enter: EnterTransition): EnterTransition {
//	// Active enter & active exit reference the enter and exit transition that is currently being
//	// used. It is important to preserve the active enter/exit that was previously used before
//	// changing target state, such that if the previous enter/exit is interrupted, we still hold
//	// reference to the enter/exit that define those animations and therefore could recover.
//	var activeEnter by remember(this) { mutableStateOf(enter) }
//	if (currentState == targetState && currentState == EnterExitState.Visible) {
//		if (isSeeking) {
//			// When seeking, the timing is different and there's no need to handle interruptions.
//			activeEnter = enter
//		} else {
//			activeEnter = EnterTransition.None
//		}
//	} else if (targetState == EnterExitState.Visible) {
//		activeEnter += enter
//	}
//	return activeEnter
//}

//@Composable
//internal fun Transition<EnterExitState>.trackActiveExit(exit: ExitTransition): ExitTransition {
//	// Active enter & active exit reference the enter and exit transition that is currently being
//	// used. It is important to preserve the active enter/exit that was previously used before
//	// changing target state, such that if the previous enter/exit is interrupted, we still hold
//	// reference to the enter/exit that define those animations and therefore could recover.
//	var activeExit by remember(this) { mutableStateOf(exit) }
//	if (currentState == targetState && currentState == EnterExitState.Visible) {
//		if (isSeeking) {
//			// When seeking, the timing is different and there's no need to handle interruptions.
//			activeExit = exit
//		} else {
//			activeExit = ExitTransition.None
//		}
//	} else if (targetState != EnterExitState.Visible) {
//		activeExit += exit
//	}
//	return activeExit
//}
/**
 * Observes lookahead size.
 */
fun interface OnLookaheadMeasured {
	fun invoke(size: IntSize)
}

@OptIn(InternalAnimationApi::class, ExperimentalTransitionApi::class)
@Composable
internal fun <T> AnimatedVisibilityScopeProvider(
	transition: Transition<T>,
	visible: (T) -> Boolean,
	modifier: Modifier,
	displaying: () -> Boolean = {
		visible(transition.targetState) || visible(transition.currentState) ||
				transition.isSeeking || transition.hasInitialValueAnimations
	},
	shouldDisposeBlock: (EnterExitState, EnterExitState) -> Boolean,
	onLookaheadMeasured: OnLookaheadMeasured? = null,
	content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
	if (displaying()) {
		val childTransition = transition.createChildTransition(label = "EnterExitTransition") {
			transition.targetEnterExit(visible, it)
		}

		val shouldDisposeBlockUpdated by rememberUpdatedState(shouldDisposeBlock)

		val shouldDisposeAfterExit by produceState(
			initialValue = shouldDisposeBlock(
				childTransition.currentState,
				childTransition.targetState
			)
		) {
			snapshotFlow { childTransition.exitFinished }.collect {
				value = if (it) {
					shouldDisposeBlockUpdated(
						childTransition.currentState,
						childTransition.targetState
					)
				} else {
					false
				}
			}
		}

		if (!childTransition.exitFinished || !shouldDisposeAfterExit) {
			val scope = remember(transition) { AnimatedVisibilityScopeImpl(childTransition) }

			Layout(
				content = { scope.content() },
				modifier = modifier.let {
					if (onLookaheadMeasured != null) it.layout { measurable, constraints ->
						measurable
							.measure(constraints)
							.run {
								if (isLookingAhead) {
									onLookaheadMeasured.invoke(IntSize(width, height))
								}

								layout(width, height) { place(0, 0) }
							}
					} else
						it
				},
				measurePolicy = remember { AnimatedEnterExitMeasurePolicy(scope) }
			)
		}
	}
}

//private val Transition<EnterExitState>.exitFinished
//	get() = currentState == PostExit && targetState == PostExit
//
//private class AnimatedEnterExitMeasurePolicy(
//	val scope: androidx.compose.animation.AnimatedVisibilityScopeImpl
//) : MeasurePolicy {
//	var hasLookaheadOccurred = false
//	override fun MeasureScope.measure(
//		measurables: List<Measurable>,
//		constraints: Constraints
//	): MeasureResult {
//		val placeables = measurables.fastMap { it.measure(constraints) }
//		val maxWidth: Int = placeables.fastMaxBy { it.width }?.width ?: 0
//		val maxHeight = placeables.fastMaxBy { it.height }?.height ?: 0
//		// Position the children.
//		if (isLookingAhead) {
//			hasLookaheadOccurred = true
//			scope.targetSize.value = IntSize(maxWidth, maxHeight)
//		} else if (!hasLookaheadOccurred) {
//			// Not in lookahead scope.
//			scope.targetSize.value = IntSize(maxWidth, maxHeight)
//		}
//		return layout(maxWidth, maxHeight) {
//			placeables.fastForEach {
//				it.place(0, 0)
//			}
//		}
//	}
//
//	override fun IntrinsicMeasureScope.minIntrinsicWidth(
//		measurables: List<IntrinsicMeasurable>,
//		height: Int
//	) = measurables.fastMaxOfOrNull { it.minIntrinsicWidth(height) } ?: 0
//
//	override fun IntrinsicMeasureScope.minIntrinsicHeight(
//		measurables: List<IntrinsicMeasurable>,
//		width: Int
//	) = measurables.fastMaxOfOrNull { it.minIntrinsicHeight(width) } ?: 0
//
//	override fun IntrinsicMeasureScope.maxIntrinsicWidth(
//		measurables: List<IntrinsicMeasurable>,
//		height: Int
//	) = measurables.fastMaxOfOrNull { it.maxIntrinsicWidth(height) } ?: 0
//
//	override fun IntrinsicMeasureScope.maxIntrinsicHeight(
//		measurables: List<IntrinsicMeasurable>,
//		width: Int
//	) = measurables.fastMaxOfOrNull { it.maxIntrinsicHeight(width) } ?: 0
//}

//// This converts Boolean visible to EnterExitState
//@Composable
//private fun <T> Transition<T>.targetEnterExit(
//	visible: (T) -> Boolean,
//	targetState: T
//): EnterExitState = key(this) {
//
//	if (this.isSeeking) {
//		if (visible(targetState)) {
//			Visible
//		} else {
//			if (visible(this.currentState)) {
//				PostExit
//			} else {
//				PreEnter
//			}
//		}
//	} else {
//		val hasBeenVisible = remember { mutableStateOf(false) }
//		if (visible(currentState)) {
//			hasBeenVisible.value = true
//		}
//		if (visible(targetState)) {
//			EnterExitState.Visible
//		} else {
//			// If never been visible, visible = false means PreEnter, otherwise PostExit
//			if (hasBeenVisible.value) {
//				EnterExitState.PostExit
//			} else {
//				EnterExitState.PreEnter
//			}
//		}
//	}
//}
