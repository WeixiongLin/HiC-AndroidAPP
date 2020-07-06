package audioeffects;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.Tool.Function.AudioFunction;
import com.Tool.Function.FileFunction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

import io.github.ryanhoo.music.R;
import zty.composeaudio.Tool.Interface.DecodeOperateInterface;

import static com.Tool.Function.AudioFunction.convershort2wav;
import static com.Tool.Function.AudioFunction.convertPcm2Wav;
import static com.Tool.Function.AudioFunction.convertWav2Short;

import com.musicg.wave.Wave;



public class EditEffect {

    // 音效参数
    float p =  Integer.parseInt("5");
    float vibratoDelay = p/1000;
    float vibratoDepth = p/1000;
    float ModulationFrequency = 9;

    // source音乐
    InputStream sineInputStream;
    // 混响，啪的一声
    InputStream impulseInputStream;

    Wave inputWave;
    Wave impulseWave;

    short[] inputShort;
    short[] impulseShort;
    float[] input;
    float[] impulse;
    Context localcontext;

    public EditEffect(Context context){
        localcontext = context;
        sineInputStream = context.getResources().openRawResource(R.raw.x1);
        impulseInputStream = context.getResources().openRawResource(R.raw.reverb_impulse);
        inputWave = new Wave(sineInputStream);
        impulseWave = new Wave(impulseInputStream);
        inputShort = inputWave.getSampleAmplitudes();
        impulseShort = impulseWave.getSampleAmplitudes();
        input = Util.ShortsToFloatsNormalized(inputShort);
        impulse = Util.ShortsToFloatsNormalized(impulseShort);
    }

//    public EditEffect(Context context, String sourcepath) throws FileNotFoundException {
//        sineInputStream = new FileInputStream(sourcepath);
//        impulseInputStream = context.getResources().openRawResource(R.raw.reverb_impulse);
//        inputWave = new Wave(sineInputStream);
//        impulseWave = new Wave(impulseInputStream);
//        inputShort = inputWave.getSampleAmplitudes();
//        impulseShort = impulseWave.getSampleAmplitudes();
//        input = Util.ShortsToFloatsNormalized(inputShort);
//        impulse = Util.ShortsToFloatsNormalized(impulseShort);
//    }

    /***
     * 剪辑音频
     * @param sourcepath 源文件
     * @param temppath PCM临时文件
     * @param startsecond 起始剪辑时间
     * @param endsecond 结束剪辑时间
     * @param decodeOperateInterface 接口
     * @param context 上下文
     */
    public void CutAudio(String sourcepath, String temppath, String decodepath, int startsecond, int endsecond,
                         final DecodeOperateInterface decodeOperateInterface, Context context){
        // mp3片段转PCM临时文件
        mp3topcm(sourcepath, temppath, startsecond, endsecond, decodeOperateInterface);
        // 从PCM临时文件读到byte[]
        byte[] readfromPCM = new byte[0];
        FileOutputStream fileOutputStream = null;
//        try {
//            readfromPCM = AudioFunction.readfromPCM(temppath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // PCM临时文件写入WAV文件
        convertPcm2Wav(temppath, decodepath, 44100, 2, 8);
        // 这里8bit还是16bit不是随便选的，要与DecodeEngine.java第357行联合使用，否则会出现变速
    }

    /***
     * 把mp3文件转为PCM临时文件
     * @param sourcepath 源文件mp3
     * @param temppath PCM文件路径
     * @param startsecond 起始时间
     * @param endsecond 结束时间
     * @param decodeOperateInterface 接口
     */
    private void mp3topcm(String sourcepath, String temppath, int startsecond, int endsecond,
                          final DecodeOperateInterface decodeOperateInterface){
        // 剪辑，提取从beginsecond到endsecond的音频

        AudioFunction.DecodeMusicFile(sourcepath, temppath, startsecond, endsecond,
                decodeOperateInterface);
    }

