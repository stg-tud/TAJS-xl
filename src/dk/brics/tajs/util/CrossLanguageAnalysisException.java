/*
 * Copyright 2009-2020 Aarhus University
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

package dk.brics.tajs.util;

import dk.brics.tajs.analysis.Analysis;
import dk.brics.tajs.lattice.Context;
import dk.brics.tajs.solver.BlockAndContext;

/**
 * Exception for internal analysis errors.
 */
public class CrossLanguageAnalysisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;

    private final Analysis analysis;

    private BlockAndContext blockAndContext;

    /**
     * Constructs a new exception.
     */
    public CrossLanguageAnalysisException(String message, Analysis analysis) {
        this.message = message;
        this.analysis = analysis;
    }

    public CrossLanguageAnalysisException(String message, BlockAndContext blockAndContext, Analysis analysis) {
        this.message = message;
        this.blockAndContext = blockAndContext;
        this.analysis = analysis;
    }

    public Analysis getAnalysis(){
        return analysis;
    }

    public String getMessage() {return message;}

    public BlockAndContext getBlockAndContext() {
        return blockAndContext;
    }
}
