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

import com.codename1.io.Log;
import com.codename1.ui.CN;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 * <p>
 * This class is deprecated because you shouldn't use it directly.</p>
 * @author Francesco Galgani
 */
public class VideoEditingCallbacks {

    private static Map<String, Runnable> successCallbackMap = new HashMap<>();
    private static Map<String, Runnable> errorCallbackMap = new HashMap<>();
    private static int progress = 0;

    /**
     * Sets a success callback for the given inputVideoFile, that is called when
     * the optimization is completed.
     *
     * @param input, it ignores the path, it consider only the file name, that
     * must to be unique!
     * @param optimizeVideoForUploadCallback callback
     */
    public static void setOptimizedVideoCallback(String input, Runnable optimizeVideoForUploadCallback) {
        String inputVideoFile = removePath(input);
        Log.p("VideoEditingCallbacks -> Called setOptimizedVideoCallback for inputVideoFile: " + inputVideoFile, Log.DEBUG);
        successCallbackMap.put(inputVideoFile, optimizeVideoForUploadCallback);
    }

    /**
     * Sets an error callback for the given inputVideoFile, that is called when
     * the optimization fails.
     *
     * @param input, it ignores the path, it consider only the file name, that
     * must to be unique!
     * @param errorCallback callback
     */
    public static void setVideoErrorCallback(String input, Runnable errorCallback) {
        String inputVideoFile = removePath(input);
        Log.p("VideoEditingCallbacks -> Called setVideoErrorCallback for inputVideoFile: " + inputVideoFile, Log.DEBUG);
        errorCallbackMap.put(inputVideoFile, errorCallback);
    }

    /**
     * This method is invoked by the native code.
     *
     * @param input, it ignores the path, it consider only the file name, that
     * must to be unique!
     */
    public static void runVideoOptimizedCallback(String input) {
        String inputVideoFile = removePath(input);
        Log.p("VideoEditingCallbacks -> Called runVideoOptimizedCallback for inputVideoFile: " + inputVideoFile, Log.DEBUG);
        if (successCallbackMap.get(inputVideoFile) != null) {
            Log.p("VideoEditingCallbacks -> The success callback for inputVideoFile: " + inputVideoFile + " is going to be run", Log.DEBUG);
            CN.callSerially(() -> {
                successCallbackMap.get(inputVideoFile).run();
            });
        }
    }

    /**
     * This method is invoked by the native code.
     *
     * @param input, it ignores the path, it consider only the file name, that
     * must to be unique!
     */
    public static void runVideoErrorCallback(String input) {
        String inputVideoFile = removePath(input);
        Log.p("VideoEditingCallbacks -> Called runVideoErrorCallback for inputVideoFile: " + inputVideoFile, Log.DEBUG);
        if (errorCallbackMap.get(inputVideoFile) != null) {
            Log.p("VideoEditingCallbacks -> The error callback for inputVideoFile: " + inputVideoFile + " is going to be run", Log.DEBUG);
            CN.callSerially(() -> {
                errorCallbackMap.get(inputVideoFile).run();
            });
        }
    }

    /**
     * Returns the file name only (with extension), removing the path
     *
     * @param inputVideoFile path
     * @return
     */
    private static String removePath(String inputVideoFile) {
        int index = inputVideoFile.lastIndexOf("/");
        if (index > -1) {
            return inputVideoFile.substring(index + 1);
        } else {
            return inputVideoFile;
        }
    }

    /**
     * Get the percentage of the progress
     *
     * @return a number from 0 to 99
     */
    public static int getProgress() {
        return progress;
    }

    /**
     * Invoked by the native code
     *
     * @param progress percentage
     */
    public static void setProgress(int progress) {
        VideoEditingCallbacks.progress = progress;
    }

    /**
     * Invoked by the native code
     *
     * @param toBeLogged by the EDT
     */
    public static void logsFromNative(String toBeLogged) {
        CN.callSerially(() -> Log.p("VideoOptimizer native code -> " + toBeLogged, Log.DEBUG));
    }

}
