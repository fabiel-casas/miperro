package com.casas.fabiel.miperro

import ai.api.AIConfiguration
import ai.api.AIListener
import ai.api.GsonFactory
import ai.api.model.AIError
import ai.api.model.AIResponse
import ai.api.ui.AIButton
import android.Manifest
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.casas.fabiel.miperro.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), AIListener, AIButton.AIButtonListener {

    val TAG = MainActivity::class.java!!.getName()
    private var binding: ActivityMainBinding? = null
    val PERMISSIONS_REQUEST_LISTENING: Int = 100
    private val gson = GsonFactory.getGson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val config = AIConfiguration("c9a7d8ffc2b347af8ffa1a66abc2f6ca", "5e3dbab53cf148d18794197fa429e7fc",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System)
        binding?.buttonProcess?.initialize(config)
        binding?.buttonProcess?.setResultsListener(this)
        requirePermissions()
    }

    private fun requirePermissions() {
        val permissionsNeeded = ArrayList<String>()
        val permissionsList = ArrayList<String>()
        if (!addPermission(permissionsList, Manifest.permission.INTERNET))
            permissionsNeeded.add("android.permission.INTERNET")
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("android.permission.RECORD_AUDIO")

        if (!permissionsList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsList.toTypedArray(),
                    PERMISSIONS_REQUEST_LISTENING)
        }
    }

    override fun onResult(response: AIResponse) {
        runOnUiThread {
            Log.d(TAG, "onResult")

            Log.i(TAG, "Received success response ${gson.toJson(response)}")

            // this is example how to get different parts of result object
            val status = response.status
            Log.i(TAG, "Status code: " + status.code)
            Log.i(TAG, "Status type: " + status.errorType)

            val result = response.result
            Log.i(TAG, "Resolved query: " + result.resolvedQuery)

            Log.i(TAG, "Action: " + result.action)
            Log.i(TAG, "Speech: " + result.fulfillment.speech)
            binding?.textViewResult?.text = result.fulfillment.speech

            val metadata = result.metadata
            if (metadata != null) {
                Log.i(TAG, "Intent id: " + metadata.intentId)
                Log.i(TAG, "Intent name: " + metadata.intentName)
            }

            val params = result.parameters
            if (params != null && !params.isEmpty()) {
                Log.i(TAG, "Parameters: ")
                for (entry in params.entries) {
                    Log.i(TAG, String.format("%s: %s", entry.key, entry.value.toString()))
                }
            }
        }
    }

    override fun onError(error: AIError) {
        runOnUiThread {
            Log.d(TAG, "onError")
            binding?.textViewResult?.text = error?.message
        }
    }

    override fun onCancelled() {
        runOnUiThread {
            Log.d(TAG, "onCancelled")
            binding?.textViewResult?.text = ""
        }
    }

    override fun onListeningStarted() {
        binding?.textViewResult?.text = ""
    }

    override fun onAudioLevel(level: Float) {
    }

    override fun onListeningCanceled() {
    }

    override fun onListeningFinished() {
    }

    private fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission)
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false
        }
        return true
    }
}
