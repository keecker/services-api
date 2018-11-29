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
 * Created by Cyril Lugan <cyril@keecker.com> on 2018-11-29.
 */

package com.keecker.services.interfaces

import com.keecker.services.projection.interfaces.ProjectorCoroutineClient
import com.keecker.services.projection.interfaces.ProjectorState
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.mockito.Mockito.*

class MyClassIWantToTest(val projectorClient: ProjectorCoroutineClient) {
    fun helloProjector() = runBlocking {
        projectorClient.getState()
    }
}

class ClientsCanBeUsedAndMockedInKotlin {

    @Test
    fun `client interacts with projector`() = runBlocking<Unit> {
        val projectorClientMock = mock(ProjectorCoroutineClient::class.java)
        val myClassIWantToTest = MyClassIWantToTest(projectorClientMock)
        `when`(projectorClientMock.getState()).thenReturn(ProjectorState.defaultState)

        myClassIWantToTest.helloProjector()
        verify(projectorClientMock, times(1)).getState()
    }
}
