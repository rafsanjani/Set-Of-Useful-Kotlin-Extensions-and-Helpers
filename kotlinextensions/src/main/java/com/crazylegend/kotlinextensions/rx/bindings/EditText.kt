package com.crazylegend.kotlinextensions.rx.bindings

import android.widget.EditText
import com.crazylegend.kotlinextensions.rx.mainThreadScheduler
import com.crazylegend.kotlinextensions.rx.newThreadScheduler
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


/**
 * Created by crazy on 3/18/20 to long live and prosper !
 */

fun EditText.textChanges(debounce: Long = 300L, debounceTime: TimeUnit = TimeUnit.MILLISECONDS, skipInitialValue: Boolean = true,
                        compositeDisposable: CompositeDisposable,
                        callback: (text: String) -> Unit = {}) {
    val changes = textChanges()
    if (skipInitialValue) {
        changes.skipInitialValue()
    }

    changes.debounce(debounce, debounceTime)
            .map { it.toString() }
            .subscribeOn(newThreadScheduler)
            .observeOn(mainThreadScheduler)
            .subscribe({
                callback(it.toString())
            }, {
                it.printStackTrace()
            }).addTo(compositeDisposable)
}


