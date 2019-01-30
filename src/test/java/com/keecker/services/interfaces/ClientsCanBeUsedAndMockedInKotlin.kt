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
 * Created by Cyril Lugan on 2018-11-29.
 */

package com.keecker.services.interfaces

import com.keecker.services.interfaces.projection.ProjectorCoroutineClient
import com.keecker.services.interfaces.projection.ProjectorState
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.*

class SomeLogicIWantToTest {

    // Projector client that you want to be mocked. It is initialized by onCreate when
    // running normally, or initialized manually by a mock when unit testing.
    lateinit var projectorClient: ProjectorCoroutineClient

    // A typical way to get a projector client in an Android Service / Activity would be:

    // fun onCreate() {
    //    projectorClient = KeeckerServices.getProjectorClient(this)
    // }

    suspend fun helloProjector() {
        projectorClient.getState()
    }
}

class ClientsCanBeUsedAndMockedInKotlin {

    @Test
    fun `client interacts with projector`() = runBlocking<Unit> {
        val someLogicIWantToTest = SomeLogicIWantToTest()

        val projectorClientMock = mock(ProjectorCoroutineClient::class.java)
        someLogicIWantToTest.projectorClient = projectorClientMock
        `when`(projectorClientMock.getState()).thenReturn(ProjectorState.defaultState)

        someLogicIWantToTest.helloProjector()

        verify(projectorClientMock, times(1)).getState()
    }
}
