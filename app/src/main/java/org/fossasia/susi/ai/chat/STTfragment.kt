package org.fossasia.susi.ai.chat

import ai.kitt.snowboy.audio.RecordingThread
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_sttframe.*
import org.fossasia.susi.ai.R
import org.fossasia.susi.ai.helper.Constant
import org.fossasia.susi.ai.helper.PrefManager

/**
 * Created by meeera on 17/8/17.
 */
class STTfragment : Fragment() {
    override fun onPause() {
        super.onPause()
        if (recordingThread != null && isDetectionOn) {
            recordingThread?.stopRecording()
            isDetectionOn = false
        }
    }


    lateinit var recognizer: SpeechRecognizer
    var msg:String?= null
    private var recordingThread: RecordingThread? = null
    private var isDetectionOn = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.fragment_sttframe, container, false)
        (activity as ChatActivity).fabsetting.visibility = View.GONE
        promptSpeechInput()
        return rootView
    }

    fun checkHotwordPref(): Boolean {
        return PrefManager.getBoolean(Constant.HOTWORD_DETECTION, false)
    }
    private fun promptSpeechInput() {
        if (recordingThread != null && isDetectionOn) {
            recordingThread?.stopRecording()
            isDetectionOn = false
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app")
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        recognizer = SpeechRecognizer
                .createSpeechRecognizer(activity.getApplicationContext())
        val listener = object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (voiceResults == null) {
                    Log.e("fragment", "No voice results")
                } else {
                    Log.d("fragment", "Printing matches: ")
                    for (match in voiceResults) {
                        Log.d("fragment", match)
                    }
                }
                if (speechprogress != null)
                    speechprogress.onResultOrOnError();
                //  sendMessage(voiceResults!![0], voiceResults[0])
                recognizer.destroy()
                (activity as ChatActivity).setText(msg.toString())
                (activity as ChatActivity).fabsetting.visibility = View.VISIBLE
                activity.supportFragmentManager.popBackStackImmediate()
                if (recordingThread != null && !isDetectionOn && checkHotwordPref()) {
                    recordingThread?.startRecording()
                    isDetectionOn = true
                }
            }

            override fun onReadyForSpeech(params: Bundle) {
                Log.d("fragment", "Ready for speech")
            }

            override fun onError(error: Int) {
                Log.d("fragment",
                        "Error listening for speech: " + error)
                if (speechprogress != null)
                    speechprogress.onResultOrOnError()
                Toast.makeText(activity.getApplicationContext(), "Could not recognize speech, try again.", Toast.LENGTH_SHORT).show()
                recognizer.destroy()
                (activity as ChatActivity).fabsetting.visibility = View.VISIBLE
                activity.supportFragmentManager.popBackStackImmediate()
            }

            override fun onBeginningOfSpeech() {
                Log.d("fragment", "Speech starting")
                if (speechprogress != null)
                    speechprogress.onBeginningOfSpeech()
                //listening.text =
            }

            override fun onBufferReceived(buffer: ByteArray) {
                // This method is intentionally empty
            }

            override fun onEndOfSpeech() {
                // This method is intentionally empty
                if (speechprogress != null)
                    speechprogress.onEndOfSpeech()
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                // This method is intentionally empty
            }

            override fun onPartialResults(partialResults: Bundle) {
                val partial = partialResults
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                msg = partial[0]
                txtchat.text = partial[0]
            }

            override fun onRmsChanged(rmsdB: Float) {
                // This method is intentionally empty
                if (speechprogress != null)
                    speechprogress.onRmsChanged(rmsdB)
            }
        }
        recognizer.setRecognitionListener(listener)
        recognizer.startListening(intent)
    }
}