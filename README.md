# What?
Router style navigation library with Decompose used as a base with some features on top, like view model store, overlays, snack bars, custom extensions like animations, etc.

# Why?
There was existing multiplatform tooling, but it was kinda raw for my taste, so I started learning and experimenting by making DSLs based on it and this is the result.

# ... What was the thought process?
Uhm...

# Here's what it has and can do
- Convenient type-safe router-style navigation
- Custom animation system (inspired by Decompose)
- Properly store view models, with configuration change handling, view model scope cancellation and allat
- Convenient view model instance creation
- Display destinations as overlays 
- Display snackbars 
- Pass the backstack entry's component context using CompositionLocalProvider
- Pass the type (contained/overlay) of the displayed content also using CompositionLocalProvider 
- Store navigation controller instances like view models
- Automatically create navigation controller instances upon the creation of nav hosts that are retrievable by just calling navController, again, kind of like view models

# Optional Extensions
There a few optional Extensions. For now they're all custom animations.

# Examples/Getting Started
First you have to set up the app by creating a root of the app. This root sets up stores for view models and nav controllers, overlay stuff, and provides the root component context.

On Android:
```kotlin
class YourActivity : ComponentActivity() {
    // default component context is included with the decompose library
    val navigationRootData = navigationRootData(defaultComponentContext())
    // you can also instantiate it using di. with Koin, for example, you'd do:
    // yourKoin.loadModules(listOf(module { single { navigationRootData } })) 
    
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationRoot(navigationRootData) { YourContent() }
        }
    }
}
```

On everything else:
```kotlin
// inside any composable at the root
NavigationRoot(navigationRootData()) { YourContent() }
```

Navigation host creation:
```kotlin
NavHost<YourDestinations>(
    startingDestination = YourDestinations.Star,
    defaultAnimation = cleanFadeAndSlide(),
    routedContent = { // content that isn't in an overlay
        Scaffold(bottomBar = { GlobalSampleNavBar() }) { scaffoldPadding ->
            it(Modifier.padding(scaffoldPadding))
        }
    }
) {
    when (it) { // nested hosts!
        RootDestinations.Star -> animatedDestination(fade() + scale()) { StarNavHost() }
        RootDestinations.Heart -> animatedDestination { HeartNavHost() }
    }
}
```

Or:
```kotlin
NavHost<YourDestinations>(
    startingDestination = YourDestinations.Star,
    animations = {
        when (it) { 
        RootDestinations.Star -> fade() + scale()
        else -> fade()
    },
    routedContent = { // content that isn't in an overlay
        Scaffold(bottomBar = { GlobalSampleNavBar() }) { scaffoldPadding ->
            it(Modifier.padding(scaffoldPadding))
        }
    }
) {
    when (it) { // nested hosts!
        RootDestinations.Star -> StarNavHost()
        RootDestinations.Heart -> HeartNavHost()
    }
}
```

Or you can create a navigation controller manually:
```kotlin
    val navController = navController<YourDestinations>(startingDestination = YourDestinations.Star)
    
    NavHost<YourDestinations>(
        startingNavControllerInstance = navController,
        ...
    )
```

Navigation controller usage:
```kotlin
// getting the controller instances from the store. It's remembered by default
val navController = navController<YourDestinations>()

// in any clickable
navController.navigate(YourDestinations.Heart)

// or if you want to display a destination as an overlay
navController.navigate(YourDestinations.Star, ContentType.Overlay)

// you can open snack content
navController.openInSnack("some key", duration = 5L.seconds) {
    YourSnackbarComposable()
}

// navigate back
navController.navigateBack()
```

View model creation and usage:
```kotlin
@Composable
fun YourScreen() {
    // get or create a view model
    val vm = viewModel("optional key") { SomeViewModel(someArgument = "some text") }

	// just get a view model. 
	// this does not create a view model
	// due to reflection practically
	// not existing in the wasm target as of yet
	val vm = viewModel<SomeViewModel>("optionalKey")
}

class SomeViewModel(someArgument: String) : ViewModel() {
    // you can retain the view model until 
    //the app gets destroyed by overriding 
    // onDestroy and leaving it empty
    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
    // maybe still cancel the scope? maybe
    viewModelScope.coroutineContext.cancelChildren()
    }
}
```