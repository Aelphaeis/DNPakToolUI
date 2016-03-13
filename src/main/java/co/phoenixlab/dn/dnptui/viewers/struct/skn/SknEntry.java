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

import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SknEntry {

    private String entryName;
    private String fxFileName;
    private float unknownA;
    private byte[] unknownB;
    private int numParams;
    private Map<String, Parameter> parameters;

    public SknEntry() {
        parameters = new HashMap<>();
    }

    public SknEntry(ByteBuffer byteBuffer) {
        this();
        entryName = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        fxFileName = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        unknownA = byteBuffer.getFloat();
        unknownB = new byte[512 - 4];
        byteBuffer.get(unknownB);
        numParams = byteBuffer.getInt();
        for (int i = 0; i < numParams; i++) {
            String paramName = DNStringUtils.readLPNTString(byteBuffer);
            int dataTypeId = byteBuffer.getInt();
            DataType dataType = DataType.valueOf(dataTypeId);
            Object value;
            switch (dataType) {
                case FLOAT:
                    value = byteBuffer.getFloat();
                    break;
                case VEC4F:
                    value = new float[]{byteBuffer.getFloat(),
                            byteBuffer.getFloat(),
                            byteBuffer.getFloat(),
                            byteBuffer.getFloat()};
                    break;
                case STRING:
                    value = DNStringUtils.readLPNTString(byteBuffer);
                    break;
                case UNKNOWN:
                default:
                    throw new UnsupportedOperationException("Unknown param type " + dataTypeId);
            }
            parameters.put(paramName, dataType.createParam(paramName, value));
        }

    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getFxFileName() {
        return fxFileName;
    }

    public void setFxFileName(String fxFileName) {
        this.fxFileName = fxFileName;
    }

    public float getUnknownA() {
        return unknownA;
    }

    public void setUnknownA(float unknownA) {
        this.unknownA = unknownA;
    }

    public byte[] getUnknownB() {
        return unknownB;
    }

    public void setUnknownB(byte[] unknownB) {
        this.unknownB = unknownB;
    }

    public int getNumParams() {
        return numParams;
    }

    public void setNumParams(int numParams) {
        this.numParams = numParams;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "SknEntry{\n\t" +
                "entryName='" + entryName + '\'' +
                ",\n\tfxFileName='" + fxFileName + '\'' +
                ",\n\tunknownA=" + unknownA +
                ",\n\tnumParams=" + numParams +
                ",\n\tparameters=" +
                parameters.values().stream().map(Parameter::toString).collect(Collectors.joining(", ")) +
                '}';
    }

    public enum DataType {
        FLOAT(1, FloatParameter::new),
        VEC4F(2, Vec4FParameter::new),
        STRING(3, StringParameter::new),
        UNKNOWN(-1, Parameter::new);

        private final int id;
        private final BiFunction<String, Object, Parameter> paramFactory;

        DataType(int id, BiFunction<String, Object, Parameter> paramFactory) {
            this.id = id;
            this.paramFactory = paramFactory;
        }

        public int getId() {
            return id;
        }

        public Parameter createParam(String key, Object value) {
            Parameter p = paramFactory.apply(key, value);
            p.setDataType(this);
            return p;
        }

        public static DataType valueOf(int id) {
            for (DataType dataType : values()) {
                if (dataType.id == id) {
                    return dataType;
                }
            }
            return UNKNOWN;
        }
    }
}
