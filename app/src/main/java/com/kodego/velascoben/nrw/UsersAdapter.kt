package com.kodego.velascoben.nrw

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.kodego.velascoben.nrw.databinding.UserItemBinding
import java.io.File

class UsersAdapter(var userModel: ArrayList<SortUsers>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(var binding : UserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = UserItemBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.apply {
            if(userModel[position].userPhoto == "") {
                userImage.setImageResource(R.drawable.ic_user)
            } else {
                // Retrieve Image
                val imageName = userModel[position].userPhoto
                val storageRef = FirebaseStorage.getInstance().reference.child("images/$imageName")
                val localFile = File.createTempFile("tempImage","jpg")
                storageRef.getFile(localFile)
                    .addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    userImage.setImageBitmap(bitmap)
                }


            }
        }
    }

    override fun getItemCount(): Int {
        return userModel.size
    }


}