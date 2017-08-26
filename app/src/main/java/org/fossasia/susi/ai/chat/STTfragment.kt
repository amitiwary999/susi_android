package org.fossasia.susi.ai.chat

import android.support.v4.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_stt.*
import org.fossasia.susi.ai.R
import org.fossasia.susi.ai.chat.contract.IChatPresenter



/**
 * Created by meeera on 26/8/17.
 */
class STTfragment: Fragment() {
    lateinit var recognizer: SpeechRecognizer
    lateinit var chatPresenter: IChatPresenter
    lateinit var recognition_view: RecognitionProgressView

    val barColors = intArrayOf(Color.parseColor("#4184f3"),
            Color.parseColor("#BDBDBD"),
            Color.parseColor("#0000D4"),
            Color.parseColor("#78909C"),
            Color.parseColor("#0091EA"))

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        chatPresenter = ChatPresenter(activity as ChatActivity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.fragment_stt, container, false)
        recognition_view = rootView?.findViewById(R.id.recognition_view) as RecognitionProgressView
        recognizer = SpeechRecognizer
                .createSpeechRecognizer(activity.applicationContext)
        (activity as ChatActivity).fabsetting.hide()
        promptSpeechInput()
        return rootView
    }

    fun promptSpeechInput() {
        recognition_view.setSpeechRecognizer(recognizer)

        recognition_view.setRecognitionListener(object : RecognitionListenerAdapter() {
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
                (activity as ChatActivity).setText(voiceResults[0])
                recognizer.destroy()
                if ( (activity as ChatActivity).recordingThread != null ) {
                    chatPresenter.startHotwordDetection()
                }
                (activity as ChatActivity).fabsetting.show()
                activity.supportFragmentManager.popBackStackImmediate()
            }

            override fun onPartialResults(partialResults: Bundle) {
                val partial = partialResults
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                txtchat?.text = partial[0]
            }

            override fun onError(error: Int) {
                Log.d("fragment",
                        "Error listening for speech: " + error)
                Toast.makeText(activity.applicationContext, "Could not recognize speech, try again.", Toast.LENGTH_SHORT).show()
                recognizer.destroy()
                (activity as ChatActivity).fabsetting.show()
                activity.supportFragmentManager.popBackStackImmediate()
            }

        })
        recognition_view.setColors(barColors)
        recognition_view.play()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app")
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizer.startListening(intent)
    }
    override fun onPause() {
        super.onPause()
        (activity as ChatActivity).fabsetting.show()
        recognition_view.stop()
        recognizer.cancel()
        recognizer.destroy()
    }

}