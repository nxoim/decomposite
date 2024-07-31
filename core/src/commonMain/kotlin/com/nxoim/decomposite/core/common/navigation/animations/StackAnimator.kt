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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.passiveCancelling
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.outside
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top
import kotlinx.coroutines.launch

/**
 * Animates the stack. Caches the children for the time of animation.
 */
@OptIn(InternalDecomposeApi::class)
@Composable
fun <Key : Any, Instance : Any> StackAnimator(
	stackAnimatorScope: StackAnimatorScope<Key, Instance>,
	modifier: Modifier = Modifier,
	animations: DestinationAnimationsConfiguratorScope<Key>.() -> ContentAnimations,
	content: @Composable AnimatedVisibilityScope.(child: Instance) -> Unit,
) = key(stackAnimatorScope.key) {
	val holder = rememberSaveableStateHolder()

	LaunchedEffect(Unit) { stackAnimatorScope.observeAndUpdateAnimatorData() }

	Box(modifier) {
		val seekableTransitionState = remember {
			SeekableTransitionState(
				stackAnimatorScope.itemKey(stackAnimatorScope.sourceStack.last())
			)
		}
		val transition = rememberTransition(seekableTransitionState)

		stackAnimatorScope.visibleCachedChildren.forEach { (childKey, cachedInstance) ->
			// keys are needed for animations to work. something something
			// correctness something something control flow
			key(childKey) {
				val state by stackAnimatorScope.itemState(childKey, animations)

				// for seekable transitions
				val firstAnimData = state.animationData.scopes.values.first()
				val animationStatus = firstAnimData.animationStatus

				// TODO: should i keep this
				LaunchedEffect(state.indexFromTop) {
					if (state.indexFromTop == 0) seekableTransitionState.animateTo(childKey)
				}

				LaunchedEffect(animationStatus.animating) {
					if (state.indexFromTop == 0 && !animationStatus.animating) {
						seekableTransitionState.snapTo(childKey)
					}
				}

				LaunchedEffect(Unit) {
					snapshotFlow { firstAnimData.animationProgressForScope }
						.collect() {
							if (animationStatus.fromBackIntoTop) {
								seekableTransitionState.seekTo(
									(1f - it).coerceIn(0f, 1f),
									childKey
								)
							}

							if (animationStatus.fromOutsideIntoTop) {
								seekableTransitionState.seekTo(
									(1f + it).coerceIn(0f, 1f),
									childKey
								)
							}

							if (animationStatus.run { animationType.passiveCancelling && location.top }) {
								seekableTransitionState.seekTo((-it).coerceIn(0f, 1f))
							}
						}
				}

				// launch animations if there's changes
				LaunchedEffect(state.indexFromTop, state.index) {
					state.animationData.scopes.forEach { (_, scope) ->
						launch {
							scope.update(
								newIndex = state.index,
								newIndexFromTop = state.indexFromTop,
							)

							// note: animations called in scope.update block this
							// until theyre finished. snapTo is supposed
							// to be called when animations are done
							if (state.indexFromTop == 0) seekableTransitionState.snapTo(
								childKey
							)
						}
					}
				}

				AnimatedVisibilityScopeProvider(
					transition,
					visible = { it == cachedInstance },
					Modifier
						.accumulate(remember { state.animationData.modifiers })
						.zIndex((-state.indexFromTop).toFloat()),
					displaying = { state.displaying },
					shouldDisposeBlock = { _, _ -> false }
				) {
					holder.SaveableStateProvider(childHolderKey(childKey)) {
						content(cachedInstance)

						// rely only on the reported animation status
						if (
							animationStatus.previousLocation?.outside == false
							&& animationStatus.location.outside
							&& !animationStatus.animating
						) {
							LaunchedEffect(Unit) {
								stackAnimatorScope.visibleCachedChildren.remove(childKey)
								stackAnimatorScope.removingChildren.remove(childKey)
								stackAnimatorScope.removeAnimationDataFromCache(childKey)
								holder.removeState(childHolderKey(childKey))
							}
						}
					}
				}
			}
		}
	}
}

private inline fun Modifier.accumulate(modifiers: List<Modifier>) =
	modifiers.fold(initial = this) { acc, modifier -> acc.then(modifier) }

@OptIn(InternalDecomposeApi::class)
private fun <C : Any> childHolderKey(child: C) =
	child.hashString() + " StackAnimator SaveableStateHolder"

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
			if (visible(this.currentState)) PostExit else PreEnter
		}
	} else {
		val hasBeenVisible = remember { mutableStateOf(false) }

		if (visible(currentState)) hasBeenVisible.value = true

		if (visible(targetState)) {
			Visible
		} else {
			// If never been visible, visible = false means PreEnter, otherwise PostExit
			if (hasBeenVisible.value) PostExit else PreEnter
		}
	}
}


private val Transition<EnterExitState>.exitFinished
	get() = currentState == PostExit && targetState == PostExit


private class AnimatedVisibilityScopeImpl(
	_transition: Transition<EnterExitState>
) : AnimatedVisibilityScope {
	override var transition = _transition
	val targetSize = mutableStateOf(IntSize.Zero)
}

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

/**
 * Observes lookahead size.
 */
private fun interface OnLookaheadMeasured {
	fun invoke(size: IntSize)
}

@OptIn(InternalAnimationApi::class, ExperimentalTransitionApi::class)
@Composable
private fun <T> AnimatedVisibilityScopeProvider(
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