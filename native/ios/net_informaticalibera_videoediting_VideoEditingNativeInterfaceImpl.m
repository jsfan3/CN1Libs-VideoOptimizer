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

#import "net_informaticalibera_videoediting_VideoEditingNativeInterfaceImpl.h"

#import <AVFoundation/AVAsset.h>
#import <AVFoundation/AVAssetImageGenerator.h>
#import <AVFoundation/AVBase.h>
#import <AVFoundation/AVAsynchronousKeyValueLoading.h>
#import <AVFoundation/AVAssetTrack.h>
#import <AVFoundation/AVAssetTrackSegment.h>
#import <AVFoundation/AVMediaFormat.h>
#import <AVFoundation/AVMetadataFormat.h>
#import <CoreMedia/CMTimeRange.h>
#import <CoreMedia/CoreMedia.h>
#import "net_informaticalibera_videoediting_VideoEditingCallbacks.h"
#import "CodenameOne_GLViewController.h"
#import "cn1_globals.h"

@implementation net_informaticalibera_videoediting_VideoEditingNativeInterfaceImpl

AVAssetExportSession *encoder;
NSTimer *exportProgressBarTimer;

-(void)getImageFromVideo:(NSString*)param param1:(NSString*)param1{
    dispatch_sync(dispatch_get_main_queue(), ^{
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Called iOS native code, method \"getImageFromVideo\""));
        NSString* videoFile = param;
        NSString* jpegFile = param1;
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Video input file:"));
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"videoFile"));
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Jpeg output file:"));
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"jpegFile"));
        NSLog(@"The video file is: %@\nThe jpeg file is: %@", videoFile, jpegFile);

        NSURL* contentURL = [NSURL fileURLWithPath:videoFile];

        // https://stackoverflow.com/a/10677003
        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:contentURL options:nil];
        AVAssetImageGenerator *generator = [[AVAssetImageGenerator alloc] initWithAsset:asset];
        generator.appliesPreferredTrackTransform = YES;
        NSError *err = NULL;
        CMTime time = CMTimeMake(1, 60);
        CGImageRef imgRef = [generator copyCGImageAtTime:time actualTime:NULL error:&err];

        UIImage *theImage = [[[UIImage alloc] initWithCGImage:imgRef] autorelease];
        // Save image.
        [UIImageJPEGRepresentation(theImage, 0.9) writeToFile:jpegFile atomically:YES];

        CGImageRelease(imgRef);
        [asset release];
        [generator release];
    });
}

-(long long)getVideoDuration:(NSString*)param{
    
    float avDurationInSeconds = -1;
    float* pointer = &avDurationInSeconds;
    
    dispatch_sync(dispatch_get_main_queue(), ^{
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Called iOS native code, method \"getVideoDuration\""));
        NSString* videoFile = param;
        
        NSURL* contentURL = [NSURL fileURLWithPath:videoFile];
        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:contentURL options:nil];
        CMTime avDuration = [asset duration];
        *pointer = CMTimeGetSeconds(avDuration);
        [asset release];
    });
    
    return avDurationInSeconds;
}

-(int)getVideoBitrate:(NSString*)param{
    
    int bitrate = -1;
    int* pointer = &bitrate;
    
    dispatch_sync(dispatch_get_main_queue(), ^{
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Called iOS native code, method \"getVideoBitrate\""));
        NSString* videoFile = param;
        
        AVAssetTrack *videoTrack = nil;
        NSURL* contentURL = [NSURL fileURLWithPath:videoFile];
        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:contentURL options:nil];
        
        NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
        
        CMFormatDescriptionRef formatDescription = NULL;
        NSArray *formatDescriptions = [videoTrack formatDescriptions];
        
        if ([formatDescriptions count] > 0)
            formatDescription = (CMFormatDescriptionRef)[formatDescriptions objectAtIndex:0];
        
        if ([videoTracks count] > 0)
            videoTrack = [videoTracks objectAtIndex:0];
        
        *pointer = [videoTrack estimatedDataRate];
        
        [asset release];
    });
    
    return bitrate;
}

