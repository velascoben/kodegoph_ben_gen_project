package com.kodego.velascoben.nrw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kodego.velascoben.nrw.databinding.UserItemBinding
import com.kodego.velascoben.nrw.db.Users

class UsersAdapter (var userModel : List<Users>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(var binding : UserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = UserItemBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.apply {
            userImage.setImageResource(userModel[position].userPhoto)
        }
    }

    override fun getItemCount(): Int {
        return userModel.size
    }


}