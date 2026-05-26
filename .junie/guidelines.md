# Project Guidelines

## Unit Tests

Whenever writing unit tests, always follow these rules **without waiting to be asked**:

### 1. Coverage checklist per ViewModel
For every public function and every state exposed by the ViewModel, ensure there is a test for:
- Initial state
- Happy path (success with data)
- Happy path (success with empty/null data, when applicable)
- Error path (exception sets errorMessage, clears isLoading)
- Success after error clears errorMessage
- Error does not clear previously loaded data
- Multiple sequential calls behave correctly (data is replaced, not accumulated)
- Consecutive toggles return to original state (when applicable)
- Action called without data loaded (when applicable)
- Optimistic update revert on failure (when applicable)

### 2. Remove redundant tests
A test is redundant if:
- It only tests the behavior of the language/stdlib and not the ViewModel logic
- It is a strict subset of another test in the same file (all assertions are already covered elsewhere)

### 3. Prove bugs with failing tests before fixing
If a test reveals a bug in the ViewModel (e.g. errorMessage not cleared on success):
- First write the test asserting the **correct** behavior (so it fails)
- Run the tests to confirm the failure
- Only then fix the ViewModel
- Run the tests again to confirm they pass

### 4. Code style
- Follow the same structure and naming conventions as `DetailsViewModelTest.kt`
- Group tests with comments: `// -- <section> ---`
- Use `UnconfinedTestDispatcher` + `advanceUntilIdle()`
- Always call `Dispatchers.setMain` in `@Before` and `Dispatchers.resetMain` in `@After`
- Use `mockk()` for dependencies and `coEvery` for suspend functions

## Test IDs

Whenever test IDs are requested for a screen, follow the same pattern already established for `LoginScreen`:

### 1. Create a dedicated `<Screen>TestIds` object
Create a file named `<Screen>TestIds.kt` co-located with the screen (same package/folder), containing a Kotlin `object` with `const val` string constants:

```kotlin
object LoginTestIds {
    const val EMAIL_FIELD = "login_email_field"
    const val PASSWORD_FIELD = "login_password_field"
    const val CONTINUE_BUTTON = "login_continue_button"
}
```

- Name constants in `UPPER_SNAKE_CASE`.
- Value strings follow the pattern `<screen_name>_<element_description>` (all lowercase, underscore-separated).

### 2. Apply `Modifier.testId(...)` on each tagged element
Import and apply the `testId` extension from `com.example.movieapp.core.ui`:

```kotlin
CustomTextField(
    ...
    testId = LoginTestIds.EMAIL_FIELD,
)

CustomButton(
    ...
    modifier = Modifier.fillMaxWidth().testId(LoginTestIds.CONTINUE_BUTTON),
)
```

### 3. Enable test ID propagation on the root container
Add `.enableTestIds()` (from `com.example.movieapp.core.ui`) to the outermost container of the screen so that every `testTag` is exposed as `resource-id` for Appium/UIAutomator2:

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(AppBackground)
        .enableTestIds(),
) { ... }
```
