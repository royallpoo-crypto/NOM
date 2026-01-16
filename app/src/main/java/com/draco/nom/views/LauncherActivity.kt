package com.draco.nom.views

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.draco.nom.databinding.ActivityLauncherBinding
import com.draco.nom.viewmodels.LauncherActivityViewModel

class LauncherActivity: AppCompatActivity() {
    private val viewModel: LauncherActivityViewModel by viewModels()
    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Makes the navigation bar all nice and transparent! */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
            window.decorView.setOnApplyWindowInsetsListener { _, windowInsets ->
                windowInsets
            }

        /* When the app list updates, make sure to update the recycler */
        viewModel.appList.observe(this) {
            if (it.isNullOrEmpty())
                return@observe

            /* Hide that pesky progress bar; we don't need it anymore */
            binding.progress.visibility = View.GONE

            viewModel.recyclerAdapter.appList = it
            viewModel.recyclerAdapter.notifyDataSetChanged()
        }

        /* Update the progress bar progress when it changes */
        viewModel.packageListProgress.observe(this) {
            binding.progress.progress = it
        }

        /* Setup the recycler view now */
        viewModel.prepareRecycler(this, binding.recycler)
        // start the foreground service that will show a notification and toasts
        val svcIntent = Intent(this, com.draco.nom.services.LauncherForegroundService::class.java).apply {
            action = com.draco.nom.services.LauncherForegroundService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svcIntent) else startService(svcIntent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePackageIdList()
    }

    override fun onDestroy() {
        super.onDestroy()
        // do not stop the foreground service here; it runs independently
    }

    override fun onBackPressed() {}
}