/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/


package be.tarsos.dsp;

import java.util.Arrays;

import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * An audio event flows through the processing pipeline.
 * オーディオイベントが処理パイプラインを流れる
 * The object is reused for performance reasons.
 * このオブジェクトはパフォーマンス上の理由から再利用される
 * The arrays with audio information are also reused, so watch out when using the buffer getter and setters. 
 * オーディオ情報の入った配列も再利用されるので、バッファのゲッターとセッターを使うときは注意しましょう。
 * @i)逆にいえば、ゲッタとセッタが用意されているということだろうか？
 *
 * @author Joren Six
 */
public class AudioEvent {
	/**
	 * The format specifies a particular arrangement of data in a sound stream.
	 *:フォーマットは、サウンドストリームのデータの特定の配置を指定します。
	 */
	private final TarsosDSPAudioFormat format;
	
	private final TarsosDSPAudioFloatConverter converter;
	
	/**
	 * The audio data encoded in floats from -1.0 to 1.0.
	 * オーディオデータは浮動小数点の-1.0から1.0の範囲でエンコードされます
	 */
	private float[] floatBuffer;
	
	/**
	 * The audio data encoded in bytes according to format.
	 * オーディオデータはbyte型でフォーマットされます
	 */
	private byte[] byteBuffer;
	
	/**
	 * The overlap in samples.
	 * サンプルの重なり具合
	 */
	private int overlap;
	
	/**
	 * The length of the stream, expressed in sample frames rather than bytes
	 * ストリームの長さ/　byte単位ではなく、サンプルフレーム数単位で表現される
	 */
	private long frameLength;
	
	/**
	 * The number of bytes processed before this event.
	 * このイベントの前に処理されたバイト数
	 * It can be used to calculate the time stamp for when this event started.
	 *　イベントが開始された際のタイムスタンプを計算するために使用することができる
	 * @i)スイッチ切り替え時の開始フレームを記録する為のものだろうか？
	 */
	private long bytesProcessed;

	private int bytesProcessing;
	
	
	public AudioEvent(TarsosDSPAudioFormat format){
		this.format = format;
		this.converter = TarsosDSPAudioFloatConverter.getConverter(format);
		this.overlap = 0;
	}
	
	public float getSampleRate(){
		return format.getSampleRate();
	}
	//サンプリング周波数を入手するためのゲッタ
	public int getBufferSize(){
		return getFloatBuffer().length;
	}
	//バッファサイズを入手するためのゲッタ

	/**
	 * @return  The length of the stream, expressed in sample frames rather than bytes
	 * ストリームの長さ/　byte単位ではなく、サンプルフレーム数単位で表現される
	 * @i)どうしてこれを繰り返し述べているのだろう？
	 */
	public long getFrameLength(){
		return frameLength;
	}
	//フレームの長さを入手するためのゲッタ
	public int getOverlap(){
		return overlap;
	}
	//サンプルの重なり具合のデータを入手するためのゲッタ？

	public void setOverlap(int newOverlap){
		overlap = newOverlap;
	}
	//サンプルの重なり具合を上書きするためのセッタ
	
	public void setBytesProcessed(long bytesProcessed){
		this.bytesProcessed = bytesProcessed;		
	}
	//イベント開始前のフレーム数を上書きするためのセッタ

	/**
	 * Calculates and returns the time stamp at the beginning of this audio event.
	 * オーディオイベント開始時点のタイムスタンプを計算し、返却する。
	 * @return The time stamp at the beginning of the event in seconds.
	 * イベント開始時点でのタイムスタンプ（秒）
	 */
	public double getTimeStamp(){
		return bytesProcessed / format.getFrameSize() / format.getSampleRate();
	}
	
	public double getEndTimeStamp(){
		return(bytesProcessed + bytesProcessing) / format.getFrameSize() / format.getSampleRate();
	}
	
	public long getSamplesProcessed(){
		return bytesProcessed / format.getFrameSize();
	}

	/**
	 * Calculate the progress in percentage of the total number of frames.
	 * 総コマ数に対する進捗率をパーセントで計算します
	 * @i)何のためにこの処理を行っているのだろう？
	 * 
	 * @return a percentage of processed frames or a negative number if the
	 *         number of frames is not known beforehand.
	 *	処理されたフレーム数のパーセンテージを指定します。
	 *	フレーム数が事前に不明である場合には負数を返します。
	 * @i)どうしてこの様な仕様が作られているのだろう？
	 */
	public double getProgress(){
		return bytesProcessed / format.getFrameSize() / (double) frameLength;
	}
	
	/**
	 * Return a byte array with the audio data in bytes.
	 *　音声データをバイト単位で表したバイト配列を返す。
	 *  A conversion is done from float, cache accordingly on the other side...
	 *  float型からの変換が行われ、一方でキャッシュしておく...
	 * 
	 * @return a byte array with the audio data in bytes.
	 *	音声データをバイト単位で格納したbyte型配列を返す
	 */
	public byte[] getByteBuffer(){
		int length = getFloatBuffer().length * format.getFrameSize();
		if(byteBuffer == null || byteBuffer.length != length){
			byteBuffer = new byte[length];
		}
		converter.toByteArray(getFloatBuffer(), byteBuffer);
		return byteBuffer;
	}
	
	public void setFloatBuffer(float[] floatBuffer) {
		this.floatBuffer = floatBuffer;
	}
	//Float型配列にバッファの値を入れるためのセッタ
	
	public float[] getFloatBuffer(){
		return floatBuffer;
	}
	//上の逆のゲッタ
	
	/**
	 * Calculates and returns the root mean square of the signal. Please
	 * cache the result since it is calculated every time.
	 * @return The <a
	 *         href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> of
	 *         the signal present in the current buffer.
	 */
	public double getRMS() {
		return calculateRMS(floatBuffer);
	}
	
	
	/**
	 * Returns the dBSPL for a buffer.
	 * 
	 * @return The dBSPL level for the buffer.
	 */
	public double getdBSPL() {
		return soundPressureLevel(floatBuffer);
	}
	
	/**
	 * Calculates and returns the root mean square of the signal. Please
	 * cache the result since it is calculated every time.
	 * @param floatBuffer The audio buffer to calculate the RMS for.
	 * @return The <a
	 *         href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> of
	 *         the signal present in the current buffer.
	 */
	public static double calculateRMS(float[] floatBuffer){
		double rms = 0.0;
		for (int i = 0; i < floatBuffer.length; i++) {
			rms += floatBuffer[i] * floatBuffer[i];
		}
		rms = rms / Double.valueOf(floatBuffer.length);
		rms = Math.sqrt(rms);
		return rms;
	}

	public void clearFloatBuffer() {
		Arrays.fill(floatBuffer, 0);
	}

		/**
	 * Returns the dBSPL for a buffer.
	 * 
	 * @param buffer
	 *            The buffer with audio information.
	 * @return The dBSPL level for the buffer.
	 */
	private static double soundPressureLevel(final float[] buffer) {
		double rms = calculateRMS(buffer);
		return linearToDecibel(rms);
	}
	
	/**
	 * Converts a linear to a dB value.
	 * 
	 * @param value
	 *            The value to convert.
	 * @return The converted value.
	 */
	private static double linearToDecibel(final double value) {
		return 20.0 * Math.log10(value);
	}

	public boolean isSilence(double silenceThreshold) {
		return soundPressureLevel(floatBuffer) < silenceThreshold;
	}

	public void setBytesProcessing(int bytesProcessing) {
		this.bytesProcessing = bytesProcessing;
		
	}
	
}
