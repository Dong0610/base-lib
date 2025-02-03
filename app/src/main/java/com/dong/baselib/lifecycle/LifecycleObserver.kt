package com.dong.baselib.lifecycle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> mutableLiveData(value: T): MutableLiveData<T> = MutableLiveData(value)

fun <T> LiveData<T>.asFlow(): Flow<T> = MutableLiveData<T>().apply {
    this.value = this@asFlow.value
}.asFlow()

fun <T> MutableLiveData<T>.change(value: (T) -> Unit) {
    this.observeForever {
        value(it)
    }
}

fun <T> MutableLiveData<T>.post(value: T) {
    this.postValue(value)
}

fun <T> mutableListLiveData(initialList: MutableList<T> = mutableListOf()): MutableLiveData<MutableList<T>> {
    return MutableLiveData(initialList)
}

fun <T> MutableLiveData<MutableList<T>>.addItem(item: T) {
    val updatedList = this.value ?: mutableListOf()
    updatedList.add(item)
    this.postValue(updatedList)
}

fun <T> MutableLiveData<MutableList<T>>.removeItem(item: T) {
    val updatedList = this.value ?: mutableListOf()
    updatedList.remove(item)
    this.value = updatedList
}
fun <T> MutableLiveData<MutableList<T>>.changeItemAt(index: Int, newItem: T) {
    val updatedList = this.value ?: mutableListOf()

    if (index in updatedList.indices) {
        updatedList[index] = newItem
        this.postValue(updatedList)
    }
}

fun <T> MutableLiveData<MutableList<T>>.clearItems() {
    this.value = mutableListOf()
}


fun <T> Fragment.LauncherEffect(
    key: LiveData<T>,
    block: suspend CoroutineScope.(T) -> Unit
) {
    key.observe(viewLifecycleOwner) { newValue ->
        viewLifecycleOwner.lifecycleScope.launch {
            block(newValue)
        }
    }
}

fun <T> AppCompatActivity.DisposeEffect(
    key: MutableLiveData<T>,
    block: suspend CoroutineScope.(T) -> Unit
) {
    val observer = Observer<T> { newValue ->
        lifecycleScope.launch {
            block(newValue)
        }
    }
    val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            key.observe(this@DisposeEffect, observer)
        }

        override fun onStop(owner: LifecycleOwner) {
            key.removeObserver(observer)
        }
    }
    lifecycle.addObserver(lifecycleObserver)
}

fun <T> AppCompatActivity.LauncherEffect(
    key: MutableLiveData<T>,
    block: suspend CoroutineScope.(T) -> Unit
) {
    val observer = Observer<T> { newValue ->
        lifecycleScope.launch {
            block(newValue)
        }
    }
    key.observe(this, observer)

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            key.removeObserver(observer)
            super.onDestroy(owner)
        }
    })
}

class LifecycleObserver : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        println("Component has started")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
    }

    override fun onStop(owner: LifecycleOwner) {
    }
}