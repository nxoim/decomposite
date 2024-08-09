[![](https://jitpack.io/v/nxoim/decomposite.svg)](https://jitpack.io/#nxoim/decomposite)

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-wasm](https://img.shields.io/badge/Platform-wasm-purple)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)(?)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)(?)
(i dont have apple's devices)

# What?
Router style navigation library with Decompose used as a base with some features on top, like view model store, overlays, custom extensions like animations, etc.

# Why?
There was existing multiplatform tooling, but it was kinda raw for my taste, so I started learning and experimenting by making DSLs based on it and this is the result.

# ... What was the thought process?
Uhm...

# Here's what it has and can do
- Convenient type-safe router-style navigation
- Custom animation system (inspired by Decompose)
- Properly store view models, with configuration change handling, view model scope cancellation and allat
- Convenient view model instance creation
- Display destinations in overlays
- Pass the backstack entry's component context using CompositionLocalProvider
- Pass the type (contained/overlay) of the displayed content also using CompositionLocalProvider 
- Store navigation controller instances like view models
- Automatically create navigation controller instances upon the creation of nav hosts that are retrievable by just calling navController, again, kind of like view models

# Examples/Getting Started
In your version catalog add the "com.github.nxoim.decomposite:decomposite" artifact. In your toml file that would be:
```
decomposite = { module = "com.github.nxoim.decomposite:decomposite", version.ref = "version" }
```

First you have to set up the app by creating a root of the app. This root sets up stores for view models and nav controllers, overlay stuff, and provides the root component context.

On Android:
```kotlin
class YourActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    	super.onCreate(savedInstanceState)

        // default component context is included with the decompose library
        val navigationRootData = defaultNavigationRootData()

        setContent {
            NavigationRootProvider(navigationRootData) { YourContent() }
        }
    }
}
```

Check out [the android sample](https://github.com/nxoim/decomposite/blob/update/sample/app/src/androidMain/kotlin/com/nxoim/decomposite/App.android.kt) if you want predictive gesture animations application-wide and on older androids. 

On everything else:
```kotlin
// outside compose
val navigationRootData = defaultNavigationRootData()

// ...

// inside any composable at the root
NavigationRootProvider(navigationRootData) { YourContent() }
```

Navigation host creation:
```kotlin
// creating an instance
val yourNavController = navController<YourDestinations>(startingDestination = YourDestinations.Star)

Scaffold(
    bottomBar = { 
		GlobalSampleNavBar(onBack = { yourNavController.navigateBack() }) 
	}
) { scaffoldPadding ->
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
// in any clickable
yourNavController.navigate(YourDestinations.Heart)

// navigate back
yourNavController.navigateBack()
```

View model creation and usage:
```kotlin
@Composable
fun YourScreen() {
    // get or create a view model
    val vm = viewModel("optional key") { SomeViewModel(someArgument = "some text") }

    // just get a view model. 
    val vm = getExistingViewModel<SomeViewModel>("optional key")
}

class SomeViewModel(someArgument: String) : ViewModel() {
    // you can retain the view model until the app gets destroyed by overriding 
    // onDestroy and not calling removeFromViewModelStore
    override fun onDestroy(removeFromViewModelStore: () -> Unit) {
        // maybe still cancel the scope? maybe
        viewModelScope.coroutineContext.cancelChildren()
    }
}
```

Back gestures on other platforms:
```kotlin
// this is for jvm
@OptIn(ExperimentalDecomposeApi::class)
fun main() = application {
        // initialize this at the root of your app
        val navigationRootData = defaultNavigationRootData()

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
                    BackGestureProviderContainer(
                        navigationRootData.defaultComponentContext,
                        content = { NavigationRootProvider(navigationRootData) { App() } }
                    )
                }
            }
        }
    }
```

Or you can apply a modifier to the content you want to handle the back gestures, like:
```kotlin
ExampleComposable(Modifier.backGestureProvider(LocalBackDispatcher.current))
```