-(NSString*)getVideoSize:(NSString*)param{
    int width = 0;
    int height = 0;
    int* widthP = &width;
    int* heightP = &height;
    
    dispatch_sync(dispatch_get_main_queue(), ^{
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Called iOS native code, method \"getVideoSize\""));
        NSString* videoFile = param;
        
        AVAssetTrack *videoTrack = nil;
        NSURL* contentURL = [NSURL fileURLWithPath:videoFile];
        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:contentURL options:nil];
        
        NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
        
        CMFormatDescriptionRef formatDescription = NULL;
        NSArray *formatDescriptions = [videoTrack formatDescriptions];

        if ([formatDescriptions count] > 0)
            formatDescription = (CMFormatDescriptionRef)[formatDescriptions objectAtIndex:0];
        
        if ([videoTracks count] > 0)
            videoTrack = [videoTracks objectAtIndex:0];
        
        CGSize trackDimensions = {
            .width = 0.0,
            .height = 0.0,
        };
        trackDimensions = [videoTrack naturalSize];
        *widthP = trackDimensions.width;
        *heightP = trackDimensions.height;
        
        [asset release];
    });
    
    return [NSString stringWithFormat:@"%dx%d", width, height];;
}

-(void)optimizeVideoForUpload:(NSString*)param param1:(NSString*)param1{

    dispatch_async(dispatch_get_main_queue(), ^{
        net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Called iOS native code, method \"optimizeVideoForUpload\""));
        
        NSString* videoFile = param;
        NSString* outputPath = param1;

        NSFileManager *fileManager = [NSFileManager defaultManager];
        if ([fileManager fileExistsAtPath:outputPath]){ 
                [fileManager removeItemAtPath:outputPath error:nil];
        }  
        
        NSURL* contentURL = [NSURL fileURLWithPath:videoFile];
        AVURLAsset *inputUrlAsset = [[AVURLAsset alloc] initWithURL:contentURL options:nil];
        NSURL *outputUrl = [NSURL fileURLWithPath:outputPath];

        net_informaticalibera_videoediting_VideoEditingCallbacks_setProgress___int(CN1_THREAD_GET_STATE_PASS_ARG 0);
        encoder = [[AVAssetExportSession alloc] initWithAsset:inputUrlAsset presetName:AVAssetExportPresetMediumQuality];
        exportProgressBarTimer = [NSTimer scheduledTimerWithTimeInterval:.1 target:self selector:@selector(updateExportDisplay) userInfo:nil repeats:YES];

        encoder.outputFileType = AVFileTypeMPEG4;
        encoder.shouldOptimizeForNetworkUse = YES;
        encoder.outputURL = outputUrl;

        [encoder exportAsynchronouslyWithCompletionHandler:^
        {
          if (encoder.status == AVAssetExportSessionStatusCompleted)
          {
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Video exported successfully"));
            net_informaticalibera_videoediting_VideoEditingCallbacks_runVideoOptimizedCallback___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG videoFile));
          }
          else if (encoder.status == AVAssetExportSessionStatusCancelled)
          {
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Video export cancelled"));
            net_informaticalibera_videoediting_VideoEditingCallbacks_runVideoErrorCallback___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG videoFile));
          }
          else
          {
            NSLog(@"Video export failed with error: %@ (%ld)", encoder.error.localizedDescription, encoder.error.code);
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Video export failed with error:"));
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG encoder.error.localizedDescription));
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Code of error:"));
            net_informaticalibera_videoediting_VideoEditingCallbacks_logsFromNative___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [NSString stringWithFormat:@"%ld", encoder.error.code]));
            net_informaticalibera_videoediting_VideoEditingCallbacks_runVideoErrorCallback___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG videoFile));
          }
        }];
        
        [inputUrlAsset release];
    });
}

- (void)updateExportDisplay {
    net_informaticalibera_videoediting_VideoEditingCallbacks_setProgress___int(CN1_THREAD_GET_STATE_PASS_ARG (encoder.progress * 100));
    if (encoder.progress > .99) {
        [exportProgressBarTimer invalidate];
    }
}

-(BOOL)isSupported{
    return YES;
}

@end