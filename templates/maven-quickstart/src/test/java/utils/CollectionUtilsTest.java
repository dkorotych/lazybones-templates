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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
@RunWith(JUnitParamsRunner.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class CollectionUtilsTest {

    @Test
    @Parameters
    public void testIsNotEmptyCollection(Collection<?> collection, boolean expected) {
        Assert.assertThat(CollectionUtils.isNotEmpty(collection), CoreMatchers.is(expected));
    }

    @Test
    @Parameters
    public void testIsNotEmptyMap(Map<?, ?> map, boolean expected) {
        Assert.assertThat(CollectionUtils.isNotEmpty(map), CoreMatchers.is(expected));
    }

    @Test
    @Parameters
    public void testIsNullOrEmpty(Collection<?> collection, boolean expected) {
        Assert.assertThat(CollectionUtils.isNullOrEmpty(collection), CoreMatchers.is(expected));
    }

    @Test
    @Parameters
    public void testGetSafeList(List<?> list) {
        if (list == null) {
            final List<?> safeList = CollectionUtils.getSafeList(list);
            Assert.assertThat(safeList, CoreMatchers.notNullValue(List.class));
            Assert.assertThat(safeList.isEmpty(), CoreMatchers.is(true));
        } else {
            Assert.assertThat(CollectionUtils.getSafeList(list), CoreMatchers.sameInstance(list));
        }
    }

    @Test
    @Parameters
    public void testGetSafeListVarargs(Object[] items) {
        if (items == null) {
            final List<?> safeList = CollectionUtils.getSafeList(items);
            Assert.assertThat(safeList, CoreMatchers.notNullValue(List.class));
            Assert.assertThat(safeList.isEmpty(), CoreMatchers.is(true));
        } else {
            Assert.assertThat(CollectionUtils.getSafeList(items), CoreMatchers.allOf(CoreMatchers.hasItems(items)));
        }
    }

    @Test
    @Parameters
    public void testTransformWithoutNull(Iterable iterable, Function transformer, List expected) {
        Assert.assertThat(CollectionUtils.transformWithoutNull(iterable, transformer), CoreMatchers.is(expected));
    }

    private Object[] parametersForTestIsNotEmptyCollection() {
        return Provider.merge(
                Provider.transform(Provider.emptyCollection(),
                        o -> new Object[]{
                            ((Object[]) o)[0], false
                        }),
                Provider.transform(Provider.notEmptyCollection(),
                        o -> new Object[]{
                            ((Object[]) o)[0], true
                        }));
    }

    private Object[] parametersForTestIsNotEmptyMap() {
        return Provider.merge(
                Provider.transform(Provider.emptyMap(),
                        o -> new Object[]{
                            ((Object[]) o)[0], false
                        }),
                Provider.transform(Provider.notEmptyMap(),
                        o -> new Object[]{
                            ((Object[]) o)[0], true
                        }));
    }

    private Object[] parametersForTestIsNullOrEmpty() {
        return Provider.transform(parametersForTestIsNotEmptyCollection(), o -> {
            Object[] params = (Object[]) o;
            params[1] = !(boolean) params[1];
            return params;
        });
    }

    private Object[] parametersForTestGetSafeList() {
        return Provider.merge(Provider.emptyList(), Provider.notEmptyList());
    }

    private Object[] parametersForTestGetSafeListVarargs() {
        return new Object[]{
            new Object[]{
                null
            },
            new Object[]{
                (String) null
            },
            new Object[]{
                1, 2, 3, 4, 5, 6, 7, 8
            },
            new Object[]{
                "", "s", "f", "gfh"
            }
        };
    }

    private Object[] parametersForTestTransformWithoutNull() {
        return new Object[]{
            new Object[]{
                null, null, Collections.emptyList()
            },
            new Object[]{
                new ArrayList<>(), null, Collections.emptyList()
            },
            new Object[]{
                new HashSet<>(), null, Collections.emptyList()
            },
            new Object[]{
                null, (Function) o -> new Object(), Collections.emptyList()
            },
            new Object[]{
                Arrays.asList(null, "first", null, null, null, "second", null),
                (Function<String, Character>) s -> s != null ? s.charAt(0) : null,
                Arrays.asList('f', 's')
            }
        };
    }

    public static class Provider {

        public static Object[] emptyList() {
            return new Object[]{
                new Object[]{
                    (List) null
                },
                new Object[]{
                    Collections.emptyList()
                },
                new Object[]{
                    new ArrayList<>()
                },
                new Object[]{
                    new LinkedList<>()
                },
                new Object[]{
                    new Vector<>()
                }

            };
        }

        public static Object[] notEmptyList() {
            return new Object[]{
                new Object[]{
                    Collections.singletonList(new Object())
                },
                new Object[]{
                    Collections.nCopies(10, new Object())
                },
                new Object[]{
                    new LinkedList<>(Arrays.asList("1", "2", "3"))
                },
                new Object[]{
                    new ArrayList<>(Arrays.asList("9", "8", "7"))
                },
                new Object[]{
                    Arrays.asList("", "ffdss", "dfgdg", "frty56y")
                }
            };
        }

        public static Object[] emptySet() {
            return new Object[]{
                new Object[]{
                    (Set) null
                },
                new Object[]{
                    Collections.emptySet()
                },
                new Object[]{
                    Collections.emptySortedSet()
                },
                new Object[]{
                    new HashSet<>()
                },
                new Object[]{
                    new LinkedHashSet<>()
                },
                new Object[]{
                    new TreeSet<>()
                }

            };
        }

        public static Object[] notEmptySet() {
            return new Object[]{
                new Object[]{
                    Collections.singleton(new Object())
                },
                new Object[]{
                    new HashSet<>(Arrays.asList("1", "2", "3"))
                },
                new Object[]{
                    new TreeSet<>(Arrays.asList("9", "8", "7"))
                }
            };
        }

        public static Object[] emptyCollection() {
            return merge(emptyList(), emptySet());
        }

        public static Object[] notEmptyCollection() {
            return merge(notEmptyList(), notEmptySet());
        }

        public static Object[] emptyMap() {
            return new Object[]{
                new Object[]{
                    (Map) null
                },
                new Object[]{
                    Collections.emptyMap()
                },
                new Object[]{
                    Collections.emptySortedMap()
                },
                new Object[]{
                    new TreeMap<>()
                },
                new Object[]{
                    new HashMap<>()
                },
                new Object[]{
                    new Hashtable<>()
                }
            };
        }

        @SuppressWarnings("serial")
        public static Object[] notEmptyMap() {
            return new Object[]{
                new Object[]{
                    new HashMap<>(Collections.singletonMap("key", "value"))
                },
                new Object[]{
                    Collections.singletonMap(new Object(), "fsdgfg")
                },
                new Object[]{
                    new HashMap<String, String>() {
                        {
                            put("aaa", "dfgdfg");
                            put("aaagf", "sdfdgdfgdfg");
                        }
                    }
                }
            };
        }

        public static Object[] transform(Object[] parameters, Function<Object, Object[]> function) {
            return Arrays.stream(parameters)
                    .map(function)
                    .collect(Collectors.toList())
                    .toArray();
        }

        public static Object[] merge(Object[] first, Object[] second) {
            List<Object> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(first));
            parameters.addAll(Arrays.asList(second));
            return parameters.toArray();
        }
    }
}
