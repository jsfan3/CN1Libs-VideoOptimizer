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

#import <Foundation/Foundation.h>

@interface net_informaticalibera_videoediting_VideoEditingNativeInterfaceImpl : NSObject {
}

-(int)getVideoBitrate:(NSString*)param;
-(long long)getVideoDuration:(NSString*)param;
-(NSString*)getVideoSize:(NSString*)param;
-(void)optimizeVideoForUpload:(NSString*)param param1:(NSString*)param1;
-(void)getImageFromVideo:(NSString*)param param1:(NSString*)param1;
-(BOOL)isSupported;
@end
