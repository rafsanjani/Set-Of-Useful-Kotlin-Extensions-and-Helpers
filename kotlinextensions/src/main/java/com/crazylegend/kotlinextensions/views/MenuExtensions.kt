package com.crazylegend.kotlinextensions.views

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.*
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import com.crazylegend.kotlinextensions.R


/**
 * Created by hristijan on 3/7/19 to long live and prosper !
 */


/**
 * Sets a color filter on all menu icons, including the overflow button (if it exists)
 */
@UiThread
@JvmOverloads fun colorMenu(activity: Activity, menu: Menu, @ColorInt color: Int,
                            alpha: Int = 0) {
    for (i in 0 until menu.size()) {
        val menuItem = menu.getItem(i)
        colorMenuItem(menuItem, color, alpha)

        if (menuItem.hasSubMenu()) {
            val subMenu = menuItem.subMenu

            for (j in 0 until subMenu.size()) {
                colorMenuItem(subMenu.getItem(j), color, alpha)
            }
        }
    }

    val filter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    activity.runOnUiThread { setOverflowButtonColor(activity, filter, alpha) }
}

/**
 * Sets a color filter on a [MenuItem]
 */
@UiThread
@JvmOverloads fun colorMenuItem(menuItem: MenuItem, @ColorInt color: Int, alpha: Int = 0) {
    val drawable = menuItem.icon
    if (drawable != null) {
        // If we don't mutate the drawable, then all drawables with this id will have a color
        // filter applied to it.
        drawable.mutate()
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        if (alpha > 0) {
            drawable.alpha = alpha
        }
    }
}

/**
 * It's important to set overflowDescription attribute in styles, so we can grab the reference
 * to the overflow icon. Check: res/values/styles.xml
 *
 *
 * @param activity
 * *
 * @param colorFilter
 * *
 * @see [Source](https://snowdog.co/blog/how-to-dynamicaly-change-android-toolbar-icons-color/)
 */
@SuppressLint("PrivateResource")
@UiThread
private fun setOverflowButtonColor(activity: Activity,
                                   colorFilter: PorterDuffColorFilter, alpha: Int) {
    val overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description)
    val decorView = activity.window.decorView as ViewGroup
    val viewTreeObserver = decorView.viewTreeObserver
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val outViews = ArrayList<View>()
            decorView.findViewsWithText(outViews, overflowDescription,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)

            if (outViews.isEmpty()) {
                return
            }

            val overflow = outViews[0] as ImageView
            overflow.colorFilter = colorFilter

            if (alpha > 0) {
                    overflow.imageAlpha = alpha

            }

            removeOnGlobalLayoutListener(decorView, this)
        }
    })
}

private fun removeOnGlobalLayoutListener(v: View, listener: ViewTreeObserver.OnGlobalLayoutListener) {

        v.viewTreeObserver.removeOnGlobalLayoutListener(listener)

}