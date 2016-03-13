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

package co.phoenixlab.dn.dnptui.viewers.struct.msh;

import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;

public class Mesh {

    public static final int MESH_INFO_SIZE = 256 + 256 + 16 + 496;

    private String sceneName;
    private String meshName;
    private int numVertex;
    private int numIndex;
    private int unknownA;
    private int renderMode;
    private RenderMode renderModeEnum;
    private MeshData meshData;

    public Mesh(ByteBuffer byteBuffer, boolean hasBones, int version) {
        int startPos = byteBuffer.position();
        sceneName = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        meshName = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        numVertex = byteBuffer.getInt();
        numIndex = byteBuffer.getInt();
        unknownA = byteBuffer.getInt();
        renderMode = byteBuffer.getInt();
        renderModeEnum = RenderMode.fromId(renderMode);
        byteBuffer.position(startPos + MESH_INFO_SIZE);
        try {
            meshData = new MeshData(byteBuffer, this,
                    hasBones &&
                            renderMode >= RenderMode.TRIANGLES_ANIM.getId() &&
                            renderMode < RenderMode.TRIANGLES_UNK.getId(),
                    version);
        } catch (Exception e) {
            System.err.println("startpos " + startPos);
            throw e;
        }
    }

    public String getSceneName() {
        return sceneName;
    }

    public String getMeshName() {
        return meshName;
    }

    public int getNumVertex() {
        return numVertex;
    }

    public int getNumIndex() {
        return numIndex;
    }

    public int getUnknownA() {
        return unknownA;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public RenderMode getRenderModeEnum() {
        return renderModeEnum;
    }

    @Override
    public String toString() {
        return "{" +
                "sceneName='" + sceneName + '\'' +
                ", meshName='" + meshName + '\'' +
                ", numVertex=" + numVertex +
                ", numIndex=" + numIndex +
                ", unknownA=" + unknownA +
                ", renderMode=" + renderMode + " (" + renderModeEnum + ")" +
                ",\n\tmeshData=" + meshData +
                '}';
    }
}
