package com.example.a3d_printer_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class MyAdapter(private val gcodeFileList : ArrayList<gcodeFileClass>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    //testing begin

    private val diffCallback = object : DiffUtil.ItemCallback<gcodeFileClass>() {
        override fun areItemsTheSame(oldItem: gcodeFileClass, newItem: gcodeFileClass): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: gcodeFileClass, newItem: gcodeFileClass): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)


    //testing end

    interface onItemClickListener{
        fun onItemClick(position: Int)
        fun onLongItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        mListener = listener
    }

    fun setOnLongClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item,parent, false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = gcodeFileList[position]
        holder.fileName.text = currentitem.name
        holder.fileDate.text = "Timestamp: " + currentitem.date
        holder.fileSize.text = "File Size: " + currentitem.size
    }

    override fun getItemCount(): Int {
        return gcodeFileList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
//    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val fileName : TextView = itemView.findViewById(R.id.fileName)
        val fileDate : TextView = itemView.findViewById(R.id.fileDate)
        val fileSize : TextView = itemView.findViewById(R.id.fileSize)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }

            itemView.setOnLongClickListener{
                listener.onLongItemClick(adapterPosition)
                true
            }

        }
    }

}