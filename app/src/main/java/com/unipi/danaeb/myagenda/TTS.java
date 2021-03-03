package com.unipi.danaeb.myagenda;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

// Class for TextToSpeech
public class TTS {
    private TextToSpeech tts;
    private TextToSpeech.OnInitListener initListener=
            new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) { tts.setLanguage(Locale.forLanguageTag("ENG")); }
            };
    public TTS(Context context) { tts = new TextToSpeech(context, initListener); }

    public void speak(String message){
        tts.speak(message,TextToSpeech.QUEUE_ADD,null,null);
    }
}
