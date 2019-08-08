package com.crazylegend.kotlinextensions.livedata

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.crazylegend.kotlinextensions.database.DBResult
import com.crazylegend.kotlinextensions.database.getSuccess
import com.crazylegend.kotlinextensions.reflection.firstPropertyValue
import com.crazylegend.kotlinextensions.retrofit.RetrofitResult
import com.crazylegend.kotlinextensions.retrofit.getSuccess


/**
 * Created by Hristijan on 2/1/19 to long live and prosper !
 */

//app compat activity
inline fun <reified T : ViewModel> AppCompatActivity.compatProvider(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T : ViewModel> AppCompatActivity.compatProviderVMF(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory).get(T::class.java)
}

inline fun <reified T : AndroidViewModel> AppCompatActivity.compatProvider(factory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, factory).get(T::class.java)
}


//fragment
inline fun <reified T : ViewModel> Fragment.fragmentProvider(): T {

    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.fragmentProviderVMF(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(this, factory).get(T::class.java)
}

inline fun <reified T : AndroidViewModel> Fragment.fragmentProvider(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(this, factory).get(T::class.java)
}

//fragment activity
inline fun <reified T : ViewModel> FragmentActivity.fragmentActivityProvider(): T {

    return ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.fragmentActivityProviderVMF(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(this, factory).get(T::class.java)
}

inline fun <reified T : AndroidViewModel> FragmentActivity.fragmentActivityProvider(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(this, factory).get(T::class.java)
}


//shared model
inline fun <reified T : ViewModel> Fragment.sharedProvider(): T {

    return ViewModelProviders.of(requireActivity()).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.sharedProviderVMF(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(requireActivity(), factory).get(T::class.java)
}

inline fun <reified T : AndroidViewModel> Fragment.sharedProvider(factory: ViewModelProvider.Factory): T {

    return ViewModelProviders.of(requireActivity(), factory).get(T::class.java)
}


/**
 * Converts a LiveData to a SingleLiveData (exactly similar to LiveData.first()
 */
fun <T> LiveData<T>.toSingleLiveData(): SingleLiveData<T> = first()

/**
 * Converts a LiveData to a MutableLiveData with the initial value set by this LiveData's value
 */
fun <T> LiveData<T>.toMutableLiveData(): MutableLiveData<T>  = MutableLiveData<T>(value)


/**
 * Creates a LiveData that emits the initialValue immediately.
 */
fun <T> liveDataOf(initialValue: T): MutableLiveData<T> {
    return emptyLiveData<T>().apply { value = initialValue }
}

/**
 * Creates a LiveData that emits the value that the `callable` function produces, immediately.
 */
fun <T> liveDataOf(callable: () -> T): LiveData<T> {
    val returnedLiveData = MutableLiveData<T>()
    returnedLiveData.value = callable.invoke()
    return returnedLiveData
}

/**
 * Creates an empty LiveData.
 */
fun <T> emptyLiveData(): MutableLiveData<T> {
    return MutableLiveData()
}

/**
 * Emits at most 1 item and returns a SingleLiveData
 */
fun <T> LiveData<T>.first(): SingleLiveData<T> {
    return SingleLiveData(this)
}


/**
 * Emits the first n valueus
 */
fun <T> LiveData<T>.take(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var takenCount = 0
    mutableLiveData.addSource(this) {
        if (takenCount < count) {
            mutableLiveData.value = it
            takenCount++
        } else {
            mutableLiveData.removeSource(this)
        }
    }
    return mutableLiveData
}

/**
 * Takes until a certain predicate is met, and does not emit anything after that, whatever the value.
 */
inline fun <T> LiveData<T>.takeUntil(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var metPredicate = predicate(value)
    mutableLiveData.addSource(this) {
        if (predicate(it)) metPredicate = true
        if (!metPredicate) {
            mutableLiveData.value = it
        } else {
            mutableLiveData.removeSource(this)
        }
    }
    return mutableLiveData
}


/**
 * Skips the first n values
 */
fun <T> LiveData<T>.skip(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var skippedCount = 0
    mutableLiveData.addSource(this) {
        if (skippedCount >= count) {
            mutableLiveData.value = it
        }
        skippedCount++
    }
    return mutableLiveData
}


/**
 * Skips all values until a certain predicate is met (the item that actives the predicate is also emitted)
 */
inline fun <T> LiveData<T>.skipUntil(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var metPredicate = false
    mutableLiveData.addSource(this) {
        if (metPredicate || predicate(it)) {
            metPredicate = true
            mutableLiveData.value = it
        }
    }
    return mutableLiveData
}

/**
 * Emits only the values that are not null
 */
fun <T> LiveData<T>.nonNullValues(): NonNullLiveData<T> {
    return NonNullLiveData(this)
}


/**
 * Emits the default value when a null value is emitted
 */
fun <T> LiveData<T>.defaultIfNull(default: T): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) {
        mutableLiveData.value = it ?: default
    }
    return mutableLiveData
}

/**
 * Emits the items that are different from all the values that have been emitted so far
 */
fun <T> LiveData<T>.distinct(): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    val dispatchedValues = mutableListOf<T?>()
    mutableLiveData.addSource(this) {
        if (!dispatchedValues.contains(it)) {
            mutableLiveData.value = it
            dispatchedValues.add(it)
        }
    }
    return mutableLiveData
}