    /***
     * 把一个MP3文件的片段读成short数组
     * @param mp3path MP3 路径
     * @param start 开始时间
     * @param end 结束时间
     * @param decodeOperateInterface 接口
     * @return MP3解码成的short数组
     */
    private short[] mp3toshort(String mp3path, int start, int end,
                               final DecodeOperateInterface decodeOperateInterface){
        String tempath = "";
        mp3topcm(mp3path, tempath, start, end, decodeOperateInterface);

        // 开始把PCM转short

        // 音频处理是否完成
        boolean firstAudioFinish = false;
        // 第一、二、合成音频的ByteBuffer数组
        byte[] AudioByteBuffer;
        short resultShort;
        int index;
        // 实际读进缓冲区的大小
        int AudioReadNumber;
        // 缓冲区预设大小，每次写1024byte
        final int byteBufferSize = 1024;
        AudioByteBuffer = new byte[byteBufferSize];
//        outputShortArray = new short[byteBufferSize / 2];

        // 一二音频输入流，合成音频输出流
//        FileInputStream firstAudioInputStream = FileFunction.GetFileInputStreamFromFile
//                (firstAudioFilePath);


        short[] shorts = new short[0];
        return shorts;
    }
    

    public static short[] ChangeVolume(String sourcepath, String decodepath, int start, int end, double rate){
        short[] shorts = convertWav2Short(sourcepath, 44100, 2, 16);
        for (int i=start*44100; i<end*44100 && i<shorts.length; i++){
            shorts[i] *= rate;
        }
        convershort2wav(shorts, decodepath, 44100, 2, 16);
        Log.e("lengthofshort", String.valueOf(shorts.length));
        return shorts;
    }


    /***
     * 实现颤音效果并返回short数组
     * @param path
     * @param vibratoDelay
     * @param vibratoDepth
     * @return
     */
    public short[] vibrato(String path, String decodepath, int start, int end,
                           float vibratoDelay, float vibratoDepth, float vibratoModulationFrequency) throws FileNotFoundException {

        sineInputStream = new FileInputStream(path);
        impulseInputStream = localcontext.getResources().openRawResource(R.raw.reverb_impulse);
        inputWave = new Wave(sineInputStream);
        impulseWave = new Wave(impulseInputStream);
        inputShort = inputWave.getSampleAmplitudes();
        impulseShort = impulseWave.getSampleAmplitudes();
        input = Util.ShortsToFloatsNormalized(inputShort);
        impulse = Util.ShortsToFloatsNormalized(impulseShort);

        float[] output;
        output = Effects.Vibrato(input, inputWave.getWaveHeader().getSampleRate(),
                vibratoDelay, vibratoDepth, vibratoModulationFrequency);

        // 可能会用到处理之后的short?
        short[] shorts_output = Util.FloatsToShortsNormalized(output);
        short[] raw = convertWav2Short(path, 44100, 2, 16);

        System.arraycopy(raw, 0, shorts_output,0,start*44100*2);

        // 长度不一致, 防止越界
        int min = shorts_output.length;
        if (raw.length<shorts_output.length){
            min = raw.length;
        }

        System.arraycopy(raw, end*44100*2, shorts_output,
                end*44100*2,min-end*44100*2-1);

        convershort2wav(shorts_output, decodepath, 44100, 2, 16);

        return shorts_output;
    }

    /***
     * 实现电吉他效果
     * @param path
     * @param flangerDelay
     * @param flangerDepth
     * @param flangerModulationFrequency
     * @return
     */
    public short[] flanger(String path, String decodepath, int start, int end,
                           float flangerDelay, float flangerDepth, float flangerModulationFrequency) throws FileNotFoundException {

        sineInputStream = new FileInputStream(path);
        impulseInputStream = localcontext.getResources().openRawResource(R.raw.reverb_impulse);
        inputWave = new Wave(sineInputStream);
        impulseWave = new Wave(impulseInputStream);
        inputShort = inputWave.getSampleAmplitudes();
        impulseShort = impulseWave.getSampleAmplitudes();
        input = Util.ShortsToFloatsNormalized(inputShort);
        impulse = Util.ShortsToFloatsNormalized(impulseShort);

        float[] output;
        output = Effects.Flanger(input, inputWave.getWaveHeader().getSampleRate(),
                flangerDelay, flangerDepth, flangerModulationFrequency);

        short[] shorts_output = Util.FloatsToShortsNormalized(output);
        short[] raw = convertWav2Short(path, 44100, 2, 16);

        System.arraycopy(raw, 0, shorts_output,0,start*44100*2);

        // 长度不一致, 防止越界
        int min = shorts_output.length;
        if (raw.length<shorts_output.length){
            min = raw.length;
        }

        System.arraycopy(raw, end*44100*2, shorts_output,
                end*44100*2,min-end*44100*2-1);

        convershort2wav(shorts_output, decodepath, 44100, 2, 16);

        return shorts_output;
    }

