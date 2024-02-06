package com.github.cythara.tuning;

import android.util.Log;

import com.github.cythara.Note;

import java.util.Arrays;
import java.util.List;

public class NoteFrequencyCalculator {
    //音の周波数を取得する計算表

    private static List<String> notes =
            Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private float referenceFrequency;
    /**参照周波数をfloat型で取得している*/

    public NoteFrequencyCalculator(float referenceFrequency) {
        this.referenceFrequency = referenceFrequency;
    }

    public double getFrequency(Note note) {
        int semitonesPerOctave = 12;
        //1オクターブ当たりの半音数
        int referenceOctave = 4;
        /**参照オクターブ数：何故4なんだ...？*/
        double distance = semitonesPerOctave * (note.getOctave() - referenceOctave);
       // Log.i("distance_debug", String.valueOf(distance));

        //getOctave()は1~9→0→-1→1の周期をとっている

        distance += notes.indexOf(note.getName() + note.getSign()) - notes.indexOf("A");

        return referenceFrequency * Math.pow(2, distance / 12);
    }
}