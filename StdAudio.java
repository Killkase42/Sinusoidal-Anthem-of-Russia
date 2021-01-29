package com.company;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class StdAudio {

    /**
     *  The sample rate: 44,100 Hz for CD quality audio.
     */
    public static final int SAMPLE_RATE = 44100;

    private static final int BYTES_PER_SAMPLE = 8;       // 16-bit audio
    private static final int BITS_PER_SAMPLE = 16;       // 16-bit audio
    private static final double MAX_16_BIT = 32768;
    private static final int SAMPLE_BUFFER_SIZE = 4096;

    private static final int MONO = 1;
    private static final boolean LITTLE_ENDIAN = false;
    private static final boolean SIGNED = true;

    private static SourceDataLine line;   // Sound played.
    private static byte[] buffer;         // Internal buffer.
    private static int bufferSize = 0;    // Number of samples currently in internal buffer.

    private StdAudio() {
    }

    // Static initializer.
    static {
        init();
    }

    // Opens audio stream.
    private static void init() {
        try {
            // 44,100 Hz, 16-bit audio, mono, signed PCM, little endian.
            AudioFormat format = new AudioFormat((float) SAMPLE_RATE, BITS_PER_SAMPLE, MONO, SIGNED, LITTLE_ENDIAN);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE);

            // the internal buffer is a fraction of the actual buffer size, this choice is arbitrary
            // it gets divided because we can't expect the buffered data to line up exactly with when
            // the sound card decides to push out its samples.
            buffer = new byte[SAMPLE_BUFFER_SIZE * BYTES_PER_SAMPLE/3];
        }
        catch (LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
        line.start(); // Sound is only generated after this line.
    }

    /**
     * Closes standard audio.
     */
    public static void close() {
        line.drain();
        line.stop();
    }

    /**
     * Writes one sample (between -1.0 and +1.0) to standard audio.
     * If the sample is outside the range, it will be clipped.
     *
     * @param  sample the sample to play.
     * @throws IllegalArgumentException if the sample is {@code Double.NaN}.
     */
    public static void play(double sample) {
        if (Double.isNaN(sample)) throw new IllegalArgumentException("sample is NaN");

        // Clip if outside [-1, +1].
        if (sample < -1.0) sample = -1.0;
        if (sample > +1.0) sample = +1.0;

        // Convert to bytes.
        short s = (short) (MAX_16_BIT * sample);
        if (sample == 1.0) s = Short.MAX_VALUE;
        buffer[bufferSize++] = (byte) s;
        buffer[bufferSize++] = (byte) (s >> 8);

        // Sends to sound card if buffer is full.
        if (bufferSize >= buffer.length) {
            line.write(buffer, 0, buffer.length);
            bufferSize = 0;
        }
    }

    // Plays sounds using sine waves of different durations and amplitudes and makes use of
    // a double array instead of just a double to store multiple frequencies (chords).
    public static void multiplePlay(double[] hzs, double duration, double amplitude) {
        amplitude = amplitude / hzs.length;
        int N = (int) (SAMPLE_RATE * duration);
        double sum;
        for (int i = 0; i <= N; i++) {
            sum = 0;
            for (double hz : hzs)
                sum += amplitude * Math.sin(2 * Math.PI * i * hz / SAMPLE_RATE);
            play(sum);
        }
    }

    // Programmed sine waves to play.
    public static void main(String[] args) {

        // Assigning certain frequencies (hz) their respective note names.
        double rest = 0;

        double C3 = 130.81;
        double D3 = 146.83;
        double Eb3 = 155.56;
        double F3 = 174.61;
        double G3 = 196.00;
        double Ab3 = 207.65;
        double A3 = 220.00;
        double Bb3 = 233.08;
        double C4 = 261.63;
        double D4 = 293.66;
        double Eb4 = 311.13;
        double F4 = 349.23;
        double G4 = 392.00;
        double A4 = 440.00;
        double Ab4 = 415.30;
        double Bb4 = 466.16;
        double B4 = 493.88;
        double C5 = 523.25;
        double D5 = 587.33;
        double Eb5 = 622.25;
        double F5 = 698.46;
        double G5 = 783.99;
        double Ab5 = 830.61;
        double Bb5 = 932.33;

        // Changing duration values to notes.
        double sixteenth = 0.208333;
        double eighth = 0.416667;
        double sixteenthTriplet = 0.138888;
        double dottedEighth = 0.625;
        double quarter = 0.833333;
        double dottedQuarter = 1.25;
        double dottedHalf = 2.5;

        // Changing amplitude values to dynamics.
        double mf = 0.5;
        double f = 0.75;
        double ff = 1;

        // Programming the song, line breaks indicate a new bar.
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, dottedHalf, ff);
        multiplePlay(new double[]{rest}, eighth, mf);
        multiplePlay(new double[]{Bb4}, eighth, mf);

        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, quarter, mf);
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4}, dottedEighth, mf);
        multiplePlay(new double[]{C5}, sixteenth, mf);
        multiplePlay(new double[]{G3, D4, G4, Bb4, D5}, quarter, mf);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, mf);
        multiplePlay(new double[]{G3, G4}, eighth, mf);

        multiplePlay(new double[]{Ab3, C4, Eb4, C5}, quarter, mf);
        multiplePlay(new double[]{Ab3, C4, Eb4, Bb4}, dottedEighth, mf);
        multiplePlay(new double[]{Ab4}, sixteenth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4, Bb4}, quarter, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, eighth, mf);
        multiplePlay(new double[]{G3, Eb4}, eighth, mf);

        multiplePlay(new double[]{F3, Ab3, C4, F4}, quarter, mf);
        multiplePlay(new double[]{F3, Ab3, C4, F4}, dottedEighth, mf);
        multiplePlay(new double[]{F3, G4}, sixteenth, mf);
        multiplePlay(new double[]{Eb3, C4, Ab4}, quarter, mf);
        multiplePlay(new double[]{Eb3, C4, Ab4}, dottedEighth, mf);
        multiplePlay(new double[]{Eb3, C4, Bb4}, sixteenth, mf);

        multiplePlay(new double[]{Eb3, C4, C5}, quarter, mf);
        multiplePlay(new double[]{D3, D4, D5}, eighth, mf);
        multiplePlay(new double[]{C3, Eb4, Eb5}, eighth, mf);
        multiplePlay(new double[]{Bb3, F4, Bb4, D5, F5}, eighth, mf);
        multiplePlay(new double[]{Ab3, F4, Bb4, D5, F5}, eighth, mf);
        multiplePlay(new double[]{G3, F4, Bb4, D5, F5}, eighth, mf);
        multiplePlay(new double[]{F3, Bb4}, eighth, mf);

        // 6
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, quarter, f);
        multiplePlay(new double[]{G3, F4, Bb4, F5}, dottedEighth, f);
        multiplePlay(new double[]{G3, Eb4, Bb4, Eb5}, sixteenth, f);
        multiplePlay(new double[]{Bb3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{F3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{D3, F4, D5}, eighth, f);
        multiplePlay(new double[]{Bb3, D4, Bb4}, eighth, f);

        multiplePlay(new double[]{C3, Eb4, G4, C5, Eb5}, quarter, f);
        multiplePlay(new double[]{Eb3, D4, G4, D5}, dottedEighth, f);
        multiplePlay(new double[]{Eb3, C4, C5}, sixteenth, f);
        multiplePlay(new double[]{G3, D4, G4, D5}, eighth, f);
        multiplePlay(new double[]{D3, D4, G4, D5}, eighth, f);
        multiplePlay(new double[]{Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, Bb3, G4}, eighth, f);

        multiplePlay(new double[]{Ab3, C4, Eb4, C5}, quarter, mf);
        multiplePlay(new double[]{F3, C4, Eb4, Bb4}, dottedEighth, mf);
        multiplePlay(new double[]{F3, Ab3, C4, Eb4, Ab4}, sixteenth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4, Bb4}, quarter, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, dottedEighth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, sixteenth, mf);

        multiplePlay(new double[]{F3, Eb4, A4, Eb5}, quarter, mf);
        multiplePlay(new double[]{F3, Eb4, G4, D5}, dottedEighth, mf);
        multiplePlay(new double[]{F3, A4, C4}, sixteenth, mf);
        multiplePlay(new double[]{Bb3, D4, Bb4}, eighth, mf);
        multiplePlay(new double[]{Ab3, D4, Bb4}, eighth, mf);
        multiplePlay(new double[]{G3, D4, Bb4}, eighth, mf);
        multiplePlay(new double[]{F3}, eighth, mf);

        // 10
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{D3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{F3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{G3, D5, F5}, eighth, f);
        multiplePlay(new double[]{G3, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{G3, Bb4, D5}, eighth, f);
        multiplePlay(new double[]{G3, C5, Eb5}, eighth, f);

        multiplePlay(new double[]{Bb3, D5, F5}, dottedQuarter, f);
        multiplePlay(new double[]{Bb3, D4, F4, Bb4}, eighth, f);
        multiplePlay(new double[]{Bb3, D4, F4, Bb4}, quarter, f);
        multiplePlay(new double[]{Ab3, D4, F4, Bb4}, eighth, f);
        multiplePlay(new double[]{G3}, eighth, f);

        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{G3, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{D3, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{Eb3, Bb4, D5}, eighth, f);
        multiplePlay(new double[]{Eb3, A4, C5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4}, eighth, f);
        multiplePlay(new double[]{Eb3, A4, C5}, eighth, f);

        multiplePlay(new double[]{G3, Bb4, D5}, dottedQuarter, f);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, G4, Bb4}, eighth, f);
        multiplePlay(new double[]{F3, Ab4, C5}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5}, eighth, f);

        // 14
        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{Ab3, Eb4, Ab4, C5}, dottedEighth, mf);
        multiplePlay(new double[]{Eb3, Eb4, Bb4, D5}, sixteenth, mf);
        multiplePlay(new double[]{C3, C4, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{C4, Eb4, Ab4, C5}, dottedEighth, mf);
        multiplePlay(new double[]{Bb3, Eb4, Bb4, D5}, sixteenth, mf);

        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{Ab3, Eb4, Ab4, C5}, eighth, mf);
        multiplePlay(new double[]{Eb3, Ab4, C5, Eb5}, eighth, mf);
        multiplePlay(new double[]{C3, C4, Ab4, C5, Eb5, Ab5}, eighth, mf);
        multiplePlay(new double[]{Bb3, Ab4, Ab5}, sixteenthTriplet, mf);
        multiplePlay(new double[]{Ab4, Ab5}, sixteenthTriplet, mf);
        multiplePlay(new double[]{Ab4, Ab5}, sixteenthTriplet, mf);
        multiplePlay(new double[]{Ab3, Ab4, Ab5}, eighth, mf);
        multiplePlay(new double[]{G3, Ab4, Ab5}, eighth, mf);

        multiplePlay(new double[]{F3, Ab4, C5, F5, Ab5}, dottedQuarter, f);
        multiplePlay(new double[]{G3, Ab4, C5, F5, Ab5}, sixteenth, f);
        multiplePlay(new double[]{Ab3, Ab4, C5, F5, Ab5}, sixteenth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, G5}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{Bb3, Ab4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5}, eighth, f);

        multiplePlay(new double[]{Eb3, G4, Bb4, Eb4, G5}, eighth, f);
        multiplePlay(new double[]{D3, G4, Bb4, Eb4, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb4, G5}, eighth, f);
        multiplePlay(new double[]{G3, G4, Bb4, Eb5}, eighth, f);
        multiplePlay(new double[]{F3, G4, Bb4, Eb5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5}, eighth, f);
        multiplePlay(new double[]{D3, G4, Bb4, Eb5}, eighth, f);
        multiplePlay(new double[]{C3, G4, Bb4, Eb5}, eighth, f);

        // 18
        multiplePlay(new double[]{D3, F4, A4, C5, F5}, dottedQuarter, f);
        multiplePlay(new double[]{Eb3, F4, A4, C5, F5}, sixteenth, f);
        multiplePlay(new double[]{F3, F4, A4, C5, F5}, sixteenth, f);
        multiplePlay(new double[]{G3, G4, B4, Eb5}, eighth, f);
        multiplePlay(new double[]{G3, G4, B4, D5}, eighth, f);
        multiplePlay(new double[]{G3, F4, A4, C5}, eighth, f);
        multiplePlay(new double[]{G3, G4, Bb4, D5}, eighth, f);

        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{D3, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{Eb3, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{F3, Eb4, G4, C5}, eighth, f);
        multiplePlay(new double[]{C3, C4, Eb4, G4, C5}, quarter, f);
        multiplePlay(new double[]{Bb3, Eb4, G4, C5}, quarter, f);

        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, f);
        multiplePlay(new double[]{F3, Eb4, Bb4, D5}, dottedEighth, f);
        multiplePlay(new double[]{F3, Eb4, Ab4, C5}, sixteenth, f);
        multiplePlay(new double[]{G3, Eb4, Bb4}, quarter, f);
        multiplePlay(new double[]{G3, Bb3, Eb4}, eighth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, eighth, mf);

        multiplePlay(new double[]{F3, Eb4, A4, Eb5}, quarter, mf);
        multiplePlay(new double[]{F3, Eb4, G4, D5}, dottedEighth, mf);
        multiplePlay(new double[]{F3, Eb4, A4, C5}, sixteenth, mf);
        multiplePlay(new double[]{Bb3, D4, Bb4}, quarter, mf);
        multiplePlay(new double[]{Bb3, D4, Ab4, Bb4}, eighth, mf);
        multiplePlay(new double[]{Bb3, D4, Ab4, Bb4}, eighth, mf);

        // 22
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, quarter, f);
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4}, dottedEighth, f);
        multiplePlay(new double[]{Eb3, Eb4, G4, C5}, sixteenth, f);
        multiplePlay(new double[]{G3, D4, G4, Bb4, D5}, quarter, f);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, f);

        multiplePlay(new double[]{Ab3, C4, Eb4, C5}, quarter, f);
        multiplePlay(new double[]{Ab3, C4, Eb4, Bb4}, dottedEighth, f);
        multiplePlay(new double[]{Ab3, C4, Eb4, Ab4}, sixteenth, f);
        multiplePlay(new double[]{G3, Bb3, Eb4, Bb4}, quarter, f);
        multiplePlay(new double[]{Eb3, Bb3, Eb4}, eighth, f);
        multiplePlay(new double[]{Eb3, Bb3, Eb4}, eighth, f);

        multiplePlay(new double[]{F3, Ab3, C4, F4}, quarter, f);
        multiplePlay(new double[]{F3, Ab3, C4, F4}, dottedEighth, f);
        multiplePlay(new double[]{F3, Ab3, C4, G4}, sixteenth, f);
        multiplePlay(new double[]{Eb3, C4, Ab4}, quarter, f);
        multiplePlay(new double[]{Eb3, C4, Ab4}, dottedEighth, f);
        multiplePlay(new double[]{Eb3, C4, Bb4}, sixteenth, f);

        multiplePlay(new double[]{Eb3, C4, C5}, quarter, f);
        multiplePlay(new double[]{D3, D4, D5}, eighth, f);
        multiplePlay(new double[]{C3, Eb4, Eb5}, eighth, f);
        multiplePlay(new double[]{Bb3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{Ab3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{G3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{F3, F4, Bb4, D5}, eighth, f);

        //26
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, quarter, f);
        multiplePlay(new double[]{G3, F4, Bb4, F5}, dottedEighth, f);
        multiplePlay(new double[]{G3, Eb4, Bb4, Eb5}, sixteenth, f);
        multiplePlay(new double[]{Bb3,F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{F3, F4, Bb4, D5, F5}, eighth, f);
        multiplePlay(new double[]{D3, F4, D5}, eighth, f);
        multiplePlay(new double[]{Bb3, D4, Bb4 }, eighth, f);

        multiplePlay(new double[]{C3, Eb4, G4, C5, Eb5}, quarter, f);
        multiplePlay(new double[]{Eb3, D4, G4, D5}, dottedEighth, f);
        multiplePlay(new double[]{Eb3, C4, G4, C5}, sixteenth, f);
        multiplePlay(new double[]{D4, G4, D5}, quarter, f);
        multiplePlay(new double[]{Bb3, D4, G4}, eighth, mf);
        multiplePlay(new double[]{G3, Bb3, G4}, eighth, mf);

        multiplePlay(new double[]{Ab3, C4, Eb4, C5}, quarter, mf);
        multiplePlay(new double[]{F3, C4, Eb4, Bb4}, dottedEighth, mf);
        multiplePlay(new double[]{F3, Ab3, C4, Eb4, Ab4}, sixteenth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4, Bb4}, quarter, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, dottedEighth, mf);
        multiplePlay(new double[]{G3, Bb3, Eb4}, sixteenth, mf);

        multiplePlay(new double[]{F3, Eb4, A4, Eb5}, quarter, mf);
        multiplePlay(new double[]{F3, Eb4, G4, D5}, dottedEighth, mf);
        multiplePlay(new double[]{F3, A4, C5}, sixteenth, mf);
        multiplePlay(new double[]{Bb3, D4, Bb4}, eighth, mf);
        multiplePlay(new double[]{A3, Bb4, D5}, eighth, mf);
        multiplePlay(new double[]{G3, C5, Eb5}, eighth, mf);
        multiplePlay(new double[]{F3, D5, F5}, eighth, mf);

        // 30
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{D3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{F3, G4, Bb4, Eb5, G5}, eighth, f);
        multiplePlay(new double[]{G3, Bb4, D5, F5, Bb5}, eighth, f);
        multiplePlay(new double[]{G3, Bb4, C5, Eb5, Bb5}, eighth, f);
        multiplePlay(new double[]{G3, Bb4, D5, Bb5}, eighth, f);
        multiplePlay(new double[]{G3, Bb4, C5, Eb5, Bb5}, eighth, f);

        multiplePlay(new double[]{Bb3, Bb4, D5, F5, Bb5}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5, Bb5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5, Bb5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5, Bb5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Bb3, Bb4, Bb5}, eighth, f);
        multiplePlay(new double[]{F3, Bb3, D4, F4, Bb4}, eighth, f);
        multiplePlay(new double[]{Bb3, D4, F4, Bb4}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5}, eighth, f);
        multiplePlay(new double[]{Ab3, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{G3, D5, F5}, eighth, f);

        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{C3, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{D4, Eb4, G4, C5, Eb5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, D5, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, A4, C5, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Bb4, G5}, eighth, f);
        multiplePlay(new double[]{Eb3, G4, Ab4, C5, G5}, eighth, f);

        // 33
        multiplePlay(new double[]{G3, G4, Bb4, D5, G5}, eighth, f);
        multiplePlay(new double[]{G3, G4, Bb4, D5, G5}, sixteenthTriplet, f);
        multiplePlay(new double[]{G3, G4, Bb4, D5, G5}, sixteenthTriplet, f);
        multiplePlay(new double[]{G3, G4, Bb4, D5, G5}, sixteenthTriplet, f);
        multiplePlay(new double[]{G3, G4, G5}, eighth, f);
        multiplePlay(new double[]{D3, G3, Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, Bb3, D4, G4}, eighth, f);
        multiplePlay(new double[]{G3, G4, Bb4}, eighth, f);
        multiplePlay(new double[]{F4, Ab4, C5}, eighth, f);
        multiplePlay(new double[]{Bb3, Bb4, D5}, eighth, f);

        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{Ab3, Eb4, Ab4, C5}, dottedEighth, mf);
        multiplePlay(new double[]{Eb3, Eb4, Bb4, D5}, sixteenth, mf);
        multiplePlay(new double[]{C3, C4, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{C3, Eb4, Ab4, C5}, dottedEighth, mf);
        multiplePlay(new double[]{Bb3, Eb4, Bb4, D5}, sixteenth, mf);

        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, mf);
        multiplePlay(new double[]{Ab3, Eb4, Ab4, C5}, eighth, mf);
        multiplePlay(new double[]{Eb3, Ab4, C5, Eb5}, eighth, mf);
        multiplePlay(new double[]{C3, C4, Ab4, C5, Eb5, Ab5}, eighth, mf);
        multiplePlay(new double[]{Bb3, Ab4, Ab5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Bb3, Ab4, Ab5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Bb3, Ab4, Ab5}, sixteenthTriplet, f);
        multiplePlay(new double[]{Ab3, Ab4, Ab5}, eighth, f);
        multiplePlay(new double[]{G3, Ab4, Ab5}, eighth, f);

        // 36
        multiplePlay(new double[]{F3, Ab3, C5, F5, Ab5}, dottedQuarter, ff);
        multiplePlay(new double[]{G3, Ab3, C5, F5, Ab5}, sixteenth, ff);
        multiplePlay(new double[]{Ab3, C5, F5, Ab5}, sixteenth, ff);
        multiplePlay(new double[]{Bb3, Bb4, D5, G5}, eighth, ff);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5}, eighth, ff);
        multiplePlay(new double[]{Bb3, Ab4, C5, Eb5}, eighth, ff);
        multiplePlay(new double[]{Bb3, Bb4, D5, F5}, eighth, ff);

        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, ff);
        multiplePlay(new double[]{D3, G4, Bb4, Eb5, G5}, eighth, ff);
        multiplePlay(new double[]{Eb3, G4, Bb4, Eb5, G5}, eighth, ff);
        multiplePlay(new double[]{G3, G4, Bb4, Eb5}, eighth, ff);
        multiplePlay(new double[]{F3, G4, Bb4, Eb5}, eighth, ff);
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, sixteenth, ff);
        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, sixteenth, ff);
        multiplePlay(new double[]{D3, Eb4, G4, Bb4, Eb5}, eighth, ff);
        multiplePlay(new double[]{C3, Eb4, G4, Bb4, Eb5}, eighth, ff);

        multiplePlay(new double[]{D3, F4, A4, C5, F5}, dottedQuarter, ff);
        multiplePlay(new double[]{Eb3, F4, A4, C5, F5}, sixteenth, ff);
        multiplePlay(new double[]{F3, F4, A4, C5, F5}, sixteenth, ff);
        multiplePlay(new double[]{G3, G4, B4, Eb5}, eighth, ff);
        multiplePlay(new double[]{G3, G4, B4, D5}, eighth, ff);
        multiplePlay(new double[]{G3, F4, A4, C5}, eighth, ff);
        multiplePlay(new double[]{G3, G3, B4, D5}, eighth, ff);

        // 39
        multiplePlay(new double[]{C4, Eb4, G4, C5, Eb5}, eighth, ff);
        multiplePlay(new double[]{D3, Eb4, G4, C5, Eb5}, eighth, ff);
        multiplePlay(new double[]{Eb3, Eb4, G4, C5, Eb5}, eighth, ff);
        multiplePlay(new double[]{F3, Eb4, G4, C5}, eighth, ff);
        multiplePlay(new double[]{C3, C4, Eb4, G4, C5}, quarter, ff);
        multiplePlay(new double[]{Bb3, Eb4, G4, C5}, quarter, ff);

        multiplePlay(new double[]{Ab3, Eb4, C5, Eb5}, quarter, f);
        multiplePlay(new double[]{F3, Eb4, Bb4, D5}, eighth, f);
        multiplePlay(new double[]{F3, Eb4, Ab4, C5}, eighth, f);
        multiplePlay(new double[]{G3, Bb3, Eb4, Bb4}, quarter, f);
        multiplePlay(new double[]{G3, Bb3, Eb4}, dottedEighth, f);
        multiplePlay(new double[]{G3, Bb3, Eb4}, sixteenth, f);

        multiplePlay(new double[]{Bb3, D4, Ab4, Bb4}, 2.2, f);
        multiplePlay(new double[]{Ab3, C4, Ab4, Bb4, C5}, 1.1, f);
        multiplePlay(new double[]{Bb3, D4, F4, Ab4, D5}, 1.1, f);

        multiplePlay(new double[]{Eb3, Eb4, G4, Bb4, Eb5}, 4, 1.2);

        StdAudio.close(); //Closes input to the speaker.
    }
}