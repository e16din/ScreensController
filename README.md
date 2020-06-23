# @Deprecated

# ScreensController
ScreensController - MVC-framework to use only Services instead of Activities.

It is useful when you need to create one application for a device and prevent exit from the app.


## Init
```java
//Application onCreate
@Override
public void onCreate() {
    super.onCreate();

    ScreensController.set(new GeneratedScreensController(Application.this), new MainScreen(), null);
}
```

## Using

### Screen Model
```kotlin
class MainScreen : Screen() {
    init {
        layout = R.layout.screen_main
        theme = R.style.NoBarAppTheme
    }
}
```

### One Screen - One Controller
```kotlin
@ViewController(screen = MainScreen::class)
class MainController {
    @OnBind
    fun onBindView(sc: ScreensController, v: View, data: Any?) {
        // do something
    }
}
```

### One Controller - Many Screens
```kotlin
@ViewController(screen = EmployeeScreen::class)
@ViewController(screen = ConfirmScreen::class)
@ViewController(screen = CardRegistrationScreen::class)
@ViewController(screen = FingerRegistrationScreen::class)
class FingerScannerController {

    @OnBind
    fun onBindView(sc: ScreensController, view: View, data: Any?) {
        // do something
    }

    @OnHide
    fun onHide() {
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
