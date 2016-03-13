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

package co.phoenixlab.dn.dnptui.viewers.stageini.struct;

import co.phoenixlab.dn.dnptui.viewers.util.BufferUtils;
import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Prop implements Comparable<Prop> {

    public static final int TYPE_TAGGED = 0x068A;
    public static final int TYPE_LIGHT = 0x06C0;
    public static final int TYPE_NORMAL = 0x0678;
    public static final int TYPE_BUFF = 0x0681;
    private int offset;
    private Type type;
    private int typeId;
    private String sknFile;
    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;
    private int unknownB;
    private int objectId;
    private byte[] unknownD;
    private int numTags;
    private Map<Integer, Integer> tags;

    public Prop(ByteBuffer byteBuffer) {
        tags = new HashMap<>();
        offset = byteBuffer.position();
        typeId = byteBuffer.getInt();
        type = Type.fromId(typeId);
        sknFile = DNStringUtils.readFixedLengthNTString(byteBuffer, 64);
        position = readVector3f(byteBuffer);
        rotation = readVector3f(byteBuffer);
        scale = readVector3f(byteBuffer);
        unknownB = byteBuffer.getInt();
        objectId = byteBuffer.getInt();
        int bufsize = type.getBufSize();
        unknownD = new byte[bufsize];
        byteBuffer.get(unknownD);
        if (type == Type.TAGGED) {
            numTags = byteBuffer.getInt();
            for (int i = 0; i < numTags; i++) {
                tags.put(byteBuffer.getInt(), byteBuffer.getInt());
            }
            BufferUtils.skip(byteBuffer, 2);
        }
        //  TODO
    }

    @Override
    public String toString() {
        String s = String.format("PropInfo 0x%08X {\n\t", offset) +
                "type=" + type +
                String.format(",\n\ttypeId=0x%04X", typeId) +
                ",\n\tsknFile='" + sknFile + '\'' +
                ",\n\tposition=" + position +
                ",\n\trotation=" + rotation +
                ",\n\tscale=" + scale +
                ",\n\tunknownB=" + unknownB +
                ",\n\tobjectId=" + objectId;
        if (typeId == TYPE_TAGGED) {
            s += ",\n\ttags=" + tags.toString();
        }
        s += "\n}";
        return s;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getSknFile() {
        return sknFile;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public int getUnknownB() {
        return unknownB;
    }

    public int getObjectId() {
        return objectId;
    }

    public byte[] getUnknownD() {
        return unknownD;
    }

    public int getOffset() {
        return offset;
    }

    public int getNumTags() {
        return numTags;
    }

    public Map<Integer, Integer> getTags() {
        return tags;
    }

    public Type getType() {
        return type;
    }

    private Vector3f readVector3f(ByteBuffer byteBuffer) {
        return new Vector3f(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
    }

    @Override
    public int compareTo(Prop o) {
        return Integer.compareUnsigned(this.objectId, o.objectId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Prop prop = (Prop) o;
        return objectId == prop.objectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }

    public enum Type {
        DEFAULT(TYPE_NORMAL, 1548),
        TAGGED(TYPE_TAGGED, 1544),
        LIGHT(TYPE_LIGHT, 1620),
        BUFF(TYPE_BUFF, 1557),
        UNKNOWN(0, 1548);

        private final int id;
        private final int bufSize;

        Type(int id, int bufSize) {
            this.id = id;
            this.bufSize = bufSize;
        }

        public int getId() {
            return id;
        }

        public int getBufSize() {
            return bufSize;
        }

        public static Type fromId(int id) {
            for (Type type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
