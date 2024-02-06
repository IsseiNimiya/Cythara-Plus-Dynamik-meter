package com.github.cythara.tuning;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.widget.TextViewCompat;

/**Activity2への移行後、使用停止*/
public class SoundLevelMeter implements Runnable{
    private static final int SAMPLE_RATE=8000;
    /**サンプリング周波数の値を高くしてしまうと bufferSizeが大きくなってしまい、
     * アプリ全体に負荷をかけてしまうことに繋がる。44100Hzは実用的ではないレベルと思われる。*/

    public static int bufferSize;
    public static AudioRecord audioRecord;
    private static boolean isRecording;
    private static boolean isPausing;
    private static double baseValue;
    private static int read;
    private int dB_min=50;
    private int dB_max=0;

    private static boolean SUFF=true;
    private static boolean SUP=true;
    private static boolean SUMP=true;
    private static boolean SUF=true;
    private static boolean SUMF=true;
    private static boolean SUPP=true;
    private int debug=0;

    private static double PiaNis=25.0;/**測定自体にはあまり意味がないかもしれない。ピアニッシモの練習に使えるだろうか？*/
    private static double Pia= 30.0;
    private static double MezPia=45.0;
    private static double MezFor=60.0;
    private static double For=75.0;
    private static double ForTis=90.0;

    private int MMaxValue=0;

    public interface SoundLevelMeterListener {
        void onMeasure(double db);
    }

    private SoundLevelMeterListener listener;
    /**インターフェイスにlistenerと名前を付けている。*/

    public SoundLevelMeter() {
        /**コンストラクター*/
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        /**サンプリング周波数を大きくしてしまうと、間接的にbufferSizeが大きくなってしまう*/

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                /**ここのAudioSource.~がマイクの入力ソースを指定する部分
                 * MICが電話用マイク・CAMCORDERが下部のサブマイク・DEFAULTなどはMICを指定するものが多数*/
                SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        /**audioRecordをインスタンス化*/

        listener = null; /**インターフェイス=null 初期化を行っているのだろうか？*/
        isRecording = true;/**ここがfalseならチューナーの画面に移行する。runメソッドを呼び出し続けてしまうので問題となる*/
        baseValue = 1.0;
        /**12.0の数値は騒音計が示す音圧レベルと近似する値を探し出し、手作業で調整した物とのこと
         * 1.0（等倍）に落とし込むとそこそこの誤差に落ち着くが、コンプレッサのように余裕が出る訳ではないので...*/
        pause();
    }

