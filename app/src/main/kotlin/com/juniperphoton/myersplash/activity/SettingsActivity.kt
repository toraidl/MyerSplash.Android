package com.juniperphoton.myersplash.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.event.RefreshUIEvent
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.Toaster
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import org.greenrobot.eventbus.EventBus

@Suppress("unused", "unused_parameter")
class SettingsActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "SettingsActivity"

        private val KEY_SAVING_QUALITY = App.instance.getString(R.string.preference_key_saving_quality)
        private val KEY_LIST_QUALITY = App.instance.getString(R.string.preference_key_list_quality)
    }

    private lateinit var savingStrings: Array<String>
    private lateinit var loadingStrings: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        quickDownloadSettings.onCheckedChanged = {
            EventBus.getDefault().post(RefreshUIEvent())
        }

        savingStrings = arrayOf(
                getString(R.string.settings_saving_highest),
                getString(R.string.settings_saving_high),
                getString(R.string.settings_saving_medium))

        loadingStrings = arrayOf(
                getString(R.string.settings_loading_large),
                getString(R.string.settings_loading_small),
                getString(R.string.settings_loading_thumb))

        val savingChoice = LocalSettingHelper.getInt(this, KEY_SAVING_QUALITY, 1)
        savingQualitySettings.content = savingStrings[savingChoice]

        val loadingChoice = LocalSettingHelper.getInt(this, KEY_LIST_QUALITY, 0)
        loadingQualitySettings.content = loadingStrings[loadingChoice]

        clearCacheSettings.setOnClickListener(this)
        settingClearDatabase.setOnClickListener(this)
        savingQualitySettings.setOnClickListener(this)
        loadingQualitySettings.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clearCacheSettings -> {
                clearUp()
            }
            R.id.settingClearDatabase -> {
                clearDatabase()
            }
            R.id.savingQualitySettings -> {
                setSavingQuality()
            }
            R.id.loadingQualitySettings -> {
                setLoadingQuality()
            }
        }
    }

    private fun clearUp() {
        Fresco.getImagePipeline().clearCaches()
        Toaster.sendShortToast("All clear :D")
        clearCacheSettings.content = "0 MB"
        EventBus.getDefault().post(RefreshUIEvent())
    }

    private fun clearDatabase() {
        Toaster.sendShortToast("All clear :D")
        RealmCache.getInstance().executeTransaction(Realm::deleteAll)
    }

    private fun setSavingQuality() {
        val choice = LocalSettingHelper.getInt(this, KEY_SAVING_QUALITY, 1)
        AlertDialog.Builder(this@SettingsActivity).apply {
            setTitle(getString(R.string.settings_saving_quality))
            setSingleChoiceItems(savingStrings, choice) { dialog, which ->
                LocalSettingHelper.putInt(this@SettingsActivity, KEY_SAVING_QUALITY, which)
                dialog.dismiss()
                savingQualitySettings.content = savingStrings[which]
            }
            show()
        }
    }

    private fun setLoadingQuality() {
        val choice = LocalSettingHelper.getInt(this, KEY_LIST_QUALITY, 0)
        AlertDialog.Builder(this@SettingsActivity).apply {
            setTitle(getString(R.string.settings_loading_quality))
            setSingleChoiceItems(loadingStrings, choice) { dialog, which ->
                LocalSettingHelper.putInt(this@SettingsActivity, KEY_LIST_QUALITY, which)
                dialog.dismiss()
                loadingQualitySettings.content = loadingStrings[which]
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        clearCacheSettings.content = "${ImagePipelineFactory.getInstance().mainFileCache.size / 1024 / 1024} MB"
    }
}
