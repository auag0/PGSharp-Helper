package com.azarasi.pgsharphelper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azarasi.pgsharphelper.models.Route
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class RouteListAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<RouteListAdapter.ViewHolder>() {
    var routes: List<Route> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.listitem_route_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRouteName: MaterialTextView = itemView.findViewById(R.id.tvRouteName)
        private val tvRoutePoints: MaterialTextView = itemView.findViewById(R.id.tvRoutePoints)
        private val btnRouteSave: MaterialButton = itemView.findViewById(R.id.btnRouteSave)

        fun bind(route: Route) {
            tvRouteName.text = route.name
            tvRoutePoints.text = route.points.size.toString()
            btnRouteSave.setOnClickListener {
                listener.saveRouteAsGpx(route)
            }
        }
    }

    interface Listener {
        fun saveRouteAsGpx(route: Route)
    }
}