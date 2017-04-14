/*
 * Copyright [2017] Charlie Black
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.demo;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.distributed.DistributedLockService;

import java.util.Properties;

/**
 * This Grid Singleton is intended to be instantiated by the Geode cache initializer.   The cache initializer
 * is callback that happens right before the cache goes live.
 * <p>
 * Created by Charlie Black on 4/13/17.
 */
public class GridSingleton implements Declarable {

    private static GridSingleton instance;

    private boolean gridSingleton = false;

    public static GridSingleton getInstance() {
        return instance;
    }

    public boolean isGridSingleton() {
        return gridSingleton;
    }

    /**
     * Initializes a user-defined object using the given properties. Note that any uncaught exception
     * thrown by this method will cause the <code>Cache</code> initialization to fail.
     *
     * @param props Contains the parameters declared in the declarative xml file.
     * @throws IllegalArgumentException If one of the configuration options in <code>props</code> is
     *                                  illegal or malformed.
     */
    @Override
    public void init(Properties props) {

        instance = this;

        //We must allow this method to return otherwise Geode will not start up.
        Runnable runnable = () -> {
            Cache cache = CacheFactory.getAnyInstance();
            DistributedLockService lockService = DistributedLockService.create("GridSingleton_Service", cache.getDistributedSystem());
            lockService.lock("GridSingleton_lock", -1, -1);
            gridSingleton = true;
        };

        Thread thread = new Thread(runnable, "GridSingleton_Thread");
        thread.setDaemon(true);
        thread.start();

    }
}
