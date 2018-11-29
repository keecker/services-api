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

/**
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
 *
 * - Give a way to await in Java:
 *    ```
 *    fun <T> javaAvait(def: Deferred<T>) : T = runBlocking {
 *        def.await()
 *    }
 *    ```
 */

public class ClientsCanBeUsedAndMockedInJava {

    /**
     * A sample class using a Keecker Services Client. You want to test that interaction.
     */
    public static class MyClassIWantToTest {

        /**
         * Kotlin suspend functions are not really usable in Java, we advise to use the
         * asynchronous interface.
         */
        private ProjectorAsyncClient projectorClient;

        /**
         * If the client can be injected somehow, you will be able to test its interaction with
         * your Java code.
         *
         * @param projectorClient injected Keecker Services Client
         */
        MyClassIWantToTest(ProjectorAsyncClient projectorClient) {
            this.projectorClient = projectorClient;
        }

        /**
         * Interacts with the Keecker Projector
         */
        void helloProjector() {
            projectorClient.getStateAsync().get();
        }
    }

    @Test
    public void projectorClientInteraction() {
        ProjectorAsyncClient projectorClientMock = mock(ProjectorAsyncClient.class);
        MyClassIWantToTest myClassIWantToTest = new MyClassIWantToTest(projectorClientMock);

        CompletableFutureCompat<ProjectorState> deferred = new CompletableFutureCompat<>();
        deferred.complete(ProjectorState.getDefaultState());

        when(projectorClientMock.getStateAsync()).thenReturn(deferred);
        myClassIWantToTest.helloProjector();
        verify(projectorClientMock, times(1)).getStateAsync();
    }
}