/**
 * Emits the items that are different from the last item
 */
fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var latestValue: T? = null
    mutableLiveData.addSource(this) {
        if (latestValue != it) {
            mutableLiveData.value = it
            latestValue = it
        }
    }
    return mutableLiveData
}

/**
 * Merges this LiveData with another one, and emits any item that was emitted by any of them
 */
fun <T> LiveData<T>.mergeWith(vararg liveDatas: LiveData<T>): LiveData<T> {
    val mergeWithArray = mutableListOf<LiveData<T>>()
    mergeWithArray.add(this)
    mergeWithArray.addAll(liveDatas)
    return merge(mergeWithArray)
}

/**
 * Merges multiple LiveData, and emits any item that was emitted by any of them
 */
fun <T> merge(liveDataList: List<LiveData<T>>): LiveData<T> {
    val finalLiveData: MediatorLiveData<T> = MediatorLiveData()
    liveDataList.forEach { liveData ->

        liveData.value?.let {
            finalLiveData.value = it
        }

        finalLiveData.addSource(liveData) { source ->
            finalLiveData.value = source
        }
    }
    return finalLiveData
}

/**
 * Emits the `startingValue` before any other value.
 */
fun <T> LiveData<T>.startWith(startingValue: T?): LiveData<T> {
    val finalLiveData: MediatorLiveData<T> = MediatorLiveData()
    finalLiveData.value = startingValue
    finalLiveData.addSource(this) { source ->
        finalLiveData.value = source
    }
    return finalLiveData
}

/**
 * Converts the LiveData to `SingleLiveData` and concats it with the `otherLiveData` and emits their
 * values one by one
 */
fun <T> LiveData<T>.then(otherLiveData: LiveData<T>): LiveData<T> {
    return if (this is SingleLiveData) {
        when (otherLiveData) {
            is SingleLiveData -> SingleLiveDataConcat(this, otherLiveData)
            else -> SingleLiveDataConcat(this, otherLiveData.toSingleLiveData())
        }
    } else {
        when (otherLiveData) {
            is SingleLiveData -> SingleLiveDataConcat(this.toSingleLiveData(), otherLiveData)
            else -> SingleLiveDataConcat(this.toSingleLiveData(), otherLiveData.toSingleLiveData())
        }
    }
}

fun <T> LiveData<T>.concatWith(otherLiveData: LiveData<T>) = then(otherLiveData)

/**
 * Concats the given LiveData together and emits their values one by one in order
 */
fun <T> concat(vararg liveData: LiveData<T>): LiveData<T> {
    val liveDataList = mutableListOf<SingleLiveData<T>>()
    liveData.forEach {
        if (it is SingleLiveData<T>)
            liveDataList.add(it)
        else
            liveDataList.add(it.toSingleLiveData())
    }
    return SingleLiveDataConcat(liveDataList)
}


/**
 * Emits the items that pass through the predicate
 */
inline fun <T> LiveData<T>.filter(crossinline predicate: (T?) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) {
        if (predicate(it))
            mutableLiveData.value = it
    }
    return mutableLiveData
}


/**
 * emits the item that was emitted at `index` position
 * Note: This only works for elements that were emitted `after` the `elementAt` is applied.
 */
