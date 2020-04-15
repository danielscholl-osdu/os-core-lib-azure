//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.common;

import java.util.Objects;

/**
 * A collection of useful validators.
 */
public final class Validators {

    /**
     * This class is all static methods - no need for a constructor to be public.
     */
    private Validators() {
    }

    /**
     * Asserts that data is non null.
     *
     * @param data      The field which should not be null or empty
     * @param fieldName The name of the field, used for helpful exception
     *                  messaging
     * @throws NullPointerException if the data is null
     */
    public static void checkNotNull(
            final Object data, final String fieldName) {
        Objects.requireNonNull(data, fieldName + " cannot be null!");
    }

    /**
     * Throws an
     * Throws a {@link NullPointerException} if it is null.
     *
     * @param data      The field which should not be null or empty
     * @param fieldName The name of the field, used for helpful exception
     *                  messaging
     * @throws IllegalArgumentException  if the string is empty
     * @throws NullPointerException  if the string is null
     */
    public static void checkNotNullAndNotEmpty(
            final String data, final String fieldName) {
        checkNotNull(data, fieldName);
        if (data.chars().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(fieldName + " cannot be empty!");
        }
    }
}
