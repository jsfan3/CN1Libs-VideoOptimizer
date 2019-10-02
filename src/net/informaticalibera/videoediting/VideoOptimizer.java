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

import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.system.NativeLookup;
import static com.codename1.ui.CN.getAppHomePath;
import com.codename1.ui.geom.Dimension;
import com.codename1.util.OnComplete;
import com.codename1.util.StringUtil;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Video Optimizer CN1Lib public layer class: use this class to get info about a
 * video, to get a preview of a video and to optimize a video for fast upload.
 *
 * @author Francesco Galgani
 */
public class VideoOptimizer {

    private static boolean isOptimizerExecuting = false;
    private VideoEditingNativeInterface videoEditing = NativeLookup.create(VideoEditingNativeInterface.class);

    /**
     * Returns the duration of the videoFile in seconds, or -1 in case of error
     *
     * @param videoFile placed in FileSystemStorage
     * @return duration in seconds, or -1
     */
    public long getVideoDuration(String videoFile) {
        if (videoEditing != null && videoEditing.isSupported()) {
            // it's mandatory to remove the "file://" when accessing to native interfaces
            String inputFile = removeFilePrefix(videoFile);
            return videoEditing.getVideoDuration(inputFile);
        } else {
            Log.p("VideoOptimizer is not supported in the current platform", Log.ERROR);
            return -1;
        }
    }

    /**
     * Extract a frame from the given video and returns a jpeg file, or null in
     * case of error
     *
     * @param videoFile placed in FileSystemStorage
     * @return the jpeg preview placed in FileSystemStorage (or null in case of
     * error), it ensures to don't overwrite any existing file
     */
    public String getVideoPreview(String videoFile) {
        if (videoEditing != null && videoEditing.isSupported()) {
            String outputFile = getRandomUniqueFilePath("jpg"); // note that it has the "file://" prefix
            // it's mandatory to remove the "file://" when accessing to native interfaces
            videoEditing.getImageFromVideo(removeFilePrefix(videoFile), removeFilePrefix(outputFile));
            // before returning, it check that the jpeg file was produced correctly
            try {
                if (FileSystemStorage.getInstance().exists(outputFile)) {
                    String mimeType = Util.guessMimeType(outputFile);
                    if ("image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType)) {
                        // Ok, the file exists and it's a valid jpeg
                        return outputFile;
                    }
                }
            } catch (IOException ex) {
                Log.p("The jpeg file produces by VideoOptimizer.getVideoPreview cannot be open", Log.ERROR);
            }
            return null;
        } else {
            Log.p("VideoOptimizer is not supported in the current platform", Log.ERROR);
            return null;
        }
    }

    /**
     * Get the average bitrate (in bits/sec), or -1 in case of error.
     *
     * @param videoFile placed in FileSystemStorage
     * @return average bitrate (in bits/sec), or -1 in case of error
     */
    public int getVideoBitrate(String videoFile) {
        if (videoEditing != null && videoEditing.isSupported()) {
            // it's mandatory to remove the "file://" when accessing to native interfaces
            String inputFile = removeFilePrefix(videoFile);
            return videoEditing.getVideoBitrate(inputFile);
        } else {
            Log.p("VideoOptimizer is not supported in the current platform", Log.ERROR);
            return -1;
        }
    }

    /**
     * Returns the size of the given video, or null in case of error
     *
     * @param videoFile placed in FileSystemStorage
     * @return the size of the given video, or null in case of error
     */
    public Dimension getVideoSize(String videoFile) {
        if (videoEditing != null && videoEditing.isSupported()) {
            // it's mandatory to remove the "file://" when accessing to native interfaces
            String inputFile = removeFilePrefix(videoFile);
            String videoSize = videoEditing.getVideoSize(inputFile);
            java.util.List<String> tokens = StringUtil.tokenize(videoSize, "x");
            if (tokens.size() == 2) {
                try {
                    int width = Integer.parseInt(tokens.get(0));
                    int height = Integer.parseInt(tokens.get(1));
                    return new Dimension(width, height);
                } catch (NumberFormatException ex) {
                    // probably something wrong happened in the native layer and it was logged;
                    // in this public layer, we simply return null as declared in the Javadoc
                }
            }
            Log.p("VideoOptimizer.getVideoSize returned an invalid size", Log.ERROR);
            return null;
        } else {
            Log.p("VideoOptimizer is not supported in the current platform", Log.ERROR);
            return null;
        }
    }

