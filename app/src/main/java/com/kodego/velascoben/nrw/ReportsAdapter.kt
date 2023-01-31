package com.kodego.velascoben.nrw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kodego.velascoben.nrw.databinding.LeakItemBinding
import com.kodego.velascoben.nrw.db.Reports

class ReportsAdapter(var reportModel : List<Reports>) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(var binding : LeakItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LeakItemBinding.inflate(layoutInflater, parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.binding.apply {

            var type : String = ""

            // Check leakage type is reported
            if (reportModel[position].reportType == "low") {
                type = "Low"
            } else if (reportModel[position].reportType == "medium") {
                type = "Medium"
            } else if (reportModel[position].reportType == "high") {
                type = "High"
            }

            tvReportTime.text = reportModel[position].reportTime
            tvReportDate.text = reportModel[position].reportDate
            tvReportBarangay.text = reportModel[position].reportAddress2
            tvReportType.text = "Type: $type"

            if (reportModel[position].repairDate == "") {
                tvReportStatus.text = "PENDING"
            } else {
                tvReportStatus.text = "REPAIRED"
            }


        }
    }

    override fun getItemCount(): Int {
        return reportModel.size
    }


}