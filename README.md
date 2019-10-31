
  
# Coreactor  
Coreactor is a bidirectional architectural pattern for Android applications based on Kotlin coroutines API.  
  
## Dictionary  
  
**Coreactor** is the "manager" of the whole process of keeping and changing the state. It receives actions from the view and reacts to these actions by emitting reducers and events back to the view. 
  
**View** is responsible for rendering the state, handling events and for sending actions triggered by a user to the coreactor.  
  
**State** is the basic element of the architecture. It represents the state of the view.
  
**Action** is a message that is sent from the view to the coreactor which triggers desired effects (request data, change the state, observe global state ... )  
  
**Reducer** is a function send from the coreactor which changes the current state to the new one which is then dispatched back to the view.  
  
**Event** a message that is sent from the coreactor to the view which should be used to trigger stateless effects (show a toast, navigate to the next screen ... ) 
  
## Setup

TODO

## Sample

In the following sample, we will create a basic counter activity that will allow a user to increment or decrement value of the counter and show the toast when the number will be divisible by 5.

### Step 1  
  
Create a state that contains the counter value.
```  
data class CounterState(val counter: Int) : State  
```  
  
### Step 2  
  
Define actions that will be sent from the view to the coreactor when the user taps on the increment or the decrement button.
```  
object OnIncrementClicked : Action<CounterState>  
  
object OnDecrementClicked : Action<CounterState>  
```  
  
### Step 3  
  
Define reducers that will transform the previous state of the view to the new state which will be rendered by the view. In this case, reducers will either increment or decrement the current state.
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
  
Reducers can be also implemented directly in the coreactor as lambda expressions. 
```  
val incrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter + 1) }  
  
val decrementReducer = actionReducer { oldState -> oldState.copy(counter = oldState.counter - 1) }  
```  
  
### Step 4  
  
Define events that will be sent to the view to perform stateless operations. In this case, it will be the event that will show the toast with a message.
```  
data class ShowToast(val message: String) : Event<CounterState>()  
```  
  
### Step 5  
  
Create a coreactor that creates an initial state and reacts to actions sent from view by emitting reducers and events. Coreactor implements CoroutineScope so coroutines can be used to perform asynchronous operations.
```  
class CounterCoreactor : Coreactor<CounterState>() {
    
    override fun createInitialState(): CounterState {
        return CounterState(counter = 0)
    }
    
    override fun onAction(action: Action<CounterState>) = coreactorFlow {
        when (action) {
            OnIncrementClicked -> {
                emit(IncrementReducer)
                if (currentState.counter % 5 == 0) {
                    emit(ShowToast("Divisible by 5"))
                }
            }
            OnDecrementClicked -> {
                emit(DecrementReducer)
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
  
Since coreactor is based on Architecture Components and internally is implemented as ViewModel, it requires an implementation of the factory which is responsible to instantiate ViewModels.
  
```
class CounterCoreactorFactory : CoreactorFactory<CounterCoreactor>() {
    
    override val coreactor = CounterCoreactor()
    
    override val coreactorClass = CounterCoreactor::class
}  
```
  
### Step 7   
The last but not least step is to create a view responsible for rendering the state and handling the events. In this case, it will be an Activity but it can be also a Fragment or some custom implementation of the view.  
  
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