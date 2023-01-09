package com.kodego.velascoben.nrw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kodego.velascoben.nrw.databinding.GridItemBinding

class SessionAdapter (var sessionModel : List<Sessions>) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    inner class SessionViewHolder(var binding : GridItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GridItemBinding.inflate(layoutInflater, parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.binding.apply {
            tvTime.text = sessionModel[position].sessionTime
            tvDate.text = sessionModel[position].sessionDate
            tvSession.text = sessionModel[position].sessionType

        }
    }

    override fun getItemCount(): Int {
        return sessionModel.size
    }


}