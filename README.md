[![Build Status](https://app.bitrise.io/app/f2af7ee280d7331e/status.svg?token=OSoznIWwGIU1j6J9DLyxng)](https://app.bitrise.io/app/f2af7ee280d7331e)
[![](https://jitpack.io/v/SumeraMartin/coreactor.svg)](https://jitpack.io/#SumeraMartin/coreactor)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Coreactor 

Coreactor is an MVI framework for Android applications with a focus on readability and simplicity.
  
## Why Coreactor?  

Coreactor provides a simple but powerful way of using MVI architecture with the full potential of Kotlin Coroutines. The library is built with the intention of readable code that can be easily understood by others.

The previous version of this library was using RxJava but implementation with coroutine allows the same functionality with more readable and easier to maintain code.

## Coreactor terminology 
**Coreactor** is the "manager" of the whole process of keeping and changing the state. It receives actions from the view and reacts to these actions by emitting reducers and events back to the view. 

**View** is responsible for rendering the state, handling events and for sending actions triggered by a user to the coreactor.

**State** is the base element of the architecture. It represents the state of the view that is immutable and can be changed by reducers.

**Action** is a command that is sent from the view to the coreactor that triggers operations (request data, change the state, observe global state ... )  and changes the state.
  
**Reducer** is a function send from the coreactor that changes the current state to the new one which is then dispatched back to the view.    
  
**Event** is a command that is sent from the coreactor to the view that should be used to perform stateless operations (show a toast, navigate to the next screen ...) 

## Setup
  
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```
dependencies {
    implementation 'com.github.sumeramartin:coreactor:0.0.1'
}
```
  
## Sample  
  
In the following sample, we will create a simple counter activity that will allow a user to increment or decrement the value of the counter and show the toast when the number will be divisible by 5.
  
### Step 1    
Create a state that will hold the counter value. 
```kotlin
data class CounterState(val counter: Int) : State
``` 
   
### Step 2    
Define actions that will be sent from the view to the coreactor when a user taps on the increment or the decrement button.
```kotlin 
object OnIncrementClicked : Action<CounterState>
object OnDecrementClicked : Action<CounterState>
```    

### Step 3    
Define reducers that will transform the previous state to the new state. In this case, reducers will either increment or decrement the current counter value by 1. 
``` kotlin
object IncrementReducer : Reducer<CounterState>() {
    override fun reduce(oldState: CounterState): CounterState { 
        return oldState.copy(counter = oldState.counter + 1) 
    }
}  
  
object DecrementReducer : Reducer<CounterState>() {
    override fun reduce(oldState: CounterState): CounterState { 
        return oldState.copy(counter = oldState.counter - 1) 
    }
}  
   
``` 
Reducers can be also implemented directly in the coreactor as lambda expressions. 
```kotlin
val incrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter + 1) }
val decrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter - 1) } 
``` 
Or can be directly emitted anonymously without the need for any specific object or class. 
```kotlin  
emitReducer { oldState -> oldState.copy(counter = oldState.counter + 1) }
emitReducer { oldState -> oldState.copy(counter = oldState.counter - 1) } 
```

### Step 4    
Define events that will be sent to the view to perform stateless operations. In this case, it will be an event that will show a toast with a message.
```kotlin 
data class ShowToast(val message: String) : Event<CounterState>() 
```

### Step 5    
Implements a coreactor that creates an initial state and reacts to actions sent from the view by emitting reducers and events. Coreactor implements CoroutineScope so coroutines can be used to perform asynchronous operations.  
```kotlin 
class CounterCoreactor : Coreactor<CounterState>() {

    override fun createInitialState(): CounterState {
        return CounterState(counter = 0)
    }
    
    override fun onAction(action: Action<CounterState>) {
        when (action) { 
            OnIncrementClicked -> { 
                emit(IncrementReducer) 
                if (currentState.counter % 5 == 0) { 
                    emit(ShowToast("Divisible by 5")) 
                } 
            } OnDecrementClicked -> { 
                emit(IncrementReducer) 
                if (currentState.counter % 5 == 0) { 
                    emit(ShowToast("Divisible by 5")) 
                }  
            } 
            SomeActionWhichRequirestheUsageOfCoroutines -> { 
                launch { 
                    ...
                    delay(1000) 
                    emit(ShowToast("Operation is finished")) 
                } 
            } 
        } 
    }
}  
```
  
### Step 6    
Since coreactor is based on Architecture Components and internally is implemented as ViewModel, it requires an implementation of the factory which is responsible for the instantiating of coreactor.
    
```kotlin
class CounterCoreactorFactory : CoreactorFactory<CounterCoreactor>() {

     override val coreactor = CounterCoreactor()
       
     override val coreactorClass = CounterCoreactor::class
}
```

### Step 7 
The last but not least step is to create a view responsible for rendering the state, handling the events and sending actions. In this case, it will be an Activity, but it can be also a Fragment. 
```kotlin  
class CounterActivity : CoreactorActivity<CounterState>(), CoreactorView<CounterState> {

    override fun layoutRes() = R.layout.activity_counter

    override val coreactorFactory = CounterCoreactorFactory()
    
    override val coreactorView = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    
        counterView_incrementButton.setOnClickListener {
            sendAction(OnIncrementClicked)
        }

        counterView_decrementButton.setOnClickListener {
            sendAction(OnDecrementClicked)
        }
    }

    override fun onState(state: CounterState) {
        counterView_counterValue.text = state.counter
    }

    override fun onEvent(event: Event<CounterState>) {
        when (event) {
            is ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

## Advanced usage  

### Wait until methods
`waitUntil...` methods suspend current coroutine and then will wait until the given condition is fulfilled or the expected object is received. They are implemented for actions, reducers, events, states, and lifecycle states and should be used for more complex flows that require multiple consequent actions.
```kotlin
OnDeleteItemClickedAction -> launch {

    emit { ShowDeletePromptReducer }
    
    val result = waitUntilAction { DeletePromptResultAction } // Suspends current coroutine until DeletePromptResultAction is sent from the view
    
    if (result.isConfirmed) {
        deleteItem()
        emit { RemoveItemFromTheList }
    }  
    
    emit { HideDeletePromptReducer }
  }
```

### Scoped methods
Launches and runs the given suspend block when the coreactor will be at least in the expected state. When the coreactor won't be in the given state then the block will be executed once when the coreactor will get into the expected state. The Job will be canceled when the coreactor will leave the expected state (launchWhenStarted will be canceled when the coreactor will get into stopped state ...). These methods are recommended to be used for observing the global state.
```kotlin
launchWhenCreated {
    ...
}
launchWhenStarted {
    ...
}
launchWhenResumed {
    ...
}
```
### Logger
Coreactor implements a logger that can be easily customized and can be used out of the box for easy debugging of the implementation.

Simple console logger logs only the basic information (reducer emitted, event emitted, action send, a new state created)
```kotlin
override val logger = SimpleConsoleLogger<CustomState>(tag = "TAG")
```

Detailed console logger logs more detailed information as a simple logger (the event is waiting for the started or the created state, the event is thrown away, a new state is dispatched to view, lifecycle state is changed)
```kotlin
override val logger = DetailedConsoleLogger<CustomState>(tag = "TAG")
```

The custom logger can be implemented with `CoreactorLogger` interface. 

### Interceptor
Coreactor allows intercepting of events, reducers, states, and actions. These received objects then can be adjusted or can be omitted by returning a `null` value.
```kotlin
override val interceptor = CustomInterceptor<CustomState>()
```
The custom interceptor can be implemented with `CoreactorInterceptor` interface. 

### Channels 
All actions, reducers, events, states, and lifecycle states are sent to their own `BroadcastChannel` that can be accessed and used in the custom coreactor. Actions, events, and reducers channels are implemented as `ArrayBroadcastChannel(1)` so they will emit only the newly received objects and lifecycle states and states are implemented as `ConflatedBroadcastChannel` so they will emit the most recent item also.
```kotlin
launch {
    openStateSubscription().consumeAsFlow().collect { state ->
        // Do something
    }
}
```

### Open methods
Coreactor methods `onLifecycleState` or `onState` can be overridden to receive changes in the lifecycle state and in the current state. 
