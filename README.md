# ScreensController
Android MVC framework

## Init
```java
//Application onCreate
@Override
public void onCreate() {
    super.onCreate();

    ScreensController.set(new GeneratedScreensController(), new MainScreen(), null);
}
```

## Using

### M[Screen] V[Activity] C[ScreensController]
```kotlin
class MainScreen : Screen() {
    init {
        layout = R.layout.screen_main
        theme = R.style.NoBarAppTheme
    }
}
```

### M[Any] V[View] C[ViewController]
```kotlin
@ViewController(screen = MainScreen::class)
class GameController {
    @OnBind
    fun onBindView(sc: ScreensController, v: View, data: Any?) {
        // do something
    }
}
```

## Download

```groovy
dependencies {
    implementation project(":sc")
    kapt project(":sc-processor")
}
```