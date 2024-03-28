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

# Examples/Getting Started
In your version catalog add the "com.github.nxoim.Decomposite:decomposite:preferredVersion" artifact. In your toml file that would be:
```
decomposite = { group = "com.github.nxoim.decomposite", name = "decomposite", version.ref = "version" }
```

First you have to set up the app by creating a root of the app. This root sets up stores for view models and nav controllers, overlay stuff, and provides the root component context.

On Android:
```kotlin
class YourActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    	super.onCreate(savedInstanceState)

        // default component context is included with the decompose library
        val navigationRootData = navigationRootData(defaultComponentContext())

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
// creating an instance
val yourNavController = navController<YourDestinations>(startingDestination = YourDestinations.Star)

Scaffold(bottomBar = { GlobalSampleNavBar() }) { scaffoldPadding ->
    NavHost(
        yourNavController,
        Modifier.padding(scaffoldPadding),        
        animations = {
            when (currentChild) {
                RootDestinations.Star -> fade() + scale()
                else -> cleanSlideAndFade()
            }
        }
    ) {
        when (it) { // nested hosts!
            RootDestinations.Star -> StarNavHost()
            RootDestinations.Heart -> HeartNavHost()
        }
    }    
}
```

Navigation controller usage:
```kotlin
// getting the controller instance from the store
val navController = getExistingNavController<YourDestinations>()

// in any clickable
navController.navigate(YourDestinations.Heart)

// or if you want to display a destination as an overlay
navController.navigate(YourDestinations.Star, ContentType.Overlay)

// navigate back
navController.navigateBack()
```

You can open snack content, even in a coroutine:
```kotlin
snackController().display("some key", duration = 5L.seconds) {
    YourSnackbarComposable()
}
```

View model creation and usage:
```kotlin
@Composable
fun YourScreen() {
    // get or create a view model
    val vm = viewModel("optional key") { SomeViewModel(someArgument = "some text") }

    // just get a view model. 
    // this does not create a view model due to reflection practically
    // not existing in the wasm target as of yet
    val vm = getExistingViewModel<SomeViewModel>("optionalKey")
}

class SomeViewModel(someArgument: String) : ViewModel() {
    // you can retain the view model until the app gets destroyed by overriding 
    // onDestroy and leaving it empty
    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
        // maybe still cancel the scope? maybe
        viewModelScope.coroutineContext.cancelChildren()
    }
}
```

Back gestures on other platforms:
```kotlin
// this is for jvm
fun main() = application {
    // initialize these at some root
    val backDispatcher = BackDispatcher()
    val navigationRootData = navigationRootDataProvider(
        DefaultComponentContext(LifecycleRegistry(), backHandler = backDispatcher)
    )

    Window(
        title = "Decomposite",
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)

        SampleTheme {
            // first wrap your app in a theme.
            // because of material 3 quirks - surface wraps the root to fix text
            // colors in overlays.
            Surface {
                // also since you need to initialize the component context of the app
                // on your preferred platform anyway - it's ok to add decomposite to
                // your entry-point/app module of the project, or combine it with your
                // navigation module

                // then initialize the back gesture overlay that will handle the back gestures.
                // initialize it first, put NavigationRoot inside it, else overlays will not 
                // detect the gestures
                PredictiveBackGestureOverlay(
                    backDispatcher,
                    backIcon = { _, _ -> }, // no back icon, we handle that on per-screen basis
                    endEdgeEnabled = false, // disable swipes from the right side,
                    content = { NavigationRoot(navigationRootData) { App() } }
                )
            }
        }
    }
}
```
