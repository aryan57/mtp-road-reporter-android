import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.utils.ApiRequestHandler
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.RoadReport
import com.example.myapplication.utils.UtilityFunctions


class RoadReportAdapter(private val onReportDeleted: OnReportDeletedListener) :
    ListAdapter<RoadReport, RoadReportAdapter.RoadReportViewHolder>(RoadReportDiffCallback()) {

    interface OnReportDeletedListener {
        fun onReportDeleted()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoadReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item_road_report, parent, false)
        return RoadReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoadReportViewHolder, position: Int) {
        val roadReport = getItem(position)
        holder.bind(roadReport)
    }

    inner class RoadReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val categoryTextView: TextView = itemView.findViewById(R.id.category)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.date_time)
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val location: TextView = itemView.findViewById(R.id.location)
        private val description: TextView = itemView.findViewById(R.id.description)
        private val delete: Button = itemView.findViewById(R.id.delete)
        private val apiRequestHandler = ApiRequestHandler(itemView.context)

        fun bind(roadReport: RoadReport) {
            userNameTextView.text = roadReport.name
            categoryTextView.text = roadReport.categoryName
            dateTimeTextView.text = UtilityFunctions.unixTimeToCustomDateString(roadReport.unixTime)
            description.text = roadReport.description
            location.text = UtilityFunctions.getGoogleMapsLinkFromLatLong(
                roadReport.latitude, roadReport.longitude
            )

            delete.setOnClickListener {
                apiRequestHandler.makeApiRequest(Request.Method.DELETE,
                    Constants.API_PATH_DELETE_POST + "/" + roadReport.id.toString(),
                    null,
                    { response ->
                        Toast.makeText(
                            itemView.context, response.getString("message"), Toast.LENGTH_SHORT
                        ).show()

                        // Notify the fragment to refresh the list
                        onReportDeleted.onReportDeleted()

                    },
                    { errorMessage ->
                        Toast.makeText(itemView.context, errorMessage, Toast.LENGTH_SHORT).show()
                    },
                    {})
            }

            Glide.with(imageView.context)
                .load(UtilityFunctions.getImageUrlFromImgID(roadReport.imgID))
                .placeholder(R.drawable.baseline_downloading_24).error(R.drawable.baseline_error_24)
                .into(imageView)
        }
    }
}

class RoadReportDiffCallback : DiffUtil.ItemCallback<RoadReport>() {
    override fun areItemsTheSame(oldItem: RoadReport, newItem: RoadReport): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RoadReport, newItem: RoadReport): Boolean {
        return oldItem == newItem
    }

}
