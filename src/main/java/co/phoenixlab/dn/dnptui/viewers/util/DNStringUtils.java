/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package co.phoenixlab.dn.dnptui.viewers.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DNStringUtils {

    public static String readLPNTString(ByteBuffer byteBuffer) {
        int len = byteBuffer.getInt();
        byte[] buf = new byte[len];
        byteBuffer.get(buf);
        return new String(buf, 0, buf.length - 1, StandardCharsets.UTF_8);
    }

    public static String readLPString(ByteBuffer byteBuffer) {
        int len = byteBuffer.getInt();
        byte[] buf = new byte[len];
        byteBuffer.get(buf);
        return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
    }

    public static String readFixedLengthString(ByteBuffer byteBuffer, int size) {
        byte[] buf = new byte[size];
        byteBuffer.get(buf);
        return new String(buf, StandardCharsets.UTF_8).trim();
    }


    public static String readFixedLengthNTString(ByteBuffer byteBuffer, int size) {
        byte[] buf = new byte[size];
        byteBuffer.get(buf);
        int end = buf.length;
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == 0) {
                end = i;
                break;
            }
        }
        return new String(buf, 0, end, StandardCharsets.UTF_8);
    }
}
