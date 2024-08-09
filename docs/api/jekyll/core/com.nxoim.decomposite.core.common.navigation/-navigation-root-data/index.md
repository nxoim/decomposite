---
title: NavigationRootData
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation](../index.html)/[NavigationRootData](index.html)



# NavigationRootData



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



data class [NavigationRootData](index.html)(val defaultComponentContext: DefaultComponentContext = DefaultComponentContext(
		LifecycleRegistry(),
		StateKeeperDispatcher(savedState = null)
	), val navStore: [NavControllerStore](../-nav-controller-store/index.html) = NavControllerStore(), val viewModelStore: [ViewModelStore](../../com.nxoim.decomposite.core.common.viewModel/-view-model-store/index.html) = defaultComponentContext.instanceKeeper.getOrCreateSimple {
		ViewModelStore()
	})

Creates the root of the app for back gesture handling, storing view models, and navigation controller instances. View model store by default is wrapped into default component context's instance keeper.



Initialize this outside of setContent.



## Constructors


| | |
|---|---|
| [NavigationRootData](-navigation-root-data.html) | [common]<br>constructor(defaultComponentContext: DefaultComponentContext = DefaultComponentContext( 		LifecycleRegistry(), 		StateKeeperDispatcher(savedState = null) 	), navStore: [NavControllerStore](../-nav-controller-store/index.html) = NavControllerStore(), viewModelStore: [ViewModelStore](../../com.nxoim.decomposite.core.common.viewModel/-view-model-store/index.html) = defaultComponentContext.instanceKeeper.getOrCreateSimple { 		ViewModelStore() 	}) |


## Properties


| Name | Summary |
|---|---|
| [defaultComponentContext](default-component-context.html) | [common]<br>val [defaultComponentContext](default-component-context.html): DefaultComponentContext |
| [navStore](nav-store.html) | [common]<br>val [navStore](nav-store.html): [NavControllerStore](../-nav-controller-store/index.html) |
| [viewModelStore](view-model-store.html) | [common]<br>val [viewModelStore](view-model-store.html): [ViewModelStore](../../com.nxoim.decomposite.core.common.viewModel/-view-model-store/index.html) |

