/*
 * DDSPixelFormat.java - This file is part of Java DDS ImageIO Plugin
 *
 * Copyright (C) 2011 Niklas Kyster Rasmussen
 *
 * COPYRIGHT NOTICE:
 * Java DDS ImageIO Plugin is based on code from the DDS GIMP plugin.
 * Copyright (C) 2004-2010 Shawn Kirst <skirst@insightbb.com>,
 * Copyright (C) 2003 Arne Reuter <homepage@arnereuter.de>
 *
 * Java DDS ImageIO Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Java DDS ImageIO Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java DDS ImageIO Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * FILE DESCRIPTION:
 * TODO Write File Description for DDSPixelFormat.java
 *
 * CHANGES:
 * - Renamed package.
 * - Extracted Format to DDSFormat
 *
 * ORIGINAL: https://code.google.com/p/java-dds/
 */
package dncli.dds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DDSPixelFormat {
    //Flags
    public static final int ALPHAPIXELS = 0x1; //0x00000001;
    public static final int ALPHA = 0x2; //0x00000001;
    public static final int FOURCC = 0x4;
    public static final int RGB = 0x40;
    public static final int YUV = 0x200;
    public static final int LUMINANCE = 0x20000;

    private final int size;
    private final long flags;
    private final long fourCC;
    private final long rgbBitCount;
    private final long rMask;
    private final long gMask;
    private final long bMask;
    private final long aMask;
    private final long rMaskFixed;
    private final long gMaskFixed;
    private final long bMaskFixed;
    private final long aMaskFixed;
    private final int rShift;
    private final int gShift;
    private final int bShift;
    private final int aShift;
    private final int rBits;
    private final int gBits;
    private final int bBits;
    private final int aBits;
    private final DDSFormat format;

    public DDSPixelFormat(int size, long flags, long fourCC, long rgbBitCount, long rMask, long gMask, long bMask, long aMask) {
        this.size = size;
        this.flags = flags;
        this.fourCC = fourCC;
        this.rgbBitCount = rgbBitCount;
        this.rMask = rMask;
        this.gMask = gMask;
        this.bMask = bMask;
        this.aMask = aMask;

        this.rShift = shift(rMask);
        this.gShift = shift(gMask);
        this.bShift = shift(bMask);
        this.aShift = shift(aMask);

        this.rBits = bits(rMask);
        this.gBits = bits(gMask);
        this.bBits = bits(bMask);
        this.aBits = bits(aMask);

        this.rMaskFixed = rMask >> rShift << (8 - rBits);
        this.gMaskFixed = gMask >> gShift << (8 - gBits);
        this.bMaskFixed = bMask >> bShift << (8 - bBits);
        this.aMaskFixed = aMask >> aShift << (8 - aBits);

        format = calcFormat();
    }

    public int getSize() {
        return size;
    }

    public long getFlags() {
        return flags;
    }

    public long getFourCC() {
        return fourCC;
    }

    public long getRgbBitCount() {
        return rgbBitCount;
    }

    public long getMaskRed() {
        return rMask;
    }

    public long getMaskGreen() {
        return gMask;
    }

    public long getMaskBlue() {
        return bMask;
    }

    public long getMaskAlpha() {
        return aMask;
    }

    public long getMaskFixedRed() {
        return rMaskFixed;
    }

    public long getMaskFixedGreen() {
        return gMaskFixed;
    }

    public long getMaskFixedBlue() {
        return bMaskFixed;
    }

    public long getMaskFixedAlpha() {
        return aMaskFixed;
    }

    public int getShiftRed() {
        return rShift;
    }

    public int getShiftGreen() {
        return gShift;
    }

    public int getShiftBlue() {
        return bShift;
    }

    public int getShiftAlpha() {
        return aShift;
    }

    public int getBitsRed() {
        return rBits;
    }

    public int getBitsGreen() {
        return gBits;
    }

    public int getBitsBlue() {
        return bBits;
    }

    public int getBitsAlpha() {
        return aBits;
    }

    public DDSFormat getFormat() {
        return format;
    }

    public boolean isAlphaPixels() {
        return ((flags & ALPHAPIXELS) != 0);
    }

    public boolean isAlpha() {
        return ((flags & ALPHA) != 0);
    }

    public boolean isFourCC() {
        return ((flags & FOURCC) != 0);
    }

    public boolean isRGB() {
        return ((flags & RGB) != 0);
    }

    public boolean isYUV() {
        return ((flags & YUV) != 0);
    }

    public boolean isLuminance() {
        return ((flags & LUMINANCE) != 0);
    }

    public void printValues() {
        printValues(0);
    }

    public void printValues(int nSpace) {
        String sSpace = "";
        for (int i = 0; i < nSpace; i++) {
            sSpace = sSpace + "	";
        }
        System.out.println(sSpace + "PixelFormat: ");

        System.out.println(sSpace + "	size: " + size);
        System.out.print(sSpace + "	flags: " + flags);
        if ((flags & ALPHAPIXELS) != 0) System.out.print(" (ALPHAPIXELS)");
        if ((flags & ALPHA) != 0) System.out.print(" (ALPHA)");
        if ((flags & FOURCC) != 0) System.out.print(" (FOURCC)");
        if ((flags & RGB) != 0) System.out.print(" (RGB)");
        if ((flags & YUV) != 0) System.out.print(" (YUV)");
        if ((flags & LUMINANCE) != 0) System.out.print(" (LUMINANCE)");
        System.out.print("\n");
        System.out.println(sSpace + "	fourCC: " + fourCC + " (" + getFormat().getName() + ")");
        System.out.println(sSpace + "	rgbBitCount: " + rgbBitCount);
        System.out.println(sSpace + "	rMask: " + Long.toHexString(rMask) + " int(" + rMask + ") fixed(" + Long.toHexString(rMaskFixed) + ") shift(" + rShift + ") bits(" + rBits + ")");
        System.out.println(sSpace + "	gMask: " + Long.toHexString(gMask) + " int(" + gMask + ") fixed(" + Long.toHexString(gMaskFixed) + ") shift(" + gShift + ") bits(" + gBits + ")");
        System.out.println(sSpace + "	bMask: " + Long.toHexString(bMask) + " int(" + bMask + ") fixed(" + Long.toHexString(bMaskFixed) + ") shift(" + bShift + ") bits(" + bBits + ")");
        System.out.println(sSpace + "	aMask: " + Long.toHexString(aMask) + " int(" + aMask + ") fixed(" + Long.toHexString(aMaskFixed) + ") shift(" + aShift + ") bits(" + aBits + ")");
        System.out.println(sSpace + "	Format: " + getFormat().getName());
    }

    private char shift(long mask) {
        char i = 0;
        if (mask <= 0) return 0;
        while (((mask >> i) & 1) <= 0) {
            ++i;
        }
        return i;
    }

    private char bits(long mask) {
        char i = 0;

        while (mask > 0) {
            if ((mask & 1) != 0) ++i;
            mask >>= 1;
        }
        return i;
    }

    private DDSFormat calcFormat() {
        if ((flags & FOURCC) == 0) {
            List<FormatItem> list = new ArrayList<FormatItem>();
            if (isLuminance()) {
                if (aMask != 0) list.add(new FormatItem("A", aMask, aBits));
                if (rMask != 0) list.add(new FormatItem("L", rMask, rBits));
            } else {
                if (aMask != 0) list.add(new FormatItem("A", aMask, aBits));
                if (rMask != 0) list.add(new FormatItem("R", rMask, rBits));
                if (gMask != 0) list.add(new FormatItem("G", gMask, gBits));
                if (bMask != 0) list.add(new FormatItem("B", bMask, bBits));
            }
            Collections.sort(list);
            String s = "";
            for (FormatItem item : list) {
                s = s + item.toString();
            }
            DDSFormat.UNCOMPRESSED.setName(rgbBitCount + "bit-" + s);
            return DDSFormat.UNCOMPRESSED;
        } else {
            for (DDSFormat formatSearch : DDSFormat.values()) {
                if (fourCC == formatSearch.getFourCC()) {
                    return formatSearch;
                }
            }
        }
        return DDSFormat.NOT_DDS;
    }

    private static class FormatItem implements Comparable<FormatItem> {

        private final String name;
        private final Long mask;
        private final long bits;


        public FormatItem(String name, long mask, long bits) {
            this.name = name;
            this.mask = mask;
            this.bits = bits;
        }

        @Override
        public String toString() {
            return name + bits;
        }

        public int compareTo(FormatItem o) {
            return o.mask.compareTo(mask);
        }
    }
}