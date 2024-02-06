package com.github.cythara.tuning;

import static com.github.cythara.NoteName.A;
import static com.github.cythara.NoteName.C;
import static com.github.cythara.NoteName.E;
import static com.github.cythara.NoteName.G;

import com.github.cythara.Note;
import com.github.cythara.NoteName;
import com.github.cythara.Tuning;

public class BrassBandTuning implements Tuning {
    @Override
    public Note[] getNotes() {
        return BrassBandTuning.Pitch.values();
    }

    @Override
    public Note findNote(String name) {
        return BrassBandTuning.Pitch.valueOf(name);
    }

    private enum Pitch implements Note {

        A0(A,0),
        G1(G, 1), C1(C, 1), E1(E, 1), A1(A, 1),
        G2(G, 2), C2(C, 2), E2(E, 2), A2(A, 2),
        G3(G, 3), C3(C, 3), E3(E, 3), A3(A, 3),
        G4(G, 4), C4(C, 4), E4(E, 4), A4(A, 4),
        G5(G, 5), C5(C, 5), E5(E, 5), A5(A, 5),
        G6(G, 6), C6(C, 6), E6(E, 6), A6(A, 6),
        G7(G, 7), C7(C, 7), E7(E, 7), A7(A, 7),
        C8(C,8);

        private final String sign;
        private final int octave;
        private NoteName name;

        Pitch(NoteName name, int octave) {
            this.name = name;
            this.octave = octave;
            this.sign = "";
        }

        public NoteName getName() {
            return name;
        }

        @Override
        public int getOctave() {
            return octave;
        }

        @Override
        public String getSign() {
            return sign;
        }
    }
}
