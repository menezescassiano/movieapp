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
