package com.kodego.velascoben.nrw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kodego.velascoben.nrw.databinding.RepairItemBinding
import com.kodego.velascoben.nrw.db.Reports

class RepairsAdapter(var repairModel : List<Reports>) : RecyclerView.Adapter<RepairsAdapter.RepairViewHolder>() {

    inner class RepairViewHolder(var binding : RepairItemBinding) : RecyclerView.ViewHolder(binding.root)

    var onUpdate : ((Reports, Int) -> Unit) ? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RepairItemBinding.inflate(layoutInflater, parent, false)
        return RepairViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
        holder.binding.apply {

            var type : String = ""

            // Check leakage type is reported
            if (repairModel[position].reportType == "low") {
                type = "Low"
            } else if (repairModel[position].reportType == "medium") {
                type = "Medium"
            } else if (repairModel[position].reportType == "high") {
                type = "High"
            }

            tvReportTime.text = repairModel[position].reportTime
            tvReportDate.text = repairModel[position].reportDate
            tvReportBarangay.text = repairModel[position].reportAddress2
            tvReportType.text = "Type: $type"

            btnRepair.setOnClickListener() {
                onUpdate?.invoke(repairModel[position],position)
            }

        }
    }

    override fun getItemCount(): Int {
        return repairModel.size
    }


}