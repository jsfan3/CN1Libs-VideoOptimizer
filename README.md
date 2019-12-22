# CN1Libs - VideoOptimizer
Video optimizer for Android and iOS Codename One apps, for fast upload.
It allows to get info about a video, get a preview image of a video and optimize a video for fast upload (currently it uses default compression values both on Android and on iOS according to the best trade-off of "quality / file length / time to compress" that I found.

## Javadocs
See: https://jsfan3.github.io/CN1Libs-VideoOptimizer/

The main class to be used is: https://jsfan3.github.io/CN1Libs-VideoOptimizer/net/informaticalibera/videoediting/VideoOptimizer.html

## Installation
Follow the standard way to install a CN1Lib from the Extension Manager: https://www.codenameone.com/blog/automatically-install-update-distribute-cn1libs-extensions.html

### Build hint added automatically and compatibility with other CN1Libs
Take note that this CN1Lib adds automatically the build hints:
```
android.buildToolsVersion=28
android.min_sdk_version=24
```
This can affect the compatibility with your project or with other CN1Libs

### Compatibility with devices ###
The devices supported are:
- Android 7+ with ARM64 CPU (other CPUs are not supported to keep this CN1Lib as small as possible).
- iOS 12+ (I didn't test previous versions, I don't know if they are supported).

### Note about app size on Android ###
This CN1Lib increases your APKs of 11MB because it includes a custom version of [Mobile FFmpeg](https://github.com/tanersener/mobile-ffmpeg) compiled by me. Note that a full version of Mobile FFmpeg requires about 80MB and a minimal version requires about 30MB, so my custom version is a lot smaller. I reduced the CPU support only to ARMv7 and ARM64 and I reduced the number of supported codecs to a minimal set.
On iOS, there is no impact on app size, because I used the native APIs without any external library.

### Supported input and output video formats ###
On iOS, the input video formats supported are the one supported by `AVAssetExportSession`, however I didn't find a list of the supported formats: if you find any info, I'll add a link here. The optimized output video is encoded using the option [`AVFileTypeMPEG4`](https://developer.apple.com/documentation/avfoundation/avfiletypempeg4?language=objc), however the Apple's doc doesn't specify which MPEG4 encoder is used.

On Android, I compiled Mobile FFmpeg so: `./android.sh --disable-x86 --disable-x86-64`, that means I didn't include any external library. The supported formats are the ones supported by `libavcodec`: https://en.wikipedia.org/wiki/Libavcodec#Implemented_video_codecs Note that h.264 is only decoded, but the encoding of the optimized video is done using `MPEG-4 Part 2`, that doesn't need any external library (x264 encoder is not an option, because its license doesn't allow its use in a closed source project, and CISCO openh264 is not an option because it's too much slow on encoding, accoring to my tests, and it requires to pay `MPEG LA` licensing fees).

### Output video dimension (width and height) ###
The optimized video dimension is close to a 360p video, however the exact size depends on the input video size. On Android it's used an integer scale factor and on iOS the documentation of the used `AVAssetExportPresetMediumQuality` is too much generic and it doesn't say anything about the output file dimension.

### Speed ###
On iOS, the optimization is very fast.

On Android, it's slow and it can take longer time than the video duration. However I chose options that allow to save as fast as possible preserving an acceptable video quality.

## Example of usage
![Example of usage](https://raw.githubusercontent.com/jsfan3/CN1Libs-VideoOptimizer/master/screenshot.png)

Create a new project and add the build hints:
```
ios.NSPhotoLibraryUsageDescription=Some functionality of the application requires access to your media gallery
ios.NSAppleMusicUsageDescription=Some functionality of the application requires access to your media library
```

In the main class, add the following code:
```
    private String originalFile = null;
    private String optimizedFile = null;

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        Form hi = new Form("Video optimizer test", BoxLayout.y());
        VideoOptimizer videoOptimizer = new VideoOptimizer();

        Button previewBtn = new Button("GET PREVIEW");
        Button durationBtn = new Button("GET DURATION");
        Button bitrateBtn = new Button("GET BITRATE");
        Button sizeBtn = new Button("GET SIZE");
        Button optimizeBtn = new Button("OPTIMIZE VIDEO");
        Button playOriginalBtn = new Button("PLAY ORIGINAL");
        Button playOptimizedBtn = new Button("PLAY OPTIMIZED");

        SpanLabel infoLabel = new SpanLabel(" ");
        Label previewLabel = new Label("") {
            public Dimension calcPreferredSize() {
                Dimension dim = super.calcPreferredSize();
                dim.setHeight(CN.convertToPixels(50, false));
                return dim;
            }
        };

        hi.addAll(durationBtn, previewBtn, bitrateBtn, sizeBtn, optimizeBtn, playOriginalBtn, playOptimizedBtn, infoLabel, previewLabel);
        hi.show();

        playOriginalBtn.addActionListener(l -> {
            try {
                if (originalFile == null || !FileSystemStorage.getInstance().exists(originalFile)) {
                    ToastBar.showErrorMessage("Original video file not available", 5000);
                } else {
                    Form videoPlayer = new Form("Original video", new BorderLayout(BorderLayout.CENTER_BEHAVIOR_SCALE));
                    Media video = MediaManager.createMedia(originalFile, true);
                    videoPlayer.add(BorderLayout.CENTER, new MediaPlayer(video));
                    videoPlayer.getToolbar().setBackCommand("Back", Toolbar.BackCommandPolicy.ALWAYS, e -> {
                        hi.showBack();
                    });
                    videoPlayer.addShowListener(ll -> {
                        video.play();
                    });
                    videoPlayer.show();
                }
            } catch (IOException ex) {
                Log.e(ex);
            }
        });

        playOptimizedBtn.addActionListener(l -> {
            try {
                if (optimizedFile == null || !FileSystemStorage.getInstance().exists(optimizedFile)) {
                    ToastBar.showErrorMessage("Optimized video file not available", 5000);
                } else {
                    Form videoPlayer = new Form("Optimized video", new BorderLayout(BorderLayout.CENTER_BEHAVIOR_SCALE));
                    Media video = MediaManager.createMedia(optimizedFile, true);
                    video.setNativePlayerMode(true);
                    videoPlayer.add(BorderLayout.CENTER, new MediaPlayer(video));
                    videoPlayer.getToolbar().setBackCommand("Back", Toolbar.BackCommandPolicy.ALWAYS, e -> {
                        hi.showBack();
                    });
                    videoPlayer.addShowListener(ll -> {
                        video.play();
                    });
                    videoPlayer.show();
                }
            } catch (IOException ex) {
                Log.e(ex);
            }
        });

        durationBtn.addActionListener(l -> {
            CN.openGallery(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev != null && ev.getSource() != null) {
                        originalFile = (String) ev.getSource();
                        long duration = videoOptimizer.getVideoDuration(originalFile);
                        long h = duration / 3600;
                        long m = (duration - h * 3600) / 60;
                        long s = duration - (h * 3600 + m * 60);
                        infoLabel.setText("Duration: " + h + "h, " + m + "m, " + s + "s");
                        hi.revalidate();
                    }
                }
            }, CN.GALLERY_VIDEO);
        });

        bitrateBtn.addActionListener(l -> {
            CN.openGallery(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev != null && ev.getSource() != null) {
                        originalFile = (String) ev.getSource();
                        int bitrate = videoOptimizer.getVideoBitrate(originalFile);
                        infoLabel.setText("Bitrate: " + bitrate + " bit/s (" + (bitrate / 8 / 1024) + " KiB/s)");
                        hi.revalidate();
                    }
                }
            }, CN.GALLERY_VIDEO);
        });

        optimizeBtn.addActionListener(l -> {
            CN.openGallery(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev != null && ev.getSource() != null) {
                        originalFile = (String) ev.getSource();
                        long startTime = System.currentTimeMillis();
                        OnComplete onCompleteCallback = (output) -> {
                            optimizedFile = (String) output;
                            float originalLength = Math.round(FileSystemStorage.getInstance().getLength(originalFile) * 10 / (1024 * 1024)) / 10f;
                            float optimizedLength = Math.round(FileSystemStorage.getInstance().getLength(optimizedFile) * 10 / (1024 * 1024)) / 10f;
                            int originalBitrate = videoOptimizer.getVideoBitrate(originalFile);
                            int optimizedBitrate = videoOptimizer.getVideoBitrate(optimizedFile);
                            Dimension originalSize = videoOptimizer.getVideoSize(originalFile);
                            Dimension optimizedSize = videoOptimizer.getVideoSize(optimizedFile);
                            infoLabel.setText("Original: " + originalLength + "MiB, optimized: " + optimizedLength + "MiB" + "\n"
                                    + "Original: " + (originalBitrate / 8 / 1024) + "KiB/s, optimized: " + (optimizedBitrate / 8 / 1024) + "KiB/s" + "\n"
                                    + "Original: " + originalSize.getWidth() + "x" + originalSize.getHeight() + ", optimized: " + optimizedSize.getWidth() + "x" + optimizedSize.getHeight() + "\n"
                                    + "Video duration: " + videoOptimizer.getVideoDuration(originalFile) + " s, execution: " + ((System.currentTimeMillis() - startTime) / 1000) + " s"
                            );
                            hi.revalidate();
                        };
                        Runnable onFailureCallback = () -> {
                            infoLabel.setText("Optimizing failure, see logs.");
                            hi.revalidate();
                            Log.sendLogAsync();
                        };
                        OnProgress onProgressCallback = (percentage) -> {
                            infoLabel.setText("Optimizing, please wait... " + percentage + "%");
                            hi.revalidate();
                        };
                        videoOptimizer.optimizeVideoForUpload(originalFile, onCompleteCallback, onFailureCallback, onProgressCallback);
                        infoLabel.setText("Optimizing, please wait...");
                        hi.revalidate();

                    }
                }

            }, CN.GALLERY_VIDEO);
        });

        sizeBtn.addActionListener(l -> {
            CN.openGallery(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev != null && ev.getSource() != null) {
                        originalFile = (String) ev.getSource();
                        Dimension videoSize = videoOptimizer.getVideoSize(originalFile);
                        infoLabel.setText("Video size: " + videoSize.getWidth() + "x" + videoSize.getHeight());
                        hi.revalidate();
                    }
                }
            }, CN.GALLERY_VIDEO);
        });

        previewBtn.addActionListener(l -> {
            CN.openGallery(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev != null && ev.getSource() != null) {
                        try {
                            originalFile = (String) ev.getSource();
                            String jpegFile = videoOptimizer.getVideoPreview(originalFile);
                            EncodedImage encodedImg = EncodedImage.create(FileSystemStorage.getInstance().openInputStream(jpegFile));
                            previewLabel.getAllStyles().setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FIT);
                            previewLabel.getAllStyles().setBgImage(encodedImg);
                            previewLabel.repaint();
                            hi.scrollComponentToVisible(previewLabel);
                        } catch (IOException ex) {
                            Log.e(ex);
                        }
                    }
                }
            }, CN.GALLERY_VIDEO);
        });
    }
```

### Tip: check if a given file is a supported video

```
    private static VideoOptimizer videoOptimizer = new VideoOptimizer();

    /**
     * Very fast method to detect if the given file is a supported video
     * (it relies on VideoOptimizer CN1Lib)
     *
     * @param file placed in FileSystemStorage
     * @return
     */
    public static boolean isVideo(String file) {
        if (videoOptimizer.getVideoDuration(file) > 0) {
            return true;
        } else {
            return false;
        }
    }
    ```

## License
The software that I wrote to create this CN1Lib is public-domain software, except for the `mobile-ffmpeg.aar` file included in the Android native folder. `mobile-ffmpeg.aar` is a custom version of [Mobile FFmpeg](https://github.com/tanersener/mobile-ffmpeg) compiled by me, licensed under the LGPL v3.0: that means that you can use this software in any project (open-source or commercial closed-source doesn't matter), but the sources used to compile `mobile-ffmpeg.aar` should be available to your users. Note that my `mobile-ffmpeg.aar` doesn't include any GPL software. You can download the sources that I used here: https://archive.org/download/mobileffmpegsources/. The instruction to compile are here: https://github.com/tanersener/mobile-ffmpeg/wiki/Building