    public void setListener(SoundLevelMeterListener l) {listener = l;}
    /**このメソッドの存在意義は？*/

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        RESUME();
        /**suspendメソッドがないため記述は不要なはず*/
        short[] buffer = new short[bufferSize];
        /**.read()の1つ目の引数はshort型である必要があるそうだ*/
        /**この時点では宣言を行っているに過ぎないので、bufferの値は全て0*/
        while (isRecording) {
            if (!isPausing) {
                debug+=1;
                read = audioRecord.read(buffer, 0, bufferSize);

                Log.d("CompareTest2","["+debug+"]"+"read=audioRecord.read("
                        +(int)buffer[0]+",0,"+bufferSize+");");
                //第1引数のbufferの書替えに異常:
                //buffer[0]~[639]の全てで初期代入値の書替えに失敗している

                /**audioRecordは同名型のインスタンス
                 * readメソッドがbufferにデータをハードウェアから記録しているらしい。
                 * buffer[i]が可変で、ある程度変化するまでに時間を要しているのは、この仕様のせいだと思われる。*/
                /**readの値は可変であるようだ。3584→640のように変化。bufferSize=readの関係性も見られる*/
                if (read < 0) {
                    throw new IllegalStateException();
                }
                int maxValue = 1;
                /**-Infinityを防止するため*/
                /**read=100;1111111111111111111111111111111111111111111111111111111111111111111111111*/
                //Log.d("CompareTest","Line 152 passed!");
                for (int i = 0; i < read; i++) {
                    maxValue = Math.max(maxValue, buffer[i]);
                    Log.d("CompareTest","buffer["+i+"]is "+buffer[i]);
                    /**buffer[i]が異常な動作（値が0になる動作）をしている
                     * 以前のbuffer[i]は可変だったはず.*/
                    if(maxValue>MMaxValue){
                        MMaxValue=maxValue;
                    }
                    /**2つめの引数はbuffer[0]からbuffer[read-1]まで動作*/
                    /**int型のmaxValueとshort型のbufferを比較して、maxValueを更新している*/
                    
                    Log.d("CompareTest","now dB="+ ((20.0 * Math.log10(maxValue / baseValue))+11.5));
                    /**理由は不明なものの定数だけ綺麗にズレが確認できている。pixel環境特有のコンプでもついているのだろうか？*/

                    /**buffer[i]が正の値をとらない限り、maxValueが増えることはない
                     * bufferはread（厳密には...native_read_in_short_array）によって
                     * スレッドが呼び出される度に640回書き換えられている。*/
                }

                double db = (20.0 * Math.log10(maxValue / baseValue))+11.5;
                //SoundLevelComparetor(db);/**SoundLevelCompareの呼び出し！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！*/

                if(SUFF==false){
                    setForTis(db);
                }
                if(SUF==false) {
                    setFor(db);
                    /**
                     * 1:データ入力完了のボタンを表示
                     * 2:ボタンの押下監視アルゴリズム（押下後にSUFをtrueにする）
                     * 3:（可能なら）SUPもしくはSUMFをfalseにする
                     * 以下各関数で同様に処理する*/
                    /**if(Tester==1000){//テスト用!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        SUF=true;
                    }*/
                }
                if(SUP=false){
                    setPia(db);
                }
                if(SUMF=false){
                    setMezFor(db);
                }
                if(SUMP=false){
                    setMezPia(db);
                }
                if(SUPP=false){
                    setPiaNis(db);
                }
                //Log.d("test2","now dB:"+db);

                /**20*log10(maxValue/12.0)*/
                /**20*log10(実測値/基準値）が音の出力レベルの公式*/
                /**log10(maxValue/12.0)<=3.4 なぜ？*/
                /**(maxValue/12.0)<=2511.88...*/
                /**maxValue<=30142.56 当然double型の最大値よりは小さい*/

                //もはや不要の子
                if(db>dB_max){
                    dB_max=(int)db;
                }
                if(db>0&&db<dB_min){
                    dB_min=(int) db;
                }
                Log.d("SoundLevelMeter", "now:" +db+" min:"+dB_min+
                        " max:"+dB_max);//(int)
                if (listener != null) {
                    listener.onMeasure(db);
                    /**何のために書かれているんだ？ setListenerメソッドが呼び出されないと意味が出ないような...*/
                }
            }
            try {
                Thread.sleep(200);
                /**200ミリ秒がデフォ 200ミリ秒ごとにwhileのブロックを呼び出している*/
                /**200ミリ秒ごとに640回音声のデータを記録している。1ミリ秒あたり3回くらい記録している。*/
                /**　→native_read_in_short_arrayの定義や仕様が分かればなぁ...*/
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }/**ここまでがスレッドによって呼び出される。再帰的にrunメソッドが呼び出されている*/
        //audioRecord.stop();
        audioRecord.release();
    }

    /**public void start(){
        isRecording=true;
    }*/
    /**これじゃだめか...*/

    public void STOP(){
        isRecording=false;
    }

    public void pause() {
        if (!isPausing)
            audioRecord.stop();
        isPausing = true;
        isRecording=false;
    }

    public void RESUME(){
        if(isPausing)
            audioRecord.startRecording();
        isPausing=false;
    }

    public void Running(){
        if(isPausing){
            isPausing=false;
            isRecording=true;
        }
    }

    public void setPiaNis(double PiaNis){
        this.PiaNis=PiaNis;
    }
    public void setPia(double Pia){
        this.Pia=Pia;
    }
    public void setMezPia(double MezPia){
        this.MezPia=MezPia;
    }
    public void setMezFor(double MezFor){
        this.MezFor=MezFor;
    }
    public void setFor(double For){
        this.MezFor=For;
        Log.d("SetupTest","Set OK! now For="+For);
    }
    public void setForTis(double ForTis){
        this.ForTis=ForTis;
    }

    public int SoundLevelComparetor(double dB){
        if(dB>=ForTis){
            Log.d("CompareTest","now SoundLevel is ff [debug:"+dB+">="+ForTis+"]");
        }
        else if(dB>=For){
            Log.d("CompareTest","now SoundLevel is f [debug:"+ForTis+">"+dB+">="+For+"]");
        }
        else if(dB>=MezFor){
            Log.d("CompareTest","now SoundLevel is mf [debug:"+For+">"+dB+">="+MezFor+"]");
        }
        else if(dB<=MezPia){
            Log.d("CompareTest","now SoundLevel is mp [debug:"+MezFor+">"+dB+">="+MezPia+"]");
        }
        else if(dB<=Pia){
            Log.d("CompareTest","now SoundLevel is p [debug:"+MezPia+">"+dB+">="+Pia+"]");
        }
        else if(dB<=PiaNis){
            Log.d("CompareTest","now SoundLevel is p [debug:"+Pia+">"+dB+">="+PiaNis+"]");
        }

        return 0;
    }

    /**   public void setUp(double db){
     setFor(db);
     Log.d("SetupTest","Set OK! now For="+db);
     //setMezFor();
     //setMezPia();
     //setPia();
     }*/
    /**runメソッド内でこのやり方を用いてセットするのはよくないのか*/

}
