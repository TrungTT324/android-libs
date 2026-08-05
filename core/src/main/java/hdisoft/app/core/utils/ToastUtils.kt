package hdisoft.app.core.utils

import android.content.Context
import android.widget.Toast

/**
 * Extension functions for showing Toast notifications easily across activities and components.
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showLongToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}
