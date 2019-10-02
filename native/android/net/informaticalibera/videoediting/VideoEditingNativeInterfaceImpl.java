/**
 * Video Optimized
 * Written in 2019 by Francesco Galgani, https://www.informatica-libera.net/
 *
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along
 * with this software. If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package net.informaticalibera.videoediting;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

public class VideoEditingNativeInterfaceImpl {

    Timer timer;
    TimerTask timerTask;

    public void getImageFromVideo(String param, String param1) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Called Android native method \"getImageFromVideo\"");
        final String videoFile = param;
        final String jpegFile = param1;
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoFile);
                    Bitmap capturedBitmap = mediaMetadataRetriever.getFrameAtTime(0);
                    OutputStream fOutputStream = new FileOutputStream(jpegFile);
                    capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOutputStream);
                    fOutputStream.flush();
                    fOutputStream.close();
                    mediaMetadataRetriever.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public long getVideoDuration(String param) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Called Android native method \"getVideoDuration\"");
        final String videoFile = param;
        final long[] result = {-1};
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoFile);
                    String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    mediaMetadataRetriever.release();

                    if (duration != null) {
                        result[0] = Long.parseLong(duration) / 1000;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return result[0];
    }

    public int getVideoBitrate(String param) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Called Android native method \"getVideoBitrate\"");
        final String videoFile = param;
        final int[] result = {-1};
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoFile);
                    String bitrate = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                    mediaMetadataRetriever.release();

                    if (bitrate != null) {
                        result[0] = Integer.parseInt(bitrate);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return result[0];
    }

    public String getVideoSize(String param) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Called Android native method \"getVideoSize\"");
        final String videoFile = param;
        final String[] result = {null};
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            public void run() {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoFile);
                    String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    mediaMetadataRetriever.release();

                    if (width != null && height != null) {
                        result[0] = width + "x" + height;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return result[0];
    }

    public boolean isSupported() {
        return true;
    }

    public void optimizeVideoForUpload(String param, String param1) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Called Android native method \"optimizeVideoForUpload\"");
        final String inputVideoFile = param;
        final String outputVideoFile = param1;

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(inputVideoFile);
        int width = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        mediaMetadataRetriever.release();
        final int scaleFactor;
        int scale = Math.round(Math.min(width, height) / 360);
        if (scale >= 1) {
            scaleFactor = scale;
        } else {
            scaleFactor = 1;
        }

        AsyncTask.execute(new Runnable() {
            public void run() {
                File myFile = new File(outputVideoFile);
                if (myFile.exists()) {
                    myFile.delete();
                }

                // we use custom version of MobileFFMpeg: https://github.com/tanersener/mobile-ffmpeg
                // note: FFmpeg.execute is blocking, it doesn't return until it completes
                startTimer(inputVideoFile);
                // these commands are optimized to get the faster execution with an acceptable quality
                // CISCO OpenH264 http://www.openh264.org/faq.html
                // FFmpeg.execute("-i " + inputVideoFile + " -vcodec libopenh264 -b 750000 -vf scale=iw/" + scaleFactor + ":ih/" + scaleFactor + " -c:a copy " + outputVideoFile);
                FFmpeg.execute("-i " + inputVideoFile + " -vcodec mpeg4 -b 750000 -vf scale=iw/" + scaleFactor + ":ih/" + scaleFactor + " -c:a copy " + outputVideoFile);
                stopTimer();

                int rc = FFmpeg.getLastReturnCode();
                String output = FFmpeg.getLastCommandOutput();

                if (rc == RETURN_CODE_SUCCESS) {
                    net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Exporting of the new video completed successfully");
                    net.informaticalibera.videoediting.VideoEditingCallbacks.runVideoOptimizedCallback(inputVideoFile);
                } else if (rc == RETURN_CODE_CANCEL) {
                    net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Exporting of the new video cancelled");
                    net.informaticalibera.videoediting.VideoEditingCallbacks.runVideoErrorCallback(inputVideoFile);
                } else {
                    net.informaticalibera.videoediting.VideoEditingCallbacks.logsFromNative("Exporting of the new video failed with error, code: " + rc + ", output: " + output);
                    net.informaticalibera.videoediting.VideoEditingCallbacks.runVideoErrorCallback(inputVideoFile);
                }
            }
        });
    }

    private void startTimer(String inputVideoFile) {
        net.informaticalibera.videoediting.VideoEditingCallbacks.setProgress(0);

        // set a new Timer
        timer = new Timer();

        // initialize the TimerTask's job
        initializeTimerTask(inputVideoFile);

        // schedule the timer, after the first 1000ms the TimerTask will run every 1000ms
        timer.schedule(timerTask, 2000, 2000);
    }

    private void stopTimer() {
        // stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimerTask(String inputVideoFile) {

        final long totalTime = getVideoDuration(inputVideoFile);

        timerTask = new TimerTask() {
            public void run() {
                Config.enableStatisticsCallback(new StatisticsCallback() {
                    public void apply(Statistics newStatistics) {
                        // getTime() returns ms, while totalTime are seconds, so: ms / (seconds * 1000) * 100 = ms / seconds / 10
                        long percentage = (newStatistics.getTime() / totalTime / 10);
                        if (percentage < 100) {
                            net.informaticalibera.videoediting.VideoEditingCallbacks.setProgress((int) percentage);
                        }
                    }
                });
            }
        };
    }

}
