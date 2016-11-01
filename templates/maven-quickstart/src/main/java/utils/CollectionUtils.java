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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Methods of working with collections.
 *
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
public final class CollectionUtils {

    /**
     * Hidden constructor.
     */
    private CollectionUtils() {
    }

    /**
     * Returns {@code true} if this collection contains elements.
     *
     * @param collection a group of objects
     * @return {@code true}, if this collection contains elements
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Returns {@code true} if this collection contains elements.
     *
     * @param map a group of objects
     * @return {@code true}, if this collection contains elements
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Returns {@code true} if this collection is {@code null} or it contains no
     * elements.
     *
     * @param collection a group of objects
     * @return {@code true}, if this collection is {@code null} or it contains
     *     no elements
     */
    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Return the "safe" list. This method never returns a value of
     * {@code null}, if the input list is equal to {@code null}, then instead it
     * will return an empty list
     *
     * @param <T> Type of list of elements
     * @param list List of elements
     * @return The input or an empty list
     */
    public static <T> List<T> getSafeList(final List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.<T>emptyList());
    }

    /**
     * Return the "safe" list. This method never returns a value of {@code null}
     *
     * @param <T> Type of list of elements
     * @param items Elements
     * @return The input sequence transformed to list or an empty list
     * @see Collections#emptyList()
     * @see Arrays#asList(java.lang.Object...)
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> getSafeList(final T... items) {
        if (items == null) {
            return Collections.<T>emptyList();
        } else {
            return Arrays.asList(items);
        }
    }

    /**
     * Convert a sequence of elements to the list.
     *
     * @param <I> Type of objects for input collection
     * @param <O> Type of objects for output collection
     * @param iterable The sequence of elements to convert. If the input
     *     sequence is empty, then the resulting collection will be empty
     * @param transformer The conversion function. If the transfer function is
     *     not set, the resulting collection will be empty
     * @return Reformed collection of input elements
     */
    public static <I, O> List<O> transformWithoutNull(
            final Iterable<I> iterable,
            final Function<I, O> transformer) {
        return StreamSupport
                .stream(Optional
                        .ofNullable(iterable)
                        .orElse(Collections.<I>emptySet())
                        .spliterator(), false)
                .map(Optional
                        .ofNullable(transformer)
                        .orElse(i -> null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
