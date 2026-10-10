package se.lublin.mumla.app;

import android.media.MediaRecorder;
import android.test.AndroidTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import se.lublin.humla.audio.AudioInput;

public class MicrophoneCaptureTest extends AndroidTestCase {
    public void testCaptureStopsAndCanRestart() throws Exception {
        AtomicInteger frames = new AtomicInteger();
        AtomicReference<CountDownLatch> received = new AtomicReference<>(new CountDownLatch(1));
        AudioInput input = new AudioInput((frame, frameSize) -> {
            frames.incrementAndGet();
            received.get().countDown();
        }, MediaRecorder.AudioSource.MIC, 48000, "none");

        try {
            assertFalse(input.isRecording());
            assertEquals(0, frames.get());

            input.startRecording();
            assertTrue("No microphone frames received", received.get().await(5, TimeUnit.SECONDS));
            input.stopRecording();
            assertFalse(input.isRecording());
            int stoppedFrames = frames.get();
            Thread.sleep(100);
            assertEquals("Microphone still captures after stopping", stoppedFrames, frames.get());

            received.set(new CountDownLatch(1));
            input.startRecording();
            assertTrue("Capture did not resume", received.get().await(5, TimeUnit.SECONDS));
        } finally {
            input.shutdown();
        }
        assertFalse(input.isRecording());
    }
}
