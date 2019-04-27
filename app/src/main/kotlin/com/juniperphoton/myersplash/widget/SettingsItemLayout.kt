package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.utils.LocalSettingHelper

typealias OnCheckedListener = ((Boolean) -> Unit)

class SettingsItemLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var checked: Boolean
        get() = compoundButton.isChecked
        set(checked) {
            compoundButton.isChecked = checked
        }

    @BindView(R.id.settings_item_title)
    lateinit var titleTextView: TextView

    @BindView(R.id.settings_item_content)
    lateinit var contentTextView: TextView

    @BindView(R.id.settings_item_switch)
    lateinit var compoundButton: CompoundButton

    @BindView(R.id.divider_view)
    lateinit var dividerView: View

    var onCheckedChanged: OnCheckedListener? = null

    var title: String = ""
        set(value) {
            titleTextView.text = value
            field = value
        }

    var content: String = ""
        set(value) {
            contentTextView.text = value
            field = value
        }

    var preferenceKey: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.row_settings_item, this, true)

        ButterKnife.bind(this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.SettingsItemLayout)
        val title = array.getString(R.styleable.SettingsItemLayout_setting_title)
        val content = array.getString(R.styleable.SettingsItemLayout_setting_content)
        val hasCheckbox = array.getBoolean(R.styleable.SettingsItemLayout_has_checkbox, false)
        val showDivider = array.getBoolean(R.styleable.SettingsItemLayout_show_divider, true)
        val defaultValue = array.getBoolean(R.styleable.SettingsItemLayout_default_value, true)
        preferenceKey = array.getString(R.styleable.SettingsItemLayout_preference_key)
        array.recycle()

        preferenceKey?.let {
            val value = LocalSettingHelper.getBoolean(context, it, defaultValue)
            this.checked = value
        }

        if (title != null) {
            titleTextView.text = title
        }

        if (content != null) {
            contentTextView.text = content
        }

        if (!hasCheckbox) {
            compoundButton.visibility = View.GONE
        }

        if (!showDivider) {
            dividerView.visibility = View.GONE
        }

        setOnClickListener {
            checked = !checked
        }

        compoundButton.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged?.invoke(isChecked)
            preferenceKey?.let {
                LocalSettingHelper.putBoolean(context, it, isChecked)
            }
        }
    }
}
