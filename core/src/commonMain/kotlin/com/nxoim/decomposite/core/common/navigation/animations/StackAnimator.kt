package com.nxoim.decomposite.core.common.navigation.animations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.value.Value
import com.nxoim.decomposite.core.common.navigation.DecomposeChildInstance
import com.nxoim.decomposite.core.common.navigation.LocalNavigationRoot
import com.nxoim.decomposite.core.common.navigation.animations.AnimationType.Companion.passiveCancelling
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.back
import com.nxoim.decomposite.core.common.navigation.animations.ItemLocation.Companion.top
import com.nxoim.decomposite.core.common.ultils.ImmutableThingHolder
import com.nxoim.decomposite.core.common.ultils.OnDestinationDisposeEffect
import com.nxoim.decomposite.core.common.ultils.ScreenInformation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Animates the stack. Caches the children for the time of animation.
 */
@OptIn(InternalDecomposeApi::class)
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

					// i have no idea how to use EnterExitState. i literally
					// bruteforced this. it took me 2 days
					val transitionState = remember {
						SeekableTransitionState(EnterExitState.PostExit)
					}
					val transition = rememberTransition(transitionState)
					val animatedVisibilityScope =
						remember { AnimatedVisibilityScopeImpl(transition) }

					LaunchedEffect(firstAnimData.animationStatus) {
						snapshotFlow { firstAnimData.animationProgressForScope.value }
							.collectLatest() { animationProgress ->
								val animStatus = firstAnimData.animationStatus as AnimationStatus
								val targetState = animStatus.toEnterExitState()

								val targetProgess = calculateTargetProgress(
									targetState,
									animationProgress,
									animStatus
								)

								transitionState.seekTo(targetProgess, targetState)
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
								// to remove
								println("$child at indexFromTop: $indexFromTop, index: $index")

								scope.update(
									newIndex = index,
									newIndexFromTop = indexFromTop,
									animate = scope.indexFromTop != indexFromTop || indexFromTop < 1
								)

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

					with(animatedVisibilityScope) {
						if (displaying) holder.SaveableStateProvider(childHolderKey(child)) {
							Box(
								Modifier.zIndex((-indexFromTop).toFloat())
									.accumulate(animData.modifiers),
								content = {
									content(instance)
									BasicText(
										(firstAnimData.animationStatus as AnimationStatus).toEnterExitState()
											.toString() + "\n" + firstAnimData.animationStatus.toString(),
										color = { Color.White },
										modifier = Modifier.offset(y = 20.dp),
										style = TextStyle(
											textAlign = TextAlign.Center,
											fontSize = 8.sp
										)
									)
								}
							)
						}
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

private class AnimatedVisibilityScopeImpl(
	override val transition: Transition<EnterExitState>
) : AnimatedVisibilityScope

fun AnimationStatus.toEnterExitState() = when {
	fromOutsideIntoTop -> EnterExitState.Visible
	fromTopToOutside -> EnterExitState.PostExit

	fromTopIntoBack -> EnterExitState.PostExit
	fromBackIntoTop -> EnterExitState.Visible

	location.top && animationType.passiveCancelling -> EnterExitState.PostExit
	location.back && animationType.passiveCancelling -> EnterExitState.PostExit

	location.top && !animating -> EnterExitState.Visible
	(location.back && !animating) || fromBackToBack -> EnterExitState.PreEnter

	else -> EnterExitState.PreEnter
}

// Function to calculate progress
// animation progress:
// 1f = in back stack = 0f reported progress
// 0f = top of the stack/visible content = 1f reported progress
// -1f = out of the stack/hidden content = 0f reported progress
fun calculateTargetProgress(
	targetState: EnterExitState,
	animationProgress: Float,
	animStatus: AnimationStatus
) = when (targetState) {
	EnterExitState.PreEnter -> {
		when {
			animStatus.animationType.passiveCancelling && animStatus.location.top -> 1f - animationProgress
			else -> 1f
		}
//		error("this is unused. how did this happen")
	}

	EnterExitState.Visible -> when {
		animStatus.fromOutsideIntoTop -> 1f + animationProgress
		animStatus.fromBackIntoTop -> 1f - animationProgress
		animStatus.animationType.passiveCancelling && animStatus.location.top -> 1f - animationProgress
		animStatus.animationType.passiveCancelling && animStatus.location.back -> animationProgress
		!animStatus.animating && animStatus.location.top -> 1f
		!animStatus.animating && animStatus.location.back -> 0f
		else -> error("huh EnterExitState.Visible , $animStatus")
	}

	EnterExitState.PostExit -> when {
		animStatus.fromTopToOutside -> -animationProgress
		animStatus.fromTopIntoBack -> animationProgress
		animStatus.animationType.passiveCancelling && animStatus.location.top -> 1f + animationProgress
		animStatus.animationType.passiveCancelling && animStatus.location.back -> animationProgress
		!animStatus.animating || animStatus.fromBackToBack -> 0f
		else -> error("djhdndbnf")
	}
}.coerceIn(0f, 1f)

