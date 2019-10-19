
# Coreactor
Coreactor is bidirectional architectural pattern for Android application based on Kotlin coroutines flow api.


## Dictionary

**Coreactor**  is the "manager" of the whole process of keeping and changing the state. It receives actions from the view and reacts to these actions by emitting reducers and events back to the view. 

**View** is responsible for rendering the state, handling events and for sending actions triggered by user to the coreactor.

**State** is the basic element of the architecture. Represents the local state of the view.

**Action** is a message that is send from the view to the coreactor which triggers desired effects (request data, change the state, observe global state ... )

**Reducer** is a function send from the coreactor which changes the state which is then propagated to the view.

**Event** a message that is send from the coreactor to the view which should be used to trigger stateless effects (show a toast, navigate to the next screen ... )


### Step 1

Create a state
```
data class CounterState(val counter: Int) : State
```

### Step 2

Define actions which are send from the view to the coreactor
```
object OnIncrementClicked : Action<CounterState>

object OnDecrementClicked : Action<CounterState>
```

### Step 3

Define reducers which will transform the previous state of view to the new state which will be rendered by the view
```
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

or you can use action reducers right in the coreactor implementation

```
val incrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter + 1) }

val decrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter - 1) }
```

### Step 4

Define events (actions received by the view which doesn't change the state: toast, navigation ...) that will be send to the view
```
data class ShowToast(val message: String) : Event<CounterState>()
```

### Step 5

Create a coreactor which create an initial state and transforms view actions to reducers and events

```
class CounterCoreactor : Coreactor<CounterState>() {  
  
    override fun createInitialState(): CounterState {  
        return CounterState(counter = 0)  
    }  
  
    override fun onAction(action: Action<CounterState>) = coreactorFlow {  
        when (action) {  
            OnIncrementClicked -> {  
                emit(IncrementReducer)  
                emit(ShowToast("Counter incremented"))  
            }  
            OnDecrementClicked -> {  
                emit(DecrementReducer)  
                emit(ShowToast("Counter decremented"))  
            }  
        }  
    }  
}
```

### Step 6

Since coreactor is based on Architecture Components and internally is implemented as ViewModel, it requires an implementation of factory which is responsible to instantiate ViewModels.

```
class CounterCoreactorFactory : CoreactorFactory<CounterCoreactor>() {  
  
    override val coreactor = CounterCoreactor()  
  
    override val coreactorClass = CounterCoreactor::class  
}
```

### 7 

The last but not least step is to create a view responsible for rendering the state and handling the events. In this case it will be an Activity, but it can be a fragment or a view.

```
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
