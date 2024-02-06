package com.github.cythara;

public interface Note {
    /**音名インターフェイス*/

    NoteName getName();
    /**チューニングを行っている楽器の名前を取得するゲッタ（骨組）*/

    int getOctave();
    /**チューニング段階のオクターブを取得するゲッタ*/

    String getSign();
}