    public short[] chorus(String path, String decodepath, int start, int end,
                          float chorusDelay, float chorusDepth) throws FileNotFoundException {
        sineInputStream = new FileInputStream(path);
        impulseInputStream = localcontext.getResources().openRawResource(R.raw.reverb_impulse);
        inputWave = new Wave(sineInputStream);
        impulseWave = new Wave(impulseInputStream);
        inputShort = inputWave.getSampleAmplitudes();
        impulseShort = impulseWave.getSampleAmplitudes();
        input = Util.ShortsToFloatsNormalized(inputShort);
        impulse = Util.ShortsToFloatsNormalized(impulseShort);

        Log.e("here00", "here00");

        float[] output;
        output = Effects.Chorus(input, inputWave.getWaveHeader().getSampleRate(),
                chorusDelay, chorusDepth);

        Log.e("here01", "here01");

        short[] shorts_output = Util.FloatsToShortsNormalized(output);
        short[] raw = convertWav2Short(path, 44100, 2, 16);

        Log.e("here02", "here02");

        System.arraycopy(raw, 0, shorts_output,0,start*44100*2);

        // 长度不一致, 防止越界
        int min = shorts_output.length;
        if (raw.length<shorts_output.length){
            min = raw.length;
        }

        Log.e("here03", "here03");

        System.arraycopy(raw, end*44100*2, shorts_output,
                end*44100*2,min-end*44100*2-1);

        convershort2wav(shorts_output, decodepath, 44100, 2, 16);

        Log.e("here04", "here04");

        return shorts_output;
    }

    public short[] doubling(String path, float doublingDelay, float doublingDepth){

        float[] output;
        output = Effects.Doubling(input, inputWave.getWaveHeader().getSampleRate(),
                doublingDelay, doublingDepth);

        short[] shorts_output = Util.FloatsToShortsNormalized(output);
        playWave(output, inputWave.getWaveHeader().getSampleRate(),
                inputWave.getWaveHeader().getBitsPerSample());

        return shorts_output;
    }

    public void AdjustSpeed(String path, String decodepath, int start, int end, float rate){
        // 防止数组越界
        short[] raw = AudioFunction.convertPcm2Short(path,44100, 2, 16);
        if (start<0 || end*44100*2>raw.length){
            return;
        }

        short[] raw1 = new short[start*44100*2];
        System.arraycopy(raw, 0, raw1,0,start*44100*2);
        short[] raw2 = new short[raw.length - end*44100*2];
        System.arraycopy(raw, end*44100*2, raw2,0,
                raw.length-end*44100*2);
        short[] edit = new short[(int)((end-start)*44100*2/rate)];


        // 重新采样
        for (int i=0; i<edit.length; i++){
            edit[i] = raw[raw1.length + (int)(i*rate)];
        }


        short[] result = new short[raw1.length+edit.length+raw2.length];
        System.arraycopy(raw1, 0, result,0, raw1.length);
        System.arraycopy(edit, 0, result,raw1.length, edit.length);
        System.arraycopy(raw2, 0, result,raw1.length+edit.length,
                raw2.length);
        convershort2wav(result, decodepath, 44100, 2, 16);
//        float[] floats = Util.ShortsToFloatsNormalized(result);

        // 播放
//        playWave(floats, 44100,16);
    }

    /***
     * 限制声音大小，防止失真
     * @param raw 源音频
     * @return 返回限制后的音频
     */
    private short[] bound(short[] raw){
        for (int i=0; i<raw.length; i++){
            if (raw[i] < -30000){
                raw[i] = -30000;
            }
        }
        for (int i=0; i<raw.length; i++){
            if (raw[i] > 30000){
                raw[i] = 30000;
            }
        }
        return raw;
    }

    /***
     * 播放音乐
     * @param wave 音频
     * @param sampleRate 采样率
     * @param bitDepth bit深度
     */
    private static void playWave(float[] wave, int sampleRate, int bitDepth) {

        int encoding;
        switch (bitDepth) {
            case 8:
                encoding = AudioFormat.ENCODING_PCM_8BIT;
                break;
            case 16:
                encoding = AudioFormat.ENCODING_PCM_16BIT;
                break;
            default:
                System.out.println("Error: Can only read wave with 8 or 16 bits per sample");
                return;
        }

        // Convert wave to array of shorts
        short[] waveShort = Util.FloatsToShortsNormalized(wave);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, encoding, waveShort.length * 2, AudioTrack.MODE_STATIC);
        audioTrack.write(waveShort, 0, waveShort.length);
        audioTrack.play();
    }
}
