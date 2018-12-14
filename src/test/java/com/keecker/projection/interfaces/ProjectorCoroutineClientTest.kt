/*
 * Copyright (C) 2018 KEECKER SAS (www.keecker.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-11-19.
 */

package com.keecker.projection.interfaces

import kotlinx.coroutines.runBlocking
import org.junit.Test

class ProjectorCoroutineClientTest {

    @Test
    fun `lazily connects to service on fist client request`() : Unit = runBlocking {

    }

    @Test
    fun `resubscribes to state when connection crashes`() : Unit = runBlocking {}

    @Test
    fun `retries to get state when remote service not available`() : Unit = runBlocking {}

    @Test
    fun `retries to set state when remote service not available`() : Unit = runBlocking {}

    @Test
    fun `cannot be used in the main thread`() : Unit = runBlocking {

    }
}