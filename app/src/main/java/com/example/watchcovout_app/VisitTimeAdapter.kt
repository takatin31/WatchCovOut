package com.example.watchcovout_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class VisitTimeAdapter (val activity: HomeActivity, val listTimes : ArrayList<VisitTime>) : RecyclerView.Adapter<VisitTimeAdapter.CommentViewHolder>() {
    class CommentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var time = v.findViewById<TextView>(R.id.timeTextView)
        var nbrPeople = v.findViewById<TextView>(R.id.nbrPeopleTextView)
        var checkbox = v.findViewById<CheckBox>(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(activity).inflate(R.layout.item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listTimes.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val time = listTimes[position]
        holder.time.text = time.time
        holder.nbrPeople.text = "${time.nbrPeople} person have already reserved this place"
        holder.checkbox.isChecked = time.checked

        holder.checkbox.setOnClickListener {
            val newV = !listTimes[position].checked

            if (newV){
                for (item in listTimes){
                    item.checked = false
                }
                listTimes[position].checked = newV
                holder.checkbox.isChecked = newV
                activity.chosenTime = time.time
            }else{
                activity.chosenTime = ""
            }
        }

    }


}