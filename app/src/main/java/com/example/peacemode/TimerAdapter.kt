package com.example.peacemode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimerAdapter(
    private val timerList: MutableList<Timer>,
    private val onSwitchChanged: (Timer, Boolean) -> Unit,
    private val onDeleteClicked: (Timer) -> Unit
) : RecyclerView.Adapter<TimerAdapter.TimerViewHolder>() {

    class TimerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val enableSwitch: Switch = view.findViewById(R.id.enableSwitch)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timer_item, parent, false)
        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timer = timerList[position]
        holder.timeTextView.text = String.format("%02d:%02d", timer.hour, timer.minute)
        holder.enableSwitch.isChecked = timer.isEnabled

        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChanged(timer, isChecked)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(timer)
        }
    }

    override fun getItemCount() = timerList.size
}
