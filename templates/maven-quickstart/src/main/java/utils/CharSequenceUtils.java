/*
 * Copyright 2016 Dmitry Korotych (dkorotych at gmail dot com).
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
 */

package {{packageName}}.utils;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Methods for working with the character sequence.
 *
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
public final class CharSequenceUtils {

    /**
     * Hidden constructor.
     */
    private CharSequenceUtils() {
    }

    /**
     * Returns {@code true} if this sequence is {@code null} or it contains no
     * elements.
     *
     * @param sequence the character sequence, may be null
     * @return {@code true}, if this sequence is {@code null} or it contains no
     *     elements
     */
    public static boolean isEmpty(final CharSequence sequence) {
        return nullToEmpty(sequence).length() == 0;
    }

    /**
     * Returns {@code true} if this sequence is {@code null} or it contains only
     * whitespaces symbols.
     *
     * @param sequence the character sequence, may be null
     * @return {@code true}, if this sequence is {@code null} or it contains
     *     only whitespaces.
     * @see #trim(java.lang.CharSequence)
     */
    public static boolean isBlank(final CharSequence sequence) {
        return isEmpty(sequence) || isEmpty(trim(sequence));
    }

    /**
     * Returns {@code true} if this sequence contains any not whitespaces
     * symbols.
     *
     * @param sequence the character sequence, may be null
     * @return {@code true}, if this sequence contains any not whitespaces
     *     symbols.
     * @see #isBlank(java.lang.CharSequence)
     */
    public static boolean isNotBlank(final CharSequence sequence) {
        return !isBlank(sequence);
    }

    /**
     * Returns input sequence or empty string if sequence is {@code null}.
     *
     * @param sequence the character sequence, may be null
     * @return input sequence or empty string if sequence is {@code null}
     */
    public static CharSequence nullToEmpty(final CharSequence sequence) {
        if (sequence == null) {
            return "";
        } else {
            return sequence;
        }
    }

    /**
     * Returns a character sequence whose value is this sequence, with any
     * leading and trailing whitespace removed.
     *
     * @param sequence the character sequence, may be null
     * @return a character sequence without any leading and trailing whitespace.
     * @see String#trim()
     */
    public static CharSequence trim(final CharSequence sequence) {
        if (sequence != null) {
            int length = sequence.length();
            int start = 0;
            while (start < length
                    && trimThisChar(sequence.charAt(start))) {
                start++;
            }
            while (start < length
                    && trimThisChar(sequence.charAt(length - 1))) {
                length--;
            }
            if (start > 0 || length < sequence.length()) {
                return sequence.subSequence(start, length);
            } else {
                return sequence;
            }
        }
        return null;
    }

    /**
     * Capitalizes a character sequence changing the first letter to title case
     * as per {@link Character#toTitleCase(char)}. No other letters are changed.
     * A {@code null} input sequence returns {@code null}.
     * <pre>
     * CharSequenceUtils.capitalize(null)  = null
     * CharSequenceUtils.capitalize("")    = ""
     * CharSequenceUtils.capitalize("cat") = "Cat"
     * CharSequenceUtils.capitalize("cAt") = "CAt"
     * </pre>
     *
     * @param sequence the character sequence to capitalize, may be null
     * @return the capitalized character sequence, {@code null} if null String
     *     input
     * @see #uncapitalize(java.lang.CharSequence)
     */
    public static CharSequence capitalize(final CharSequence sequence) {
        return modifyFirstChar(sequence,
                Character::isTitleCase,
                Character::toTitleCase);
    }

    /**
     * Uncapitalizes a character sequence changing the first letter to title
     * case as per {@link Character#toLowerCase(char)}. No other letters are
     * changed. A {@code null} input String returns {@code null}.
     * <pre>
     * CharSequenceUtils.uncapitalize(null)  = null
     * CharSequenceUtils.uncapitalize("")    = ""
     * CharSequenceUtils.uncapitalize("Cat") = "cat"
     * CharSequenceUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param sequence the character sequence to uncapitalize, may be null
     * @return the uncapitalized character sequence, {@code null} if null String
     *     input
     * @see #capitalize(java.lang.CharSequence)
     */
    public static CharSequence uncapitalize(final CharSequence sequence) {
        return modifyFirstChar(sequence,
                Character::isLowerCase,
                Character::toLowerCase);
    }

    /**
     * Return {@code true}, if the searched character is a whitespace character.
     *
     * @param character Character
     * @return {@code true}, if the searched character is a whitespace
     */
    private static boolean trimThisChar(final char character) {
        return character <= ' '
                || character == '\u00A0';
    }

    /**
     * Modify the first character of the input string.
     * @param sequence the character sequence to modification
     * @param predicate function to check of the first character to the need for
     *     modification
     * @param processing function of modification
     * @return The altered sequence
     */
    private static CharSequence modifyFirstChar(final CharSequence sequence,
            final Predicate<Character> predicate,
            final UnaryOperator<Character> processing) {
        if (isEmpty(sequence)) {
            return sequence;
        }
        final char firstChar = sequence.charAt(0);
        if (predicate.test(firstChar)) {
            return sequence;
        }
        return String.valueOf(processing.apply(firstChar))
                + sequence.subSequence(1, sequence.length());
    }
}
