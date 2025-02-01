package com.dong.baselibrary.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dong.baselibrary.lifecycle.change
import java.util.Collections



abstract class BaseAdapter2<T, VB : ViewBinding> :
    RecyclerView.Adapter<BaseAdapter2<T, VB>.ViewHolder>() {

    private var binding: VB? = null

    abstract fun setBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB

    abstract fun addListData(newList: MutableList<T>)

    abstract fun setData(binding: VB, item: T, layoutPosition: Int)

    val listData: MutableList<T> = mutableListOf()
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        binding = setBinding(LayoutInflater.from(context), parent, viewType)
        return ViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(listData[position])
    }

    override fun getItemCount(): Int = listData.size


    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root) {

         fun bindData(obj: T) {
            bindView(obj)
            setData(binding, obj, layoutPosition)
        }

         fun bindView(obj: T) {
            onCLick(binding, obj, layoutPosition)
        }

    }

    open fun onCLick(binding: VB, item: T, layoutPosition: Int) {

    }
}

@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T, VB : ViewBinding> : RecyclerView.Adapter<BaseAdapter<T, VB>.ViewHolder>() {

    val listItem =  mutableListOf<T>()
    private var binding : VB? = null
    var currentPosition = MutableLiveData<Int>( RecyclerView.NO_POSITION)

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int) : VB

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =  LayoutInflater.from(parent.context)
        binding = createBinding(inflater, parent, viewType)
        return ViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.binding.bind(item, position)
    }

    override fun getItemCount(): Int = listItem.size

    abstract fun VB.bind(item: T, position: Int)

    var lastIndex : Int = 0

    fun submitList(items: List<T>) {
        this.listItem.clear()
        this.listItem.addAll(items)
        notifyDataSetChanged()
    }

    fun submitList(items: MutableLiveData<MutableList<T>>) {
        items.change {
            this.listItem.clear()
            this.listItem.addAll(it)
            notifyDataSetChanged()
        }

    }


    fun removeItem(position: Int) {
        if (position >= 0) {
            listItem.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun addItems(item: List<T>) {
        this.listItem.addAll(item)
        notifyDataSetChanged()
    }

    fun onMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(listItem, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(listItem, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
    fun swipe(position: Int) {
        listItem.removeAt(position)
        notifyItemRemoved(position)
    }
    fun addItem(item: T, index: Int) {
        this.listItem.add(index, item)
        notifyItemInserted(index)
    }

    fun removeItem(item: T){
        val index = listItem.indexOf(item)
        listItem.remove(item)
        notifyItemRemoved(index)
    }

    fun changeItemWithPos(index: Int, newItem : T){
        if(index < 0  || index >= listItem.size) return
        listItem[index] = newItem;
        notifyItemChanged(index)
    }

    fun getListItem() : ArrayList<T> {
        return listItem as ArrayList<T>
    }

    fun setCurrentPos(position: Int ){
        notifyItemChanged(currentPosition.value ?:RecyclerView.NO_POSITION)
        currentPosition.value = position
        notifyItemChanged(position)
    }
    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}