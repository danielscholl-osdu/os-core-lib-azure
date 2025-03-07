// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.azure.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/***
 * Custom Executors class that returns a custom thread pool executor {@link CustomThreadPoolExecutor}.
 */
public final class CustomExecutors {

    /***
     * Private constructor -- this class should never be instantiated.
     */
    private CustomExecutors() {
    }

    /***
     * Returning new custom thread pool executor.
     * @param nThreads corresponding to corePoolSize and maxPoolSize.
     * @return instance of {@link CustomThreadPoolExecutor}
     */
    public static ExecutorService newFixedThreadPool(final int nThreads) {
        return new CustomThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }
}
