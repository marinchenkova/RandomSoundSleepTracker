package marinchenko.name.randomsoundsleeptracker.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Valentin on 09.01.2018.
 */

public class DynamicToneGen {

    private final static int MIN_DELAY = 3000;

    private boolean random = false;
    private boolean block = false;

    public DynamicToneGen() {
    }

    public void play(final double freqHz,
                     final int curve,
                     final int durationMs,
                     final int delayMs,
                     final double ratio) {
        if (this.random || this.block) {
            playDelayed(freqHz,
                    curve,
                    durationMs,
                    block && !random ? delayMs : durationMs + getRandomDelay(delayMs),
                    ratio,
                    this.random);
            this.block = false;
        }
    }

    public void setBlockTrue() {
        this.block = true;
    }

    public void setRandom(final boolean random) {
        this.random = random;
    }

    public void playDelayed(final double freqHz,
                            final int curve,
                            final int durationMs,
                            final int delayMs,
                            final double ratio,
                            final boolean random) {
        final Timer timer = new Timer();
        final TimerTask playTask = new PlayTask(freqHz, curve, durationMs, delayMs, ratio, random);
        timer.schedule(playTask, delayMs);
    }

    private static AudioTrack generateTone(final double freqHz,
                                           final int curve,
                                           final int durationMs,
                                           final double ratio) {
        final int sampleRate = 48000;

        final int count = (int) (sampleRate * 2.0 * (durationMs / 1000.0)) & ~1;
        final short[] samples = new short[count];

        for(int i = 0; i < count; i += 2){
            final double amp = dynamicVolume(ratio, curve, i, count);
            final short sample = (short)
                    (amp * (Math.sin(2 * Math.PI * i / (sampleRate / freqHz)) * 0x7FFF));
            samples[i] = sample;
            samples[i + 1] = sample;
        }


        final AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                count * (Short.SIZE / 8), AudioTrack.MODE_STATIC);

        track.write(samples, 0, count);

        return track;
    }

    /**
     * Кривая возрастания громкости от 0 до 1.
     * @param increaseRatio Доля времени возрастания на все время звучания.
     * @param curve Кривизна возрастания громкости. При отрицательных значениях кривая проходит
     *              ниже точки (0.5, 0.5).
     * @param countCurr Текущее значение счетчика.
     * @param countMax Максимальное значение счетчика (конец звука).
     * @return Громкость от 0 до 1.
     */
    private static double dynamicVolume(final double increaseRatio,
                                        double curve,
                                        final int countCurr,
                                        final int countMax) {
        final int countEnd = (int) (countMax * increaseRatio);
        if (countCurr <= countEnd) {
            final double x = ((double) countCurr) / countEnd;
            final int dir = curve > 0 ? 1 : 0;
            curve = curve == 0 ? 0.01 : Math.abs(curve);
            return Math.atan((x - dir) * curve) / Math.atan(curve) + dir;
        } else return 1;
    }

    private static int getRandomDelay(final int max) {
        return MIN_DELAY + (int) (Math.random() * max);
    }

    private class PlayTask extends TimerTask {
        private final double freqHz;
        private final int durationMs;
        private final int delayMs;
        private final double ratio;
        private final int curve;
        private final boolean random;

        PlayTask(final double freqHz,
                 final int curve,
                 final int durationMs,
                 final int delayMs,
                 final double ratio,
                 final boolean random) {
            this.freqHz = freqHz;
            this.durationMs = durationMs;
            this.delayMs = delayMs;
            this.ratio = ratio;
            this.curve = curve;
            this.random = random;
        }

        @Override
        public void run() {
            generateTone(freqHz, curve, durationMs, ratio).play();
            if (random) play(freqHz, curve, durationMs, delayMs, ratio);
        }
    }
}
