package com.example.supremeenigma

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.supremeenigma.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                runLsCommand()
            } else {
                showOutput(getString(R.string.permission_denied))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        binding.btnListFiles.setOnClickListener {
            listDirectoryContents()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    private fun listDirectoryContents() {
        if (!Shizuku.pingBinder()) {
            showOutput(getString(R.string.shizuku_not_running))
            return
        }

        when {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> runLsCommand()
            Shizuku.shouldShowRequestPermissionRationale() ->
                showOutput(getString(R.string.permission_denied))
            else -> Shizuku.requestPermission(0)
        }
    }

    /**
     * Runs `rish -c 'ls'` via Shizuku's privileged process builder to list
     * directory contents and displays the result.
     */
    private fun runLsCommand() {
        lifecycleScope.launch {
            val output = withContext(Dispatchers.IO) {
                try {
                    val process = Shizuku.newProcess(arrayOf("ls"), null, null)
                    // Read stdout before waitFor() to prevent buffer-full deadlock.
                    val text = process.inputStream.bufferedReader().readText()
                    process.waitFor()
                    text.ifEmpty { getString(R.string.empty_output) }
                } catch (e: Exception) {
                    getString(R.string.error_prefix) + e.message
                }
            }
            showOutput(output)
        }
    }

    private fun showOutput(text: String) {
        binding.tvOutput.text = text
    }
}

