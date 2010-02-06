/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: BitwiseXor.java, Class: BitwiseXor
 * Last modified: 2010-02-06
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ansorgit.plugins.bash.lang.parser.arithmetic;

/**
 * User: jansorg
 * Date: Feb 6, 2010
 * Time: 4:29:05 PM
 */
class BitwiseXor extends AbstractRepeatedExpr {
    BitwiseXor() {
        super(new BitwiseAnd(), ARITH_XOR);
    }
}