fun <T> LiveData<T>.elementAt(index: Int): SingleLiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    var currentIndex = 0
    if (this.value != null)
        currentIndex = -1
    mutableLiveData.addSource(this) {
        if (currentIndex == index) {
            mutableLiveData.value = it
            mutableLiveData.removeSource(this)
        }
        currentIndex++
    }
    return SingleLiveData(mutableLiveData)
}

/**
 * Notifies the observer with the same live data as it holds
 * @receiver MutableLiveData<T>
 */
fun <T> MutableLiveData<T>.notifyObserver() {
    postValue(value)
}

/**
 * Emits only the values that are not null
 */
fun <T> LiveData<T>.nonNull(): NonNullLiveData<T> {
    return NonNullLiveData(this)
}

/**
 * Buffers the items emitted by the LiveData, and emits them when they reach the `count` as a List.
 */
fun <T> LiveData<T>.buffer(count: Int): MutableLiveData<List<T?>> {
    val mutableLiveData: MediatorLiveData<List<T?>> = MediatorLiveData()
    val latestBuffer = mutableListOf<T?>()
    mutableLiveData.addSource(this) { value ->
        synchronized(latestBuffer) {
            latestBuffer.add(value)
            if (latestBuffer.size == count) {
                mutableLiveData.value = latestBuffer.toList()
                latestBuffer.clear()
            }
        }

    }
    return mutableLiveData
}

/**
 * Gets the application context from within the android viewmodel
 */
val AndroidViewModel.context: Context
    get() = getApplication()


fun <X, Y> LiveData<X>.map(mapFunction: (value: X?) -> Y?) =
        Transformations.map(this, mapFunction)


fun <X, Y> LiveData<X>.switchMap(mapFunction: (value: X?) -> LiveData<Y>): LiveData<Y> =
        Transformations.switchMap(this, mapFunction)

inline fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, crossinline onEventUnhandledContent: (T) -> Unit) {
    observe(owner, Observer { it?.getContentIfNotHandled()?.let(onEventUnhandledContent) })
}

/**
 * Creates an instance of [LiveData] with `this` as its value.
 */
fun <T> T?.asLiveData(): LiveData<T> = MutableLiveData<T>().apply { value = this@asLiveData }


/**
 * Creates an instance of [MutableLiveData] with `this` as its value.
 */
fun <T> T?.asMutableLiveData(): MutableLiveData<T> = MutableLiveData<T>().apply { value = this@asMutableLiveData }


/**
 * Shorthand function that will observe the liveData using `this` as the [LifecycleOwner]. If the emitted value is not
 * null then body is invoked with it as an argument.
 *
 * @param liveData the [LiveData] to be observed
 * @param body function to be invoked with the emitted value as a parameter
 */
inline fun <T, L : LiveData<T>> LifecycleOwner.observe(liveData: L, crossinline body: (T) -> Unit = {}) {
    liveData.observe(this, Observer { it?.let(body) })
}

/**
 * Shorthand function that will observe the liveData using `this` as the [LifecycleOwner]. It uses [EventObserver] to
 * observe the emitted values.
 *
 * @param liveData the [LiveData] of [Event] to be observed
 * @param body function to be invoked with the emitted value as a parameter
 */
fun <T, L : LiveData<Event<T>>> LifecycleOwner.observeEvent(liveData: L, body: (T) -> Unit = {}) {
    liveData.observe(this, EventObserver(body))
}


/**
 * For all the ones till region
 * USAGE  fun filteredData(): LiveData<List<YourObject>>? {

return Transformations.switchMap(searchQuery) {
dbResultDataFiltered.performSearch(it, "title", dbResultData)
}
}
 */
inline fun <reified T> MutableLiveData<List<T>>.performSearchDB(searchQuery: String, fieldName: String, dbResultData: MutableLiveData<DBResult<List<T>>>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    dbResultData.getSuccess { list ->
        list.forEach { model ->
            val property = model?.firstPropertyValue(fieldName)
            property?.apply {
                if (toLowerCase().contains(searchQuery, true)) {
                    applyFilteredList.add(model)
                }
            }
        }
    }
    this.value = applyFilteredList
    return this
}

