package com.Tool.Function;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import zty.composeaudio.Tool.Decode.DecodeEngine;

import com.Tool.Global.Constant;
import com.Tool.Global.Variable;

import zty.composeaudio.Tool.Interface.ComposeAudioInterface;
import zty.composeaudio.Tool.Interface.DecodeOperateInterface;

/**
 * Created by zhengtongyu on 16/5/29.
 */

public class AudioFunction {
    /***
     * 把mp3解码成PCM
     * @param musicFileUrl 源文件mp3的路径
     * @param decodeFileUrl PCM文件的路径
     * @param startSecond 起始解码时间
     * @param endSecond 结束解码时间
     * @param decodeOperateInterface 接口
     */
    public static void DecodeMusicFile(final String musicFileUrl, final String decodeFileUrl, final int startSecond,
                                       final int endSecond,
                                       final DecodeOperateInterface decodeOperateInterface) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                DecodeEngine.getInstance().beginDecodeMusicFile(musicFileUrl, decodeFileUrl, startSecond, endSecond,
                        decodeOperateInterface);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogFunction.error("异常观察", e.toString());
                    }

                    @Override
                    public void onNext(String string) {
                    }
                });
    }

    /***
     * 合成音频
     * @param firstAudioPath 实参：tempVoicePcmUrl，PCM格式录音（录音本身就是PCM）
     * @param secondAudioPath 实参：decodeFileUrl，解码为PCM的背景音乐
     * @param composeFilePath 实参：composeVoiceUrl，合成音乐
     * @param deleteSource 是否删除源
     * @param firstAudioWeight 第一个音频比重
     * @param secondAudioWeight 第二个音频比重
     * @param audioOffset 偏置，见MainActivity → decodeSuccess → BeginComposeAudio
     * @param composeAudioInterface 接口
     */
    public static void BeginComposeAudio(final String firstAudioPath, final String secondAudioPath,
                                         final String composeFilePath, final boolean deleteSource,
                                         final float firstAudioWeight,
                                         final float secondAudioWeight, final int audioOffset,
                                         final ComposeAudioInterface composeAudioInterface) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                ComposeAudio(firstAudioPath, secondAudioPath, composeFilePath, deleteSource,
                        firstAudioWeight, secondAudioWeight, audioOffset, composeAudioInterface);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String string) {
                    }
                });
    }

    /***
     * 将两段音频进行合成
     * @param firstAudioFilePath 第一段音频路径
     * @param secondAudioFilePath 第二段音频路径
     * @param composeAudioFilePath 合成音频路径
     * @param deleteSource 是否删除源
     * @param firstAudioWeight 第一段音频在合成中所占的比重
     * @param secondAudioWeight 第二段音频在合成中所占的比重
     * @param audioOffset 偏置，第二个音频的头在第一个音频的audioOffset处
     * @param composeAudioInterface 合成音频接口
     */
    public static void ComposeAudio(String firstAudioFilePath, String secondAudioFilePath,
                                    String composeAudioFilePath, boolean deleteSource,
                                    float firstAudioWeight, float secondAudioWeight,
                                    int audioOffset,
                                    final ComposeAudioInterface composeAudioInterface) {

        // 音频处理是否完成
        boolean firstAudioFinish = false;
        boolean secondAudioFinish = false;

        // 第一、二、合成音频的ByteBuffer数组
        byte[] firstAudioByteBuffer;
        byte[] secondAudioByteBuffer;
        byte[] mp3Buffer;

        short resultShort;
        short[] outputShortArray;

        // index是合成音频的下标？
        int index;
        // 实际读进缓冲区的大小
        int firstAudioReadNumber;
        int secondAudioReadNumber;
        int outputShortArrayLength;

        // 缓冲区预设大小，每次写1024byte
        final int byteBufferSize = 1024;

        firstAudioByteBuffer = new byte[byteBufferSize];
        secondAudioByteBuffer = new byte[byteBufferSize];
        mp3Buffer = new byte[(int) (7200 + (byteBufferSize * 1.25))];

        outputShortArray = new short[byteBufferSize / 2];

        Handler handler = new Handler(Looper.getMainLooper());

        // 一二音频输入流，合成音频输出流
        FileInputStream firstAudioInputStream = FileFunction.GetFileInputStreamFromFile
                (firstAudioFilePath);
        FileInputStream secondAudioInputStream = FileFunction.GetFileInputStreamFromFile
                (secondAudioFilePath);
        FileOutputStream composeAudioOutputStream = FileFunction.GetFileOutputStreamFromFile
                (composeAudioFilePath);

        LameUtil.init(Constant.RecordSampleRate, Constant.LameBehaviorChannelNumber,
                Constant.BehaviorSampleRate, Constant.LameBehaviorBitRate, Constant.LameMp3Quality);

        try {
            // 循环合成+编码
            while (!firstAudioFinish && !secondAudioFinish) {
                index = 0;

                if (audioOffset < 0) {
                    secondAudioReadNumber = secondAudioInputStream.read(secondAudioByteBuffer);

                    outputShortArrayLength = secondAudioReadNumber / 2;

                    for (; index < outputShortArrayLength; index++) {
                        // 这个地方需要解释一下，PCM文件有两位，分别是2个channel，所以分开处理
                        // 00000010 00 0f 10
                        // 00000020 .. .. ..
                        // 00000030 .. .. ..
                        // resultShort把第二个音频的两个channel合成一个整数short
                        resultShort = CommonFunction.GetShort(secondAudioByteBuffer[index * 2],
                                secondAudioByteBuffer[index * 2 + 1], Variable.isBigEnding);

                        // 加权之后的resultShort
                        outputShortArray[index] = (short) (resultShort * secondAudioWeight);
                    }

                    //
                    audioOffset += secondAudioReadNumber;

                    // 读完了
                    if (secondAudioReadNumber < 0) {
                        secondAudioFinish = true;
                        break;
                    }

                    if (audioOffset >= 0) {
                        break;
                    }
                } else {
                    firstAudioReadNumber = firstAudioInputStream.read(firstAudioByteBuffer);

                    outputShortArrayLength = firstAudioReadNumber / 2;

                    for (; index < outputShortArrayLength; index++) {
                        resultShort = CommonFunction.GetShort(firstAudioByteBuffer[index * 2],
                                firstAudioByteBuffer[index * 2 + 1], Variable.isBigEnding);

                        outputShortArray[index] = (short) (resultShort * firstAudioWeight);
                    }

                    audioOffset -= firstAudioReadNumber;

                    if (firstAudioReadNumber < 0) {
                        firstAudioFinish = true;
                        break;
                    }

                    if (audioOffset <= 0) {
                        break;
                    }
                }

                if (outputShortArrayLength > 0) {
                    int encodedSize = LameUtil.encode(outputShortArray, outputShortArray,
                            outputShortArrayLength, mp3Buffer);

                    if (encodedSize > 0) {
                        composeAudioOutputStream.write(mp3Buffer, 0, encodedSize);
                    }
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (composeAudioInterface != null) {
                        composeAudioInterface.updateComposeProgress(20);
                    }
                }
            });

            while (!firstAudioFinish || !secondAudioFinish) {
                index = 0;

                firstAudioReadNumber = firstAudioInputStream.read(firstAudioByteBuffer);
                secondAudioReadNumber = secondAudioInputStream.read(secondAudioByteBuffer);

                int minAudioReadNumber = Math.min(firstAudioReadNumber, secondAudioReadNumber);
                int maxAudioReadNumber = Math.max(firstAudioReadNumber, secondAudioReadNumber);

                if (firstAudioReadNumber < 0) {
                    firstAudioFinish = true;
                }

                if (secondAudioReadNumber < 0) {
                    secondAudioFinish = true;
                }

                int halfMinAudioReadNumber = minAudioReadNumber / 2;

                outputShortArrayLength = maxAudioReadNumber / 2;

                for (; index < halfMinAudioReadNumber; index++) {
                    resultShort = CommonFunction.WeightShort(firstAudioByteBuffer[index * 2],
                            firstAudioByteBuffer[index * 2 + 1], secondAudioByteBuffer[index * 2],
                            secondAudioByteBuffer[index * 2 + 1], firstAudioWeight,
                            secondAudioWeight, Variable.isBigEnding);

                    outputShortArray[index] = resultShort;
                }

                if (firstAudioReadNumber != secondAudioReadNumber) {
                    if (firstAudioReadNumber > secondAudioReadNumber) {
                        for (; index < outputShortArrayLength; index++) {
                            resultShort = CommonFunction.GetShort(firstAudioByteBuffer[index * 2],
                                    firstAudioByteBuffer[index * 2 + 1], Variable.isBigEnding);

                            outputShortArray[index] = (short) (resultShort * firstAudioWeight);
                        }
                    } else {
                        for (; index < outputShortArrayLength; index++) {
                            resultShort = CommonFunction.GetShort(secondAudioByteBuffer[index * 2],
                                    secondAudioByteBuffer[index * 2 + 1], Variable.isBigEnding);

                            outputShortArray[index] = (short) (resultShort * secondAudioWeight);
                        }
                    }
                }

                if (outputShortArrayLength > 0) {
                    // 把mp3Buffer转为mp3
                    int encodedSize = LameUtil.encode(outputShortArray, outputShortArray,
                            outputShortArrayLength, mp3Buffer);

                    // 把mp3Buffer输出，这时候估计是mp3格式了？
                    if (encodedSize > 0) {
                        composeAudioOutputStream.write(mp3Buffer, 0, encodedSize);
                    }
                }
            }
        } catch (Exception e) {
            LogFunction.error("ComposeAudio异常", e);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (composeAudioInterface != null) {
                        composeAudioInterface.composeFail();
                    }
                }
            });

            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (composeAudioInterface != null) {
                    composeAudioInterface.updateComposeProgress(50);
                }
            }
        });

        try {
            final int flushResult = LameUtil.flush(mp3Buffer);

            if (flushResult > 0) {
                composeAudioOutputStream.write(mp3Buffer, 0, flushResult);
            }
        } catch (Exception e) {
            LogFunction.error("释放ComposeAudio LameUtil异常", e);
        } finally {
            try {
                composeAudioOutputStream.close();
            } catch (Exception e) {
                LogFunction.error("关闭合成输出音频流异常", e);
            }

            LameUtil.close();
        }

        if (deleteSource) {
            FileFunction.DeleteFile(firstAudioFilePath);
            FileFunction.DeleteFile(secondAudioFilePath);
        }

        try {
            firstAudioInputStream.close();
            secondAudioInputStream.close();
        } catch (IOException e) {
            LogFunction.error("关闭合成输入音频流异常", e);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (composeAudioInterface != null) {
                    composeAudioInterface.composeSuccess();
                }
            }
        });
    }

    /**
     * PCM文件转WAV文件
     * @param inPcmFilePath 输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate 采样率，例如44100
     * @param channels 声道数 单声道：1或双声道：2
     * @param bitNum 采样位数，8或16
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                                      int channels, int bitNum) {

        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 输出WAV文件
     * @param out WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen 整个数据大小
     * @param sampleRate 采样率
     * @param channels 声道数
     * @param byteRate 采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, int sampleRate, int channels,
                                            long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /***
     * 把PCM文件变成short数组返回
     * @param inPcmFilePath PCM文件路径
     * @param sampleRate 采样率
     * @param channels 声道数量
     * @param bitNum 比特数
     * @return 返回转换之后的short数组
     */
    public static short[] convertPcm2Short(String inPcmFilePath, int sampleRate,
                                    int channels, int bitNum){
        FileInputStream in = null;
        File file = new File(inPcmFilePath);
        int length = 0;

        if (file.exists() && file.isFile()){
            length = (int) file.length();
        }else{
            Log.e("FileError", "file doesn't exist or is not a file");
        }

        // shorts数组是返回值，长度为data的一半
        short[] shorts = new short[length/2];

        // 把PCM文件读到data里面，length是byte数，注意用这种方式最大能读入2G的数据，不过对于歌曲来说够用了
        byte[] data = new byte[length];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            // 把PCM读进data中，然后更新length，是原来的一半，用来形成short数组的
            length = in.read(data)/2;
            int index = 0;

            for (; index < length; index++) {
                shorts[index] = CommonFunction.GetShort(data[index * 2],
                        data[index * 2 + 1], Variable.isBigEnding);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return shorts;
    }

    /***
     * 把MP3片段转为short数组
     * @param mp3Path MP3路径
     * @param tempPath PCM临时文件路径
     * @param start 开始时间
     * @param end 结束时间
     * @param sampleRate 采样率
     * @param channels 声道数
     * @param bitNum 比特数
     * @param decodeOperateInterface 接口
     * @return 返回short数组
     */
    public static short[] convertMp32Short(String mp3Path, String tempPath, int start, int end,
                                    int sampleRate, int channels, int bitNum, DecodeOperateInterface decodeOperateInterface){
        // shorts数组是返回值
        short[] shorts;
        DecodeMusicFile(mp3Path, tempPath, start, end, decodeOperateInterface);

        // 程序延时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        shorts = convertPcm2Short(tempPath, sampleRate, channels, bitNum);


        Log.e("shortLength02", String.valueOf(shorts.length));

        return shorts;
    }


    public static void convershort2wav(short[] shorts, String decodepath, int sampleRate,
                                       int channels, int bitNum){
        int length = shorts.length;
        int ByteLength = length*2;
        byte[] resultByte = new byte[ByteLength];

        // short[index]解码的临时byte数组
        byte[] tempByte;

        for (int index = 0; index < length; index ++) {
            tempByte = CommonFunction.GetBytes(shorts[index], Variable.isBigEnding);
            resultByte[2 * index] = tempByte[0];
            resultByte[2 * index + 1] = tempByte[1];
        }

        Log.e("resultByte", String.valueOf(resultByte.length));

        convertByte2wav(resultByte, decodepath, sampleRate, channels, bitNum, decodepath);
    }

    public static short[] convertWav2Short(String inPcmFilePath, int sampleRate,
                                           int channels, int bitNum){
        FileInputStream in = null;
        File file = new File(inPcmFilePath);
        int length = 0;

        if (file.exists() && file.isFile()){
            length = (int) file.length();
        }else{
            Log.e("FileError", "file doesn't exist or is not a file");
        }

        // shorts数组是返回值，长度为data的一半
        short[] shorts = new short[length/2];

        // 把PCM文件读到data里面，length是byte数，注意用这种方式最大能读入2G的数据，不过对于歌曲来说够用了
        byte[] data = new byte[length];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            // 把PCM读进data中，然后更新length，是原来的一半，用来形成short数组的
            length = in.read(data)/2;
            // 跳过文件头
            int index = 22;

            for (; index < length; index++) {
                shorts[index] = CommonFunction.GetShort(data[index * 2],
                        data[index * 2 + 1], Variable.isBigEnding);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return shorts;
    }


    public static void convertByte2wav(byte[] bytes, String decodepath, int sampleRate, int channels,
                                       int bitNum, String outWavFilePath){
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            out = new FileOutputStream(outWavFilePath);

            // byte数组大小
            long totalAudioLen = bytes.length;

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            out.write(bytes, 0, (int)totalAudioLen);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
