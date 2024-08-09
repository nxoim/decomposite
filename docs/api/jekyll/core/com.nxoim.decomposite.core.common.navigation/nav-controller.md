---
title: navController
---
//[core](../../index.html)/[com.nxoim.decomposite.core.common.navigation](index.html)/[navController](nav-controller.html)



# navController



[common]\




@[Composable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable.html)



inline fun &lt;[C](nav-controller.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [navController](nav-controller.html)(startingDestination: [C](nav-controller.html), serializer: KSerializer&lt;[C](nav-controller.html)&gt;? = serializer(), navStore: [NavControllerStore](-nav-controller-store/index.html) = LocalNavControllerStore.current, componentContext: ComponentContext = LocalComponentContext.current, key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) = navControllerKey&lt;C&gt;(), noinline childFactory: (config: [C](nav-controller.html), childComponentContext: ComponentContext) -&gt; [DecomposeChildInstance](-decompose-child-instance/index.html) = { _, childComponentContext -&gt;
		DefaultChildInstance(childComponentContext)
	}): [NavController](-nav-controller/index.html)&lt;[C](nav-controller.html)&gt;



Creates a navigation controller instance in the [NavControllerStore](-nav-controller-store/index.html), which allows for sharing the same instance between multiple calls of [navController](nav-controller.html).



Is basically a decompose component that replicates the functionality of a generic navigation controller. The instance is not retained, therefore on configuration changes components will die and get recreated. By default inherits parent's ComponentContext.



[childFactory](nav-controller.html) allows for creating custom children instances that implement [DecomposeChildInstance](-decompose-child-instance/index.html).



[key](nav-controller.html) is used for identifying childStack's during serialization and instances in [NavControllerStore](-nav-controller-store/index.html), which means keys MUST be unique.



On death removes itself from the [NavControllerStore](-nav-controller-store/index.html) right after the composition's death.