inline fun <reified T> LiveData<List<T>>.performSearchDB(searchQuery: String, fieldName: String, dbResultData: MutableLiveData<DBResult<List<T>>>, filteredList: MutableLiveData<List<T>>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    dbResultData.getSuccess { list ->
        list.forEach { model ->
            val property = model?.firstPropertyValue(fieldName)
            property?.apply {
                if (toLowerCase().contains(searchQuery, true)) {
                    applyFilteredList.add(model)
                }
            }
        }
    }
    filteredList.value = applyFilteredList
    return this
}

inline fun <reified T> LiveData<List<T>>.performSearch(searchQuery: String, fieldName: String, dbResultData: List<T>, filteredList: MutableLiveData<List<T>>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    dbResultData.forEach { model ->
        val property = model?.firstPropertyValue(fieldName)
        property?.apply {
            if (toLowerCase().contains(searchQuery, true)) {
                applyFilteredList.add(model)
            }
        }
    }
    filteredList.value = applyFilteredList
    return this
}


inline fun <reified T> MutableLiveData<List<T>>.performSearch(searchQuery: String, fieldName: String, dbResultData: List<T>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    dbResultData.forEach { model ->
        val property = model?.firstPropertyValue(fieldName)
        property?.apply {
            if (toLowerCase().contains(searchQuery, true)) {
                applyFilteredList.add(model)
            }
        }
    }
    this.value = applyFilteredList
    return this
}
/**
 * REGION
 */


/**
 * For all the ones till region
 * USAGE  fun filteredData(): LiveData<List<YourObject>>? {

return Transformations.switchMap(searchQuery) {
dbResultDataFiltered.performSearch(it, "title", dbResultData)
}
}
 */
inline fun <reified T> MutableLiveData<List<T>>.performSearchAPI(searchQuery: String, fieldName: String, resultData: MutableLiveData<RetrofitResult<List<T>>>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    resultData.getSuccess { list ->
        list.forEach { model ->
            val property = model?.firstPropertyValue(fieldName)
            property?.apply {
                if (toLowerCase().contains(searchQuery, true)) {
                    applyFilteredList.add(model)
                }
            }
        }
    }
    this.value = applyFilteredList
    return this
}

inline fun <reified T> LiveData<List<T>>.performSearchAPI(searchQuery: String, fieldName: String, resultData: MutableLiveData<RetrofitResult<List<T>>>, filteredList: MutableLiveData<List<T>>): LiveData<List<T>> {
    val applyFilteredList: ArrayList<T> = ArrayList()
    resultData.getSuccess { list ->
        list.forEach { model ->
            val property = model?.firstPropertyValue(fieldName)
            property?.apply {
                if (toLowerCase().contains(searchQuery, true)) {
                    applyFilteredList.add(model)
                }
            }
        }
    }
    filteredList.value = applyFilteredList
    return this
}


/**
 * REGION
 */


inline fun <reified T> MutableLiveData<List<T>>.switchMapSearchDB(searchQuery: MutableLiveData<String>, fieldName: String, dbResultData: MutableLiveData<DBResult<List<T>>>): LiveData<List<T>> {
    return Transformations.switchMap(searchQuery) {
        performSearchDB(it, fieldName, dbResultData)
    }
}

inline fun <reified T> LiveData<List<T>>.switchMapSearchDB(searchQuery: MutableLiveData<String>,
                                                           fieldName: String,
                                                           dbResultData: MutableLiveData<DBResult<List<T>>>,
                                                           filteredList: MutableLiveData<List<T>>): LiveData<List<T>> {
    return Transformations.switchMap(searchQuery) {
        performSearchDB(it, fieldName, dbResultData, filteredList)
    }
}



inline fun <reified T> MutableLiveData<List<T>>.switchMapSearchAPI(searchQuery: MutableLiveData<String>, fieldName: String, resultData: MutableLiveData<RetrofitResult<List<T>>>): LiveData<List<T>> {
    return Transformations.switchMap(searchQuery) {
        performSearchAPI(it, fieldName, resultData)
    }
}

inline fun <reified T> LiveData<List<T>>.switchMapSearchAPI(searchQuery: MutableLiveData<String>,
                                                           fieldName: String,
                                                           dbResultData: MutableLiveData<RetrofitResult<List<T>>>,
                                                           filteredList: MutableLiveData<List<T>>): LiveData<List<T>> {
    return Transformations.switchMap(searchQuery) {
        performSearchAPI(it, fieldName, dbResultData, filteredList)
    }
}


