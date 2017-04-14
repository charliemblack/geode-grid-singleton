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

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;

import java.util.Properties;

/**
 * This function is intented to run on all hosts to report back which member is the GridSingleton
 * Created by Charlie Black on 4/12/17.
 */
public class TestWhoIsGridSingleton implements Function, Declarable {

    public static final String ID = "TestWhoIsGridSingleton";

    /**
     * The method which contains the logic to be executed. This method should be
     * thread safe and may be invoked more than once on a given member for a
     * single {@link Execution}.
     * The context provided to this function is the one which was built using {@linkplain Execution}.
     * The contexts can be data dependent or data-independent so user should check to see if the context
     * provided in parameter is instance of {@link RegionFunctionContext}.
     *
     * @param context as created by {@link Execution}
     * @since GemFire 6.0
     */
    @Override
    public void execute(FunctionContext context) {
        String result;

        if(GridSingleton.getInstance() == null){
            result = "Grid singleton is null????";
        }else{
            result = Boolean.toString(GridSingleton.getInstance().isGridSingleton());
        }
        context.getResultSender().lastResult(result);
    }

    /**
     * Specifies whether the function sends results while executing.
     * The method returns false if no result is expected.<br>
     * <p>
     * If {@link Function#hasResult()} returns false,
     * {@link ResultCollector#getResult()} throws {@link FunctionException}.
     * </p>
     * <p>
     * If {@link Function#hasResult()} returns true,
     * {@link ResultCollector#getResult()} blocks and waits for the
     * result of function execution
     * </p>
     *
     * @return whether this function returns a Result back to the caller.
     * @since GemFire 6.0
     */
    @Override
    public boolean hasResult() {
        return true;
    }

    /**
     * Return a unique function identifier, used to register the function
     * with {@link FunctionService}
     *
     * @return string identifying this function
     * @since GemFire 6.0
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * Initializes a user-defined object using the given properties.
     * Note that any uncaught exception thrown by this method will cause
     * the <code>Cache</code> initialization to fail.
     *
     * @param props Contains the parameters declared in the declarative xml
     *              file.
     * @throws IllegalArgumentException If one of the configuration options in <code>props</code>
     *                                  is illegal or malformed.
     */
    @Override
    public void init(Properties props) {

    }
}
