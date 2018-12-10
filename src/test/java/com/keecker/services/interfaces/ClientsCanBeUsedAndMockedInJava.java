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

package com.keecker.services.interfaces;

import com.keecker.services.projection.interfaces.ProjectorAsyncClient;
import com.keecker.services.projection.interfaces.ProjectorState;
import com.keecker.services.utils.CompletableFutureCompat;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Some tests written while developing the API, to check the interoperability between kotlin
 * and Java.
 * If the projector client can be injected somehow, you will be able to mock it.
 *
 * The goal is to provide a clean coroutine based Kotlin API while allowing it to be used in Java.
 *
 * - Using kotlinx-coroutines-jdk8 would be the cleanest way, but it forces to use a minSdk 24
 *   on Android. That would be OK for Keecker, but not for existing third party apps wanting to
 *   support Keecker.
 *    ```
 *    fun someRemoteCallAsync(): CompletableFuture<Result> = future {
 *        someRemoteCall()
 *    }
 *   ```
 *   Instead we defined a custom [CompletableFutureCompat].
 */

public class ClientsCanBeUsedAndMockedInJava {

    public static class SomeLogicIWantToTest {

        // Projector client that you want to be mocked. It is initialized by onCreate when
        // running normally, or initialized manually by a mock when unit testing.
        private ProjectorAsyncClient projectorClient;

        // A typical way to get a projector client in an Android Service / Activity would be
        //
        // void onCreate() {
        //    projectorClient = KeeckerServices.getProjectorClient(this);
        //}

        void helloProjector() {
            projectorClient.getStateAsync().get();
        }
    }

    @Test
    public void projectorClientInteraction() {
        // Mock the projector client to return a completed future containing
        // a default projector state.
        ProjectorAsyncClient projectorClientMock = mock(ProjectorAsyncClient.class);
        CompletableFutureCompat<ProjectorState> deferred = new CompletableFutureCompat<>();
        deferred.complete(ProjectorState.getDefaultState());
        when(projectorClientMock.getStateAsync()).thenReturn(deferred);

        SomeLogicIWantToTest myClassIWantToTest = new SomeLogicIWantToTest();
        myClassIWantToTest.projectorClient = projectorClientMock;

        myClassIWantToTest.helloProjector();
        verify(projectorClientMock, times(1)).getStateAsync();
    }
}
