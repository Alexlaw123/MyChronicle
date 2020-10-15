package com.openlattice.chronicle

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.Optional
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.openlattice.chronicle.preferences.EnrollmentSettings
import com.openlattice.chronicle.preferences.getDevice
import com.openlattice.chronicle.preferences.getDeviceId
import com.openlattice.chronicle.services.upload.PRODUCTION
import com.openlattice.chronicle.services.upload.createRetrofitAdapter
import com.openlattice.chronicle.utils.Utils
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.Executors

class Enrollment : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val mHandler = object : Handler(Looper.getMainLooper()) {}
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data

        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val studyIdText = findViewById<EditText>(R.id.studyIdText)
            val participantIdText = findViewById<EditText>(R.id.participantIdText)
            val studyId = appLinkData.getQueryParameter("studyId")
            val participantId = appLinkData.getQueryParameter("participantId")
            studyIdText.setText(studyId)
            participantIdText.setText(participantId)
        }
    }

    fun enrollDevice(view: View) {
        doEnrollment()
    }

    fun handleOnClickDone(view: View) {
        doMainActivity(this)
        finish()
    }

    private fun validateInput(orgId: String, studyId: String, participantId: String): String {
        var errorMessage = ""
        if (orgId.isBlank()) {
            errorMessage = getString(R.string.invalid_org_id_blank)
        } else if (!Utils.isValidUUID(orgId)) {
            errorMessage = getString(R.string.invalid_org_id_format)
        } else if (studyId.isBlank()) {
            errorMessage = getString(R.string.invalid_study_id_blank)
        } else if (!Utils.isValidUUID(studyId)) {
            errorMessage = getString(R.string.invalid_study_id_format)
        } else if (participantId.isBlank()) {
            errorMessage = getString(R.string.invalid_participant)
        }

        return errorMessage;
    }

    private fun doEnrollment() {

        val orgIdText = findViewById<EditText>(R.id.orgIdText)
        val studyIdText = findViewById<EditText>(R.id.studyIdText)
        val participantIdText = findViewById<EditText>(R.id.participantIdText)
        val statusMessageText = findViewById<TextView>(R.id.statusMessage)
        val progressBar = findViewById<ProgressBar>(R.id.enrollmentProgress)
        val submitBtn = findViewById<Button>(R.id.button)
        val doneBtn = findViewById<Button>(R.id.doneButton)

        val orgIdStr: String = orgIdText.text.toString().trim()
        val studyIdStr: String = studyIdText.text.toString().trim()
        val participantId: String = participantIdText.text.toString().trim()

        val errorMessage = validateInput(orgIdStr, studyIdStr, participantId)
        if (errorMessage.isNotBlank()) {
            // validation failed
            statusMessageText.text = errorMessage
            statusMessageText.visibility = View.VISIBLE
            return
        }

        try {
            val orgId = UUID.fromString(orgIdStr)
            val studyId = UUID.fromString(studyIdStr)
            val deviceId = getDeviceId(applicationContext)

            statusMessageText.visibility = View.INVISIBLE
            submitBtn.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE

            executor.execute {
                val chronicleStudyApi = createRetrofitAdapter(PRODUCTION).create(ChronicleStudyApi::class.java)

                var chronicleId: UUID? = null
                try {
                    chronicleId = chronicleStudyApi.enrollSource(studyId, participantId, deviceId, Optional.of(getDevice(deviceId)))
                } catch (e: Exception) {
                    crashlytics.log("caught exception - orgId: \"$orgId\" ; studyId: \"$studyId\" ; participantId: \"$participantId\"")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }

                // TODO: actually retrieve device id
                if (chronicleId != null) {
                    Log.i(javaClass.canonicalName, "Chronicle id: " + chronicleId.toString())
                    mHandler.post {
                        val enrollmentSettings = EnrollmentSettings(applicationContext)

                        enrollmentSettings.setStudyId(studyId)
                        enrollmentSettings.setParticipantId(participantId)

                        // hide text fields, progress bar, and enroll button
                        studyIdText.visibility = View.GONE
                        participantIdText.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        submitBtn.visibility = View.GONE
                        // show success message and done button
                        statusMessageText.text = getString(R.string.device_enroll_success)
                        statusMessageText.visibility = View.VISIBLE
                        doneBtn.visibility = View.VISIBLE
                    }
                } else {
                    crashlytics.log("unable to enroll device - studyId: \"$studyId\" ; participantId: \"$participantId\"")
                    Log.e(javaClass.canonicalName, "unable to enroll device.")
                    mHandler.post {
                        progressBar.visibility = View.INVISIBLE
                        submitBtn.visibility = View.VISIBLE
                        doneBtn.visibility = View.INVISIBLE
                        statusMessageText.visibility = View.VISIBLE
                        statusMessageText.text = getString(R.string.device_enroll_failure)
                    }
                }
            }
        } catch (e: Exception) {
            statusMessageText.text = getString(R.string.device_enroll_failure)
            statusMessageText.visibility = View.VISIBLE
            submitBtn.visibility = View.VISIBLE
            doneBtn.visibility = View.INVISIBLE
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e( javaClass.canonicalName, "unable to enroll", e)
        }
    }
}
