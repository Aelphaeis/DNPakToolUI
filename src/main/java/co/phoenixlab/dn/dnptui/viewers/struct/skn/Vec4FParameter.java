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

package co.phoenixlab.dn.dnptui.viewers.struct.skn;

import org.joml.Vector4f;

import java.util.Arrays;

public class Vec4FParameter extends Parameter {

    private Vector4f vector4f;

    public Vec4FParameter(String name, Object value) {
        super(name, value);
        float[] v = asFloatArray();
        vector4f = new Vector4f(v[0], v[1], v[2], v[3]);
    }

    public Vec4FParameter(String name, float[] value) {
        super(name, value);
    }

    public float[] asFloatArray() {
        return (float[]) value;
    }

    public Vector4f asVector4f() {
        return vector4f;
    }

    @Override
    public String valueToString() {
        return Arrays.toString(asFloatArray());
    }
}
