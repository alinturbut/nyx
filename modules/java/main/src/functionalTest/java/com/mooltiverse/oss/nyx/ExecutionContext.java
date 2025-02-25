/*
 * Copyright 2020 Mooltiverse
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
package com.mooltiverse.oss.nyx;

import java.io.File;

/**
 * This interface models an execution context that allows running the Nyx executable from within a specific environment.
 *
 * This interface can be implemented by different contexts in order to run a certain command in a context specific way.
 */
public interface ExecutionContext {
    /**
     * Returns the command object used to run the executable.
     * 
     * @param repoDir the directory containing the Git repository
     * 
     * @return the command instance
     * 
     * @throws any exception that may be thrown when instantiating the command
     */
    Command getCommand(File repoDir)
        throws Exception;
}
