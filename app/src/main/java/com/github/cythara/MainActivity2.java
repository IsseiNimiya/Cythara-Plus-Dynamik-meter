package com.github.cythara;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    public static int bufferSize;
    public static AudioRecord audioRecord;
    private static boolean RecordingFlag;
    private static boolean SetReady,SetModeFlag;
    private static double baseValue;
    private static int read;
    private int TestModeFlag1 = 0;
    private boolean TestModeFlag2,TestModeFlag3;
    private int ID=-1;/**ラジオボタンでIDを取得し、音量設定分岐に使用する変数*/

    private SharedPreferences data;
    private int MMaxValue = 0;
    private double db = 1.0;

    private double ForTis;
    private double For;
    private double MezFor;
    private double MezPia;
    private double Pia;
    private double PiaNis;

    private int ForTis1,ForTis2,ForTis3;
    private int For1,For2,For3;
    private int MezFor1,MezFor2,MezFor3;
    private int MezPia1,MezPia2,MezPia3;
    private int Pia1,Pia2,Pia3;
    private int PiaNis1,PiaNis2,PiaNis3;

    String SoundLevelMessage = "None";
    double Tester = 0;
    private static final int SAMPLE_RATE = 8820;

    private boolean SetMessage = false;
    private ProgressBar SoundLevelBer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SoundLevelBer=(ProgressBar)findViewById(R.id.SoundLevelBer);
        SoundLevelBer.setMax(100);
        SoundLevelBer.setProgress(0);
        SoundLevelBer.setMin(0);

        LoadSoundLevels();
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        Switch toggle = findViewById(R.id.switch1);
        toggle.setOnCheckedChangeListener(new onCheckedChangeListener());

        TextView Integer_dB=findViewById(R.id.textView2);
        TextView TestSoundLevel=findViewById(R.id.textView4);

        Button button2 = findViewById(R.id.button2);

        /**ラジオボタンの実装。ここでIDとラジオボタンの結びつけを行っている*/
        RadioGroup Group = (RadioGroup) findViewById(R.id.RadioGroup);
        Group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioGroup == Group) {
                    switch (i) {
                        case R.id.RadioButton1:
                            ID = 0;
                            break;
                        case R.id.RadioButton2:
                            ID = 1;
                            break;
                        case R.id.RadioButton3:
                            ID = 2;
                            break;
                        case R.id.RadioButton4:
                            ID = 3;
                            break;
                        case R.id.RadioButton5:
                            ID = 4;
                            break;
                        case R.id.RadioButton6:
                            ID = 5;
                    }
                }
            }
        });

        SetReady=false;
        TestModeFlag1=0;
        TestModeFlag2=false;
        TestModeFlag3=true;

        /**audioRecordをインスタンス化*/
        /**音量測定（録音）の下処理*/
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                /**ここのAudioSource.~がマイクの入力ソースを指定する部分
                 * MICが電話用マイク・CAMCORDERが下部のサブマイク・DEFAULTなどはMICと同じ*/
                SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        baseValue = 1.0;
        final Handler handler=new Handler();

        /**dBの常時計測を実装*/
        Thread th =new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                short[] buffer = new short[bufferSize];
                RecordingFlag = true;

                while (RecordingFlag) {
                    read = audioRecord.read(buffer, 0, bufferSize);
                    /**audioRecordは同名型のインスタンス
                     * readメソッドがbufferにデータをハードウェアから記録している。
                     * buffer[i]が可変である程度変化するまでに時間を要しているのは、この仕様があるため。*/
                    if (read < 0) {
                        throw new IllegalStateException();
                    }
                    int maxValue = 1;
                    /**-Infinityを防止するため*/
                    for (int i = 0; i < read; i++) {
                        maxValue = Math.max(maxValue, buffer[i]);
                        if (maxValue > MMaxValue) {
                            MMaxValue = maxValue;
                        }
                        /**2つめの引数はbuffer[0]からbuffer[read-1]まで動作*/
                        /**int型のmaxValueとshort型のbufferを比較して、maxValueを更新している*/
                    }
                    db = (20.0 * Math.log10(maxValue / baseValue)) + 9.0;

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    /**Text変更用のHandlerクラス。ThreadクラスやRunnableクラスを用いて
                     * setTextメソッドを呼び出すと、Androidの「UI部品の操作はUIスレッドから行わなければならない」
                     * という制約に抵触し、エラー落ちする。その回避のためにHandlerクラスを用いて処理している。*/

                    handler.post(new Runnable() { /**UIの変更はhandler以下で行うこと*/
                        int Runnable_ID;

                        @Override
                        public void run() {
                            Integer_dB.setText(String.valueOf((int)db));
                            SoundLevelMover(SoundLevelComparator(db),db);
                            Runnable_ID=SoundLevelComparator(db);
                                TestSoundLevel.setText("  ");
                                /**平常時の下部ボタン表示を実装*/
                                button2.setText(SetReady ? "タップで記録！" : "録音準備！");
                        }
                    });
                }
            }
        });
        th.start();
    }

    /**トグルの実装*/
    public class onCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(TestModeFlag1<11){
                TestModeFlag1+=1;
            }
            if(isChecked){
                SetModeFlag=true;
                SetReady=false;
            }else{
                SetModeFlag=false;
            }
        }
    }

    /**ボタンの押下処理を実装*/
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button1) {
            /**チューニングメータへの画面遷移を実装*/
            finish();

        }else if(view.getId()==R.id.button2){
            /**音量の記録処理、下部ボタンのラベル変更を実装*/
            if(TestModeFlag1==10 && TestModeFlag3){
                TestModeFlag2=true;
                TestModeFlag3=false;
            }else{
                if(SetModeFlag){
                SetReady=!SetReady;
                }
                if(SetReady){
                    SaveSoundLevels();
                }
            }
        }
    }

    /**各強弱記号用のセッタ*/
    private void setPiaNis(double PiaNis) {
        this.PiaNis = PiaNis;
    }
    private void setPia(double Pia) {
        this.Pia = Pia;
    }
    private void setMezPia(double MezPia) {
        this.MezPia = MezPia;
    }
    private void setMezFor(double MezFor) {
        this.MezFor = MezFor;
    }
    private void setFor(double For) {this.For = For;}
    private void setForTis(double ForTis) {this.ForTis = ForTis;}

    /**以下は音量設定の読み書き、音量表示・メータ表示のための数値代入と実装部分*/
    /**保存してある基準音圧レベルを端末から読み込むためのメソッド*/
    private void LoadSoundLevels(){
        data=getSharedPreferences("DataSave", Context.MODE_PRIVATE);

        ForTis=data.getFloat("ff",0);
        For=data.getFloat("f",0);
        MezFor=data.getFloat("mf",0);
        MezPia=data.getFloat("mp",0);
        Pia=data.getFloat("p",0);
        PiaNis=data.getFloat("pp",0);


        Log.d("Test_SL","Debug now ff is "+ForTis);
        Log.d("Test_SL","Debug now f is "+For);
        Log.d("Test_SL","Debug now mf is "+MezFor);
        Log.d("Test_SL","Debug now mp is "+MezPia);
        Log.d("Test_SL","Debug now p is "+Pia);
        Log.d("Test_SL","Debug now pp is "+PiaNis);


        SetSoundLevels();
    }
    /**基準音圧レベルを端末内に保存するメソッド*/
    private void SaveSoundLevels(){
        SharedPreferences.Editor edit = data.edit();
       // Log.d("CompareTest4", "now SetMassage is " + SetMessage);
        final EditText BaseSoundLevel=findViewById(R.id.edit_BaseSoundLevel);

        BaseSoundLevel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                /**処理は書かない*/
            }
        });

        if (SetModeFlag) {
            if (RecordingFlag) {
                if(SetReady) {
                    String SdB = BaseSoundLevel.getText().toString();
                    switch (ID) {
                        case 0:
                         //   Log.d("Test_SL", "ffを設定します。");
                            if(SdB.equals("")){
                                final double DebugForTis = 97.0;

                                setForTis(DebugForTis);
                                edit.putFloat("ff", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("ff", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "ff";
                            Tester = ForTis;
                            break;
                        case 1:
                            if(SdB.equals("")) {
                                final double DebugFor = 94.0;

                           //     Log.d("Test_SL", "fを設定します。");
                                setFor(DebugFor);
                                edit.putFloat("f", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("f", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "f";
                            Tester = For;
                            break;
                        case 2:
                            //Log.d("Test_SL", "mfを設定します。");
                            if(SdB.equals("")) {
                                final double DebugMezFor = 90.0;

                                setMezFor(DebugMezFor);
                                edit.putFloat("mf", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("mf", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "mf";
                            Tester = MezFor;
                            break;
                        case 3:
                            //Log.d("Test_SL", "mpを設定します。");
                            if(SdB.equals("")) {
                                final double DebugMezPia = 88.0;
                                setMezPia(DebugMezPia);
                                edit.putFloat("mp", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("mp", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "mp";
                            Tester = MezPia;
                            break;
                        case 4:
                           // Log.d("Test_SL", "pを設定します。");
                            if(SdB.equals("")) {
                                final double DebugPia = 85.5;

                                setPia(DebugPia);
                                edit.putFloat("p", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("p", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "p";
                            Tester = Pia;
                            break;
                        case 5:
                          //  Log.d("Test_SL", "ppを設定します。");
                            if(SdB.equals("")) {
                                final double DebugPiaNis = 83.5;
                                setPiaNis(DebugPiaNis);
                                edit.putFloat("pp", (float) db);
                                edit.apply();
                            }else{
                                setForTis(Double.parseDouble(SdB));
                                edit.putFloat("pp", (float) Double.parseDouble(SdB));
                                edit.apply();
                            }
                            SoundLevelMessage = "pp";
                            Tester = PiaNis;
                    }
                }else{
                   // Log.d("Test_SL", SoundLevelMessage + "の音量を書き込みました！");
                }
            }
        }
    }
    /**現在の音圧レベルがどの強弱記号に該当するかを判定するメソッド*/
    private int SoundLevelComparator(double dB) {
        TextView SoundLevel=findViewById(R.id.textView);
        if (dB >= ForTis) {
            SoundLevel.setText("ff");
            return 1;
        } else if (dB >= For) {
            SoundLevel.setText("f");
            return 2;
        } else if (dB >= MezFor) {
            SoundLevel.setText("mf");
            return 3;
        } else if(dB<MezFor && dB>MezPia){;
            SoundLevel.setText("mid");
            return -1;
        } else if(dB<=PiaNis) {
            SoundLevel.setText("pp");
            return 6;
        } else if (dB <= Pia) {
            SoundLevel.setText("p");
            return 5;
        } else if (dB <= MezPia) {
            SoundLevel.setText("mp");
            return 4;
        }
        return 7; /**7を返している場合はエラー発生*/
    }
    /**サウンドレベルメータの25段階分けを設定するためのメソッド*/
    private void SetSoundLevels(){
        ForTis3=(int)ForTis+6;
        ForTis2=(int)ForTis+4;
        ForTis1=(int)ForTis+2;

        For3=(int)(For+((ForTis-For)/4)*3);
        For2=(int)(For+((ForTis-For)/4)*2);
        For1=(int)(For+((ForTis-For)/4));

        MezFor3=(int)(MezFor+((ForTis-MezFor)/4)*3);
        MezFor2=(int)(MezFor+((ForTis-MezFor)/4)*2);
        MezFor1=(int)(MezFor+((ForTis-MezFor)/4));

        MezPia3=(int)(MezPia-((MezPia-Pia)/4));
        MezPia2=(int)(MezPia-((MezPia-Pia)/4)*2);
        MezPia1=(int)(MezPia-((MezPia-Pia)/4)*3);

        Pia3=(int)(Pia-((Pia-PiaNis)/4));
        Pia2=(int)(Pia-((Pia-PiaNis)/4)*2);
        Pia1=(int)(Pia-((Pia-PiaNis)/4)*3);

        PiaNis3=(int)PiaNis-2;
        PiaNis2=(int)PiaNis-4;
        PiaNis1=(int)PiaNis-6;
    }
    /**サウンドレベルメータを漸次的に書き換えるメソッド*/
    private void SoundLevelMover(int SoundLevelNum,double dB){
        SoundLevelBer=(ProgressBar)findViewById(R.id.SoundLevelBer);
        switch(SoundLevelNum) {
            case 1:/**ffのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.RED));
                if (dB >= ForTis3) {
                    SoundLevelBer.setProgress(100, false);
                } else if (ForTis2 <= dB && dB < ForTis3) {
                    SoundLevelBer.setProgress(96, false);
                } else if (ForTis1 <= dB && dB < ForTis2) {
                    SoundLevelBer.setProgress(92, false);
                } else if (ForTis <= dB && dB < ForTis1) {
                    SoundLevelBer.setProgress(88,false);
                }
                break;
            case 2:/**fのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.rgb(255,165,0)));
                if (For3 <= dB && dB < ForTis) {
                    SoundLevelBer.setProgress(84, false);
                } else if (For2 <= dB && dB < For3) {
                    SoundLevelBer.setProgress(80, false);
                } else if (For1 <= dB && dB < For2) {
                    SoundLevelBer.setProgress(76, false);
                } else if (For <= dB && dB < For1) {
                    SoundLevelBer.setProgress(72, false);
                }
                break;
            case 3:/**mfのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                if (MezFor3 <= dB && dB < For) {
                    SoundLevelBer.setProgress(68, false);
                } else if (MezFor2 <= dB && dB < MezFor3) {
                    SoundLevelBer.setProgress(64, false);
                } else if (MezFor1 <= dB && dB < MezFor2) {
                    SoundLevelBer.setProgress(60, false);
                } else if (MezFor <= dB && dB < MezFor1) {
                    SoundLevelBer.setProgress(56, false);
                }
                break;

            case -1:/**midのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.LTGRAY));
                SoundLevelBer.setProgress(52, false);
                break;

            case 4:/**mpのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                if (MezPia >= dB && dB > MezPia3) {
                    SoundLevelBer.setProgress(48, false);
                } else if (MezPia3 >= dB && dB > MezPia2) {
                    SoundLevelBer.setProgress(44, false);
                } else if (MezPia2 >= dB && dB > MezPia1) {
                    SoundLevelBer.setProgress(40, false);
                } else if (MezPia1 >= dB && dB > Pia) {
                    SoundLevelBer.setProgress(36, false);
                }
                break;
            case 5:/**pのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.CYAN));
                if (Pia >= dB && dB > Pia3) {
                    SoundLevelBer.setProgress(32, false);
                } else if (Pia3 >= dB && dB > Pia2) {
                    SoundLevelBer.setProgress(28, false);
                } else if (Pia2 >= dB && dB > Pia1) {
                    SoundLevelBer.setProgress(24, false);
                } else if (Pia1 >= dB && dB > PiaNis) {
                    SoundLevelBer.setProgress(20, false);
                }
                break;
            case 6:/**ppのケース*/
                SoundLevelBer.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
                if (PiaNis >= dB && dB > PiaNis3) {
                    SoundLevelBer.setProgress(16, false);
                } else if (PiaNis3 >= dB && dB > PiaNis2) {
                    SoundLevelBer.setProgress(12, false);
                } else if (PiaNis2 >= dB && dB > PiaNis1) {
                    SoundLevelBer.setProgress(8, false);
                } else if (PiaNis1 >= dB && dB > PiaNis) {
                    SoundLevelBer.setProgress(4, false);
                } else if (PiaNis > dB) {
                    SoundLevelBer.setProgress(0, false);
                }
        }
    }
}
