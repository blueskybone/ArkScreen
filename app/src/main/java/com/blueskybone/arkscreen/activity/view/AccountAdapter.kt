package com.blueskybone.arkscreen.activity.view

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.AccountActivity
import com.blueskybone.arkscreen.base.data.AccountSk

/**
 *   Created by blueskybone
 *   Date: 2024/7/30
 */
class AccountAdapter(val list: ArrayList<AccountSk>) : Adapter<AccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = list[position]
        if (account.isOfficial) {
            holder.channelImage.setImageResource(R.drawable.hg_icon_80x80)
        } else {
            holder.channelImage.setImageResource(R.drawable.bili_icon_75x71)
        }
        holder.nickName.text = list[position].nickName
        holder.deleteImage.setOnClickListener {
            removeData(position)
            onDataRemoved?.invoke(list)
        }
        holder.frameLayout.setOnClickListener {
            onItemClicked?.invoke(position)
            //设置为默认账号
        }
    }

    private var onDataRemoved: ((ArrayList<AccountSk>) -> Unit)? = null
    private var onItemClicked: ((Int) -> Unit)? = null

    fun setOnDataRemoved(func: (ArrayList<AccountSk>) -> Unit) {
        this.onDataRemoved = func
    }

    fun setOnItemClicked(func: (Int) -> Unit) {
        this.onItemClicked = func
    }


    @SuppressLint("NotifyDataSetChanged")
    fun removeData(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(account: AccountSk) {
        list.add(account)
        notifyItemInserted(itemCount-1)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameLayout: FrameLayout = itemView.findViewById(R.id.frame_account)
        val deleteImage: ImageView = itemView.findViewById(R.id.image_delete)
        val channelImage: ImageView = itemView.findViewById(R.id.channel_icon)
        val nickName: TextView = itemView.findViewById(R.id.text_username)
    }
}

//private fun <P1, R> ((P1) -> R).invoke(): R {
//
//}
