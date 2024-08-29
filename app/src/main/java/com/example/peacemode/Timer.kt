package com.example.peacemode

data class Timer(val hour: Int, val minute: Int, var isEnabled: Boolean) {
    override fun toString(): String {
        return "$hour:$minute:$isEnabled"
    }

    companion object {
        fun fromString(timerString: String): Timer {
            val parts = timerString.split(":")
            return Timer(parts[0].toInt(), parts[1].toInt(), parts[2].toBoolean())
        }
    }
}