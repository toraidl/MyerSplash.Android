package com.juniperphoton.myersplash.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.juniperphoton.myersplash.R

/**
 * @author JuniperPhoton @ Zhihu Inc.
 * @since 2019-04-28
 */
fun buildMeteredWarningDialog(context: Context, positiveClick: () -> Unit): AlertDialog.Builder {
    return AlertDialog.Builder(context).apply {
        setTitle(R.string.attention)
        setMessage(R.string.wifi_attention_content)
        setPositiveButton(R.string.download) { dialog, _ ->
            dialog.dismiss()
            positiveClick()
        }
        setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    }
}