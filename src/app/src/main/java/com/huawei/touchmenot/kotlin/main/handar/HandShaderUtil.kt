/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.touchmenot.kotlin.main.handar

import android.opengl.GLES20
import android.util.Log
import com.huawei.touchmenot.kotlin.main.common.Constants

/**
 * This class provides code and programs for the shader related to hand rendering.
 *
 * @author hw
 * @since 2020-03-30
 */
internal object HandShaderUtil {
    private val TAG = HandShaderUtil::class.java.simpleName

    /**
     * Newline character.
     */
    val LS = System.lineSeparator()

    /**
     * Code for the hand vertex shader.
     */
    val HAND_VERTEX = ("uniform vec4 inColor;" + LS
            + "attribute vec4 inPosition;" + LS
            + "uniform float inPointSize;" + LS
            + "varying vec4 varColor;" + LS
            + "uniform mat4 inMVPMatrix;" + LS
            + "void main() {" + LS
            + "    gl_PointSize = inPointSize;" + LS
            + "    gl_Position = inMVPMatrix * vec4(inPosition.xyz, 1.0);" + LS
            + "    varColor = inColor;" + LS
            + "}")

    /**
     * Code for the hand fragment shader.
     */
    val HAND_FRAGMENT = ("precision mediump float;" + LS
            + "varying vec4 varColor;" + LS
            + "void main() {" + LS
            + "    gl_FragColor = varColor;" + LS
            + "}")

    fun createGlProgram(): Int {
        val vertex = loadShader(GLES20.GL_VERTEX_SHADER, HAND_VERTEX)
        if (vertex == Constants.INIT_ZERO) {
            return 0
        }
        val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, HAND_FRAGMENT)
        if (fragment == Constants.INIT_ZERO) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != Constants.INIT_ZERO) {
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(Constants.INIT_ONE)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, Constants.INIT_ZERO)
            if (linkStatus[Constants.INIT_ZERO] != GLES20.GL_TRUE) {
                Log.d(TAG, Constants.STR_COULD_NOT_LINK + GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = Constants.INIT_ZERO
            }
        }
        return program
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (Constants.INIT_ZERO != shader) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(Constants.INIT_ONE)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, Constants.INIT_ZERO)
            if (compiled[Constants.INIT_ZERO] == Constants.INIT_ZERO) {
                Log.d(TAG, Constants.STR_GL_ERROR_SHADER + shaderType)
                Log.d(TAG, Constants.STR_GLES + GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = Constants.INIT_ZERO
            }
        }
        return shader
    }
}