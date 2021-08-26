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

import com.huawei.hiar.ARHand

/**
 * Rendering hand AR type related data.
 *
 * @author HW
 * @since 2020-05-22
 */
internal interface HandRelatedDisplay {
    /**
     * Init render.
     */
    fun init()

    /**
     * Render objects, call per frame
     *
     * @param hands ARHands
     * @param projectionMatrix Camera projection matrix.
     */
    fun onDrawFrame(hands: Collection<ARHand>, projectionMatrix: FloatArray?)
}