    /**
     * <p>
     * Asynchronous method to optimize a video for fast upload and maximum
     * compatibility: it produces a new mp4 file placed in FileSystemStorage (it
     * ensures to don't overwrite any existing file); note that it produces
     * always an "mp4" file even if the source video is in QuickTime format.
     * </p>
     * <p>
     * This method is cpu-intensive and it can require a time to complete longer
     * than the video duration.
     * </p>
     * <p>
     * It throws an IllegalStateException if you invoke this method when there
     * is an ongoing video optimization: you must wait that it finishes before
     * invoking this method again.
     * </p>
     *
     * @param videoFile placed in FileSystemStorage
     * @param onCompleteCallback executed when the saving of the new video is
     * completed
     * @param onFailureCallback generic callback in case of error, note that
     * errors are logged by the EDT
     * @param onProgressCallback generic callback invoked every second to update
     * the UI with the progress percentage (from 0 to 99)
     */
    public void optimizeVideoForUpload(String videoFile, OnComplete<String> onCompleteCallback, Runnable onFailureCallback, OnProgress onProgressCallback) {
        if (isOptimizerExecuting) {
            throw new IllegalStateException("VideoOptimizer.optimizeVideoForUpload has been invoked at inappropriate time: you must wait for the ongoing video optimization to finish.");
        }

        if (videoEditing != null && videoEditing.isSupported()) {

            // reset to percentage
            VideoEditingCallbacks.setProgress(0);

            // Output file
            String outputFile = getRandomUniqueFilePath("mp4"); // note that it has the "file://" prefix

            // The timer is used to invoke the onProgressCallback every second
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    onProgressCallback.update(VideoEditingCallbacks.getProgress());
                }
            };
            Timer[] timer = {new Timer()};

            // sets the callbacks
            VideoEditingCallbacks.setOptimizedVideoCallback(videoFile, () -> {
                timer[0].cancel();
                onCompleteCallback.completed(outputFile);
                isOptimizerExecuting = false;
            });
            VideoEditingCallbacks.setVideoErrorCallback(videoFile, () -> {
                timer[0].cancel();
                onFailureCallback.run();
                isOptimizerExecuting = false;
            });

            // it's mandatory to remove the "file://" when accessing to native interfaces
            isOptimizerExecuting = true;
            videoEditing.optimizeVideoForUpload(removeFilePrefix(videoFile), removeFilePrefix(outputFile));

            // start the timer (that is stopped by the success and failure callbacks),
            timer[0].schedule(task, 0, 1000);
        } else {
            Log.p("VideoOptimizer is not supported in the current platform", Log.ERROR);
            onFailureCallback.run();
        }

    }

    /**
     * Returns an unique (not used) file path in the app home path of
     * FileSystemStorage, with the given extension; if the extension doesn't
     * start with a dot ".", it will be added automatically.
     *
     * @param extension it can be null to don't add any extension
     * @return
     */
    private static String getRandomUniqueFilePath(String extension) {
        long timeStamp = System.currentTimeMillis();
        if (extension != null && !extension.startsWith(".")) {
            extension = "." + extension;
        }
        String randomPath;
        if (extension != null) {
            randomPath = getAppHomePath() + timeStamp + extension;
            // check that the randomPath is not used
            while (FileSystemStorage.getInstance().exists(randomPath)) {
                timeStamp++;
                randomPath = getAppHomePath() + timeStamp + extension;
            }
        } else {
            randomPath = getAppHomePath() + timeStamp;
            // check that the randomPath is not used
            while (FileSystemStorage.getInstance().exists(randomPath)) {
                timeStamp++;
                randomPath = getAppHomePath() + timeStamp;
            }
        }
        return randomPath;
    }

    /**
     * Removes the "file:" prefix for the given filePath
     *
     * @param filePath is the full path of a file in the FileSystemStorage
     * @return
     */
    private String removeFilePrefix(String filePath) {
        //Log.p("Original filePath: " + filePath);
        String prefix = "file:";
        if (filePath.startsWith(prefix)) {
            String pathWithoutPrefix = filePath.substring(5);
            while (pathWithoutPrefix.startsWith("//")) {
                pathWithoutPrefix = pathWithoutPrefix.substring(1);
            }
            //Log.p("filePath without prefix: " + pathWithoutPrefix);
            return pathWithoutPrefix;
        } else {
            return filePath;
        }
    }

}
