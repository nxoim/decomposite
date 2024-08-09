---
title: NavigationRootData
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation](../index.html)/[NavigationRootData](index.html)/[NavigationRootData](-navigation-root-data.html)



# NavigationRootData



[common]\
constructor(defaultComponentContext: DefaultComponentContext = DefaultComponentContext(
		LifecycleRegistry(),
		StateKeeperDispatcher(savedState = null)
	), navStore: [NavControllerStore](../-nav-controller-store/index.html) = NavControllerStore(), viewModelStore: [ViewModelStore](../../com.nxoim.decomposite.core.common.viewModel/-view-model-store/index.html) = defaultComponentContext.instanceKeeper.getOrCreateSimple {
		ViewModelStore()
	})




