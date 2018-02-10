package io.frappe.android.UI

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.LinearLayout
import com.facebook.common.util.UriUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.custom.ankoView

class PhotoUIItem : AnkoComponent<ViewGroup> {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun createView(ui: AnkoContext<ViewGroup>): View {
        // UI list item
        val out = with(ui){
            linearLayout {
                lparams(width = wrapContent, height = matchParent)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                cardView {
                    simpleDraweeView {
                        id = Ids.listItem
                    }.lparams(width = dip(50), height = dip(50))
                    //verticalLayout {
                    //}.lparams(width = wrapContent, height = matchParent)
                }.lparams(height = wrapContent, width = wrapContent)
            }
        }
        return out
    }

    companion object {
        object Ids {
            val itemType = 0
            val listItem = 1
        }
    }
}

var SimpleDraweeView.imageRes get() = 1
    set(value) {
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(UriUtil.getUriForResourceId(value))
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .build()
        setController(controller)
    }

var SimpleDraweeView.imageUrl get() = ""
    set(value) {
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(value))
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .build()
        setController(controller)
    }

inline fun ViewManager.simpleDraweeView(theme: Int = 0): SimpleDraweeView = simpleDraweeView(theme) {}
inline fun ViewManager.simpleDraweeView(theme: Int = 0, init: SimpleDraweeView.() -> Unit)
        = ankoView(::SimpleDraweeView, theme) { init() }

inline fun Context.simpleDraweeView(theme: Int = 0): SimpleDraweeView = simpleDraweeView(theme) {}
inline fun Context.simpleDraweeView(theme: Int = 0, init: SimpleDraweeView.() -> Unit)
        = ankoView(::SimpleDraweeView, theme) { init() }

inline fun AppCompatActivity.simpleDraweeView(theme: Int = 0): SimpleDraweeView = simpleDraweeView(theme) {}
inline fun AppCompatActivity.simpleDraweeView(theme: Int = 0, init: SimpleDraweeView.() -> Unit)
        = ankoView(::SimpleDraweeView, theme) { init() }