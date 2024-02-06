package com.github.cythara.tuning;

import com.github.cythara.Note;
import com.github.cythara.NoteName;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NoteFrequencyCalculatorTest {
/**音名-周波数計算機テスト*/
    @Test
    public void TestCalc() {
        /**計算試験*/

        InputStream resourceAsStream = getClass().getResourceAsStream("note_frequencies.csv");
        /**csvファイルを読み込んで、それをreaderに流し込んでいる。
         * ...ということは、どこかでcsvファイルとして書き出していることになる。*/
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(resourceAsStream))) {
            while (reader.ready()) {
                /**readerの文字列が読み込み可能であるならば...*/
                String line = reader.readLine();
                /**readerを1行読み込む*/
                String[] components = line.split(",");
                /**「,」で文字列を分割する
                 * 何で文字列型「配列」なんだ？*/
                String noteWithOctave = components[0].split("/")[0];
                /**componentsの1つ目の要素を「/」で区切られる部分まで代入*/
                String frequency = components[1];
                /**componentsの2つ目の要素に周波数の文字列を代入*/

                String noteName = noteWithOctave.substring(0, 1);
                /**noteWithOctaveの0,1文字目を取得 0文字目の要素はアルファベット（音名）*/
                String octave = noteWithOctave.substring(1);
                /**noteWithOctaveの1文字目を取得　1文字目の要素はオクターブ数なのか？*/
                String sign = "";
                if (noteWithOctave.contains("#")) {
                    /**noteWithOctave内に「#」が含まれているならば...*/
                    noteName = noteWithOctave.substring(0, 1);
                    /**noteWithOctaveの0,1文字目を取得　0,1文字で音名を表す*/
                    octave = noteWithOctave.substring(2);
                    /**記号がある関係でオクターブ数は1文字横にシフトする*/
                    sign = "#";
                }

                /**上記処理の結果をfinal○○の変数に保存*/
                /**noteName：音名*/
                String finalNoteName = noteName;
                /**octave：オクターブ数のみ*/
                String finalOctave = octave;
                /**sign：最終的に表示させる音名+オクターブの形*/
                String finalSign = sign;

                Note note = new Note() {
                    @Override
                    public NoteName getName() {
                        return NoteName.fromScientificName(finalNoteName);
                    }

                    @Override
                    public int getOctave() {
                        return Integer.parseInt(finalOctave);
                        /**finalOctaveを整数クラス（≠組み込みの整数型）に変換している*/
                    }

                    @Override
                    public String getSign() {
                        return finalSign;
                    }
                    /**#もしくはbを取得するゲッタ*/
                };

                NoteFrequencyCalculator noteFrequencyCalculator =
                        new NoteFrequencyCalculator(440);
                //440Hzを参照周波数としてとっている。
                double expectedFrequency = Double.parseDouble(frequency);
                //String型で表記されている周波数をDouble型にキャストしている。expected：期待
                double actualFrequency = noteFrequencyCalculator.getFrequency(note);
                /***/

                /**何故期待値と実数値の両方を取得しているのだろう？*/

                Assert.assertEquals(expectedFrequency, actualFrequency, 0.01);

            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }

}