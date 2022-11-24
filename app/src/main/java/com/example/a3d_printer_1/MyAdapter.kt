package com.example.a3d_printer_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

//class MyAdapter(private val userList : ArrayList<User>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item,parent, false)
//        return MyViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        val currentitem = userList[position]
//        holder.firstName.text = currentitem.firstName
//    }
//
//    override fun getItemCount(): Int {
//        return userList.size
//    }
//
//    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
//        val firstName : TextView = itemView.findViewById(R.id.tvfirstName)
//    }
//
//}
class MyAdapter(private val gcodeFileList : ArrayList<gcodeFileClass>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
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

//        holder.itemView.setOnClickListener{
//            onItemClick?
//        }
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
        }
    }

}