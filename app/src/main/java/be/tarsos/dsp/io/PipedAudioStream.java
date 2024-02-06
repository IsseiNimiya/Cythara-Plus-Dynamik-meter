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

package be.tarsos.dsp.io;

import java.io.InputStream;

import be.tarsos.dsp.util.AudioResourceUtils;


/**
 * An audio file can be used to convert and read from.
 * オーディオファイルは変換して読み込むことができます。
 * It uses libAV to convert about any audio format to a one channel PCM stream of a chosen sample rate.
 * LibAVを使用して、あらゆるオーディオフォーマットを選択されたサンプリング周波数の1チャンネルPCMストリームに変換します。
 * @i)LIbAVって何だ？_1/10
 * @i)オーディオファイル用のコーデック（符号化と復号を行う処理のこと）用ライブラリーのことを指すらしい。_1/10
 * There is support for movie files as well, the first audio channel is then used as input.
 * ムービーファイルにも対応しており、その場合、最初のオーディオチャンネルが入力として使用されます。
 * The resource is either a local file or a type of stream supported by libAV (e.g. HTTP streams);
 * （使用できる）リソースはローカルファイルか、LibAVがサポートするストリームタイプ（HTTP ストリームなど）です。
 * 
 * For a list of audio decoders the following command is practical:
 * オーディオデコーダーの一覧は、以下のコマンドが実用的です。
 * <pre>
avconv -decoders | grep -E "^A" | sort
 

A... 8svx_exp             8SVX exponential
A... 8svx_fib             8SVX fibonacci
A... aac                  AAC (Advanced Audio Coding)
A... aac_latm             AAC LATM (Advanced Audio Coding LATM syntax)
...
 * </pre>
 */
public class PipedAudioStream {
	
	//private final static Logger LOG = Logger.getLogger(PipedAudioStream.class.getName());
	
	private final String resource;

	private static PipeDecoder pipeDecoder = new PipeDecoder();
	//PipeDecoderクラスのインスタンス化
	public static void setDecoder(PipeDecoder decoder){
		pipeDecoder = decoder;
	}
	//インスタンス化完了後、当該クラスそのものにセッターを用いて代入.PipeDecoderクラスの宣言がprivateである為
	
	private final PipeDecoder decoder;
	//インスタンス化はせず、宣言のみ.
	//PipeDecoderクラスはオーディオファイルをPCM,モノラル,16bit/sampleに変換するためのクラスとのこと
	public PipedAudioStream(String resource){
		this.resource = AudioResourceUtils.sanitizeResource(resource);
		//何らかの方法で取得したストリームファイルのパスをresourceにぶち込むための構文
		decoder = pipeDecoder;
	}
	
	/**
	 * Return a one channel, signed PCM stream of audio of a defined sample rate.
	 * 一つのチャンネルに定義されたサンプルレートの音声符号付きPCMストリームを返す
	 * @param targetSampleRate The target sample stream.
	 * パラメータ：targetSampleRate：対象となるサンプルストリーム
	 * @param startTimeOffset The start time offset.
	 * パラメータ：startTimeOffset:開始時間を相殺する（もの）
	 * @return An audio stream which can be used to read samples from.
	 * サンプルを読み込むために使用することができるオーディオストリームを返却します。
	 */
	public TarsosDSPAudioInputStream getMonoStream(int targetSampleRate,double startTimeOffset){
		return getMonoStream(targetSampleRate, startTimeOffset,-1);
		//再帰呼び出しを行っているのだろうか？
	}
	
	private TarsosDSPAudioFormat getTargetFormat(int targetSampleRate){
		return new TarsosDSPAudioFormat(targetSampleRate, 16, 1, true, false);
		//中でコンストラクタを呼び出して、それを返している.AudioFormatを作ろうとして呼び出しているのだろうけど、何故なのだろう？
	}


	/**
	 * Return a one channel, signed PCM stream of audio of a defined sample rate. 
	 * @param targetSampleRate The target sample stream.
	 * @param startTimeOffset The start time offset.
	 * @param numberOfSeconds the number of seconds to pipe. If negative the stream is processed until end of stream.
	 * パラメータ：numberOfSeconds:パイプの秒数。もし負の数をとる場合には、ストリームの終わりまで処理される
	 * @return An audio stream which can be used to read samples from.
	 */
	public TarsosDSPAudioInputStream getMonoStream(int targetSampleRate, double startTimeOffset,
			double numberOfSeconds) {
		//getMonoStreamのオーバーロード.一定時間が経過した段階（初回動作ではない状態）で場合分けを行うためにオーバーロードしているのかもしれない.
		InputStream stream = null;
		//Inputストリームクラスは連続するデータを読み込むためのクラスとのこと。ここではnullを入れて初期化している。
		stream = decoder.getDecodedStream(resource, targetSampleRate,startTimeOffset,numberOfSeconds);
		//上記変数にTarsosDSPライブラリのgetDecodedStream()を使用しているものと思われる.	メソッドの仕様を調べようとしているが、なかなか出てこない.　1/11
		return new UniversalAudioInputStream(stream, getTargetFormat(targetSampleRate));
	}
}
