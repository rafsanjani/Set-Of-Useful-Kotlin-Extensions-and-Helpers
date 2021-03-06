package com.crazylegend.kotlinextensions.rx.bindings

import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.crazylegend.kotlinextensions.rx.mainThreadScheduler
import com.crazylegend.kotlinextensions.rx.newThreadScheduler
import com.jakewharton.rxbinding3.slidingpanelayout.panelOpens
import com.jakewharton.rxbinding3.slidingpanelayout.panelSlides
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


/**
 * Created by crazy on 3/18/20 to long live and prosper !
 */

fun SlidingPaneLayout.panelOpenChanges(debounce: Long = 300L, debounceTime: TimeUnit = TimeUnit.MILLISECONDS, skipInitialValue: Boolean = true,
                                  compositeDisposable: CompositeDisposable,
                                  callback: (state: Boolean) -> Unit = {}) {
    val changes = panelOpens()
    if (skipInitialValue) {
        changes.skipInitialValue()
    }

    changes.debounce(debounce, debounceTime)
            .subscribeOn(newThreadScheduler)
            .observeOn(mainThreadScheduler)
            .subscribe({
                callback(it)
            }, {
                it.printStackTrace()
            }).addTo(compositeDisposable)
}


fun SlidingPaneLayout.panelSlideChanges(debounce: Long = 300L, debounceTime: TimeUnit = TimeUnit.MILLISECONDS, compositeDisposable: CompositeDisposable,
                                        callback: (state: Float) -> Unit = {}) {
    val changes = panelSlides()

    changes.debounce(debounce, debounceTime)
            .subscribeOn(newThreadScheduler)
            .observeOn(mainThreadScheduler)
            .subscribe({
                callback(it)
            }, {
                it.printStackTrace()
            }).addTo(compositeDisposable)
}
