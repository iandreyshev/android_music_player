package ru.iandreyshev.musicplayer.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected fun <TValue> LiveData<TValue>.observeWith(action: (TValue) -> Unit) {
        this.observe(this@BaseFragment, Observer {
            it?.apply(action)
        })
    }

    protected fun <TValue> LiveData<TValue>.viewObserveWith(action: (TValue) -> Unit) {
        this.observe(viewLifecycleOwner, Observer {
            it?.apply(action)
        })
    }

    protected fun <TValue> LiveData<TValue>.viewObserveNullableWith(action: (TValue?) -> Unit) {
        this.observe(viewLifecycleOwner, Observer {
            it.apply(action)
        })
    }

    protected fun <TValue> LiveData<TValue>.observeNullable(action: (TValue?) -> Unit) {
        this.observe(this@BaseFragment, Observer {
            action(it)
        })
    }

    protected fun <TValue> LiveData<TValue>.observeEvent(action: () -> Unit) {
        this.observe(this@BaseFragment, Observer {
            action()
        })
    }

    protected fun <TValue> LiveData<TValue>.viewObserveEvent(action: () -> Unit) {
        this.observe(this@BaseFragment, Observer {
            action()
        })
    }

}
