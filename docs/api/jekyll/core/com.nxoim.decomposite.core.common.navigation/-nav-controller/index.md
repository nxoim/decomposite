---
title: NavController
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation](../index.html)/[NavController](index.html)



# NavController



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [NavController](index.html)&lt;[C](index.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;(startingDestination: [C](index.html), serializer: KSerializer&lt;[C](index.html)&gt;? = null, val parentComponentContext: ComponentContext, val key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), childFactory: (config: [C](index.html), childComponentContext: ComponentContext) -&gt; [DecomposeChildInstance](../-decompose-child-instance/index.html) = { _, childComponentContext -&gt;
		DefaultChildInstance(childComponentContext)
	})

Generic navigation controller. Contains a stack for overlays and a stack for screens.



## Constructors


| | |
|---|---|
| [NavController](-nav-controller.html) | [common]<br>constructor(startingDestination: [C](index.html), serializer: KSerializer&lt;[C](index.html)&gt;? = null, parentComponentContext: ComponentContext, key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), childFactory: (config: [C](index.html), childComponentContext: ComponentContext) -&gt; [DecomposeChildInstance](../-decompose-child-instance/index.html) = { _, childComponentContext -&gt; 		DefaultChildInstance(childComponentContext) 	}) |


## Properties


| Name | Summary |
|---|---|
| [controller](controller.html) | [common]<br>val [controller](controller.html): StackNavigation&lt;[C](index.html)&gt; |
| [currentScreen](current-screen.html) | [common]<br>val [currentScreen](current-screen.html): [C](index.html) |
| [key](key.html) | [common]<br>val [key](key.html): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [parentComponentContext](parent-component-context.html) | [common]<br>val [parentComponentContext](parent-component-context.html): ComponentContext |
| [screenStack](screen-stack.html) | [common]<br>val [screenStack](screen-stack.html): Value&lt;ChildStack&lt;[C](index.html), [DecomposeChildInstance](../-decompose-child-instance/index.html)&gt;&gt; |


## Functions


| Name | Summary |
|---|---|
| [close](close.html) | [common]<br>fun [close](close.html)(destination: [C](index.html), onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Removes a destination. |
| [navigate](navigate.html) | [common]<br>fun [navigate](navigate.html)(destination: [C](index.html), removeIfIsPreceding: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = true, onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Navigates to a destination. If a destination exists already - moves it to the top instead of adding a new entry. If the [removeIfIsPreceding](navigate.html) is enabled (is by default) and the requested [destination](navigate.html) precedes the current one in the stack - navigate back instead. |
| [navigateBack](navigate-back.html) | [common]<br>fun [navigateBack](navigate-back.html)(onComplete: ([Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Navigates back in this(!) nav controller. |
| [navigateBackTo](navigate-back-to.html) | [common]<br>fun [navigateBackTo](navigate-back-to.html)(destination: [C](index.html), onComplete: ([Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Removes destinations that, in the stack, are after the provided one. |
| [replaceAll](replace-all.html) | [common]<br>fun [replaceAll](replace-all.html)(vararg destination: [C](index.html), onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Replaces all destinations with the provided one. |
| [replaceCurrent](replace-current.html) | [common]<br>fun [replaceCurrent](replace-current.html)(withDestination: [C](index.html), onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) = { })<br>Replaces the current destination with the provided one. |

