# CN1Libs - VideoOptimizer
Video optimizer for Android and iOS Codename One apps, for fast upload.
It allows to get info about a video, get a preview image of a video and optimize a video for fast upload (currently it uses default compression values both on Android and on iOS according to the best trade-off of "quality / file length / time to compress" that I found.

## Javadocs
See: https://jsfan3.github.io/CN1Libs-VideoOptimizer/

The main class to be used is: https://jsfan3.github.io/CN1Libs-VideoOptimizer/net/informaticalibera/videoediting/VideoOptimizer.html

## Installation
Follow the standard way to install a CN1Lib from the Extension Manager: https://www.codenameone.com/blog/automatically-install-update-distribute-cn1libs-extensions.html

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

## License
The software that I wrote to create this CN1Lib is public-domain software, except for the `mobile-ffmpeg.aar` file included in the Android native folder. `mobile-ffmpeg.aar` is a custom version of [Mobile FFmpeg](https://github.com/tanersener/mobile-ffmpeg) compiled by me, licensed under the LGPL v3.0: that means that you can use this software in any project (open-source or commercial closed-source doesn't matter), but the sources used to compile `mobile-ffmpeg.aar` should be available to your users. Note that my `mobile-ffmpeg.aar` doesn't include any GPL software. You can download the sources that I used here: https://archive.org/download/mobileffmpegsources/. The instruction to compile are here: https://github.com/tanersener/mobile-ffmpeg/wiki/Building

