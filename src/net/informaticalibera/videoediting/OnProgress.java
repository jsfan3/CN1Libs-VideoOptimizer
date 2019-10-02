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

/**
 * Invoked automatically every second while the video optimization is in
 * progress, this can be used to update the UI with the progress percentage.
 *
 * @author Francesco Galgani
 */
public interface OnProgress {

    /**
     * Invoked to update the UI with the progress percentage.
     *
     * @param v the value of the percentage (0 to 99).
     */
    public void update(int v);

}
