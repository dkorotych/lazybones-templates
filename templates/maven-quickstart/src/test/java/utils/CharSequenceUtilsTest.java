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
import junitparams.naming.TestCaseName;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
@RunWith(JUnitParamsRunner.class)
public class CharSequenceUtilsTest {

    @Test
    @Parameters
    public void testIsBlank(CharSequence sequence, boolean expected) {
        Assert.assertThat(CharSequenceUtils.isBlank(sequence), CoreMatchers.is(expected));
    }

    @Test
    @Parameters(method = "parametersForTestIsBlank")
    public void testIsNotBlank(CharSequence sequence, boolean expected) {
        Assert.assertThat(CharSequenceUtils.isNotBlank(sequence), CoreMatchers.not(CoreMatchers.is(expected)));
    }

    @Test
    @Parameters
    public void testIsEmpty(CharSequence sequence, boolean expected) {
        Assert.assertThat(CharSequenceUtils.isEmpty(sequence), CoreMatchers.is(expected));
    }

    @Test
    @Parameters
    public void testNullToEmpty(CharSequence sequence, CharSequence expected) {
        Assert.assertThat(CharSequenceUtils.nullToEmpty(sequence), CoreMatchers.is(expected));
    }

    @Test
    @Parameters
    public void testCapitalize(CharSequence sequence, CharSequence expected) {
        Assert.assertThat(CharSequenceUtils.capitalize(sequence), CoreMatchers.equalTo(expected));
    }

    @Test
    @Parameters
    public void testUncapitalize(CharSequence sequence, CharSequence expected) {
        Assert.assertThat(CharSequenceUtils.uncapitalize(sequence), CoreMatchers.equalTo(expected));
    }

    @Test
    @Parameters
    @TestCaseName("{method} - {index}")
    public void testTrim(CharSequence sequence, CharSequence expected) {
        Assert.assertThat(CharSequenceUtils.trim(sequence), CoreMatchers.is(expected));
    }

    private Object[] parametersForTestIsBlank() {
        return new Object[]{
            new Object[]{
                null, true
            },
            new Object[]{
                "", true
            },
            new Object[]{
                "    ", true
            },
            new Object[]{
                "\t   \n", true
            },
            new Object[]{
                "\n\n\t\n\n\n    \n\n\n", true
            },
            new Object[]{
                "     fg ", false
            },
            new Object[]{
                "43", false
            },
            new Object[]{
                "h7 \t", false
            }
        };
    }

    private Object[] parametersForTestIsEmpty() {
        return new Object[]{
            new Object[]{
                null, true
            },
            new Object[]{
                "", true
            },
            new Object[]{
                "    ", false
            },
            new Object[]{
                "\t   \n", false
            },
            new Object[]{
                "43", false
            }
        };
    }

    private Object[] parametersForTestNullToEmpty() {
        return new Object[]{
            new Object[]{
                null, ""
            },
            new Object[]{
                (StringBuffer) null, ""
            },
            new Object[]{
                (StringBuilder) null, ""
            },
            new Object[]{
                "", ""
            },
            new Object[]{
                "    ", "    "
            },
            new Object[]{
                "\t   \n", "\t   \n"
            },
            new Object[]{
                "43", "43"
            },};
    }

    private Object[] parametersForTestCapitalize() {
        return new Object[]{
            new Object[]{
                null, null
            },
            new Object[]{
                "", ""
            },
            new Object[]{
                "    ", "    "
            },
            new Object[]{
                "\t   \n", "\t   \n"
            },
            new Object[]{
                "43", "43"
            },
            new Object[]{
                "parameters", "Parameters"
            },
            new Object[]{
                "Parameters", "Parameters"
            },
            new Object[]{
                "tEST", "TEST"
            },
            new Object[]{
                "sOmE", "SOmE"
            },
            new Object[]{
                "parametersForTestCapitalize", "ParametersForTestCapitalize"
            },
            new Object[]{
                "ParametersForTestCapitalize", "ParametersForTestCapitalize"
            }
        };
    }

    private Object[] parametersForTestUncapitalize() {
        return new Object[]{
            new Object[]{
                null, null
            },
            new Object[]{
                "", ""
            },
            new Object[]{
                "    ", "    "
            },
            new Object[]{
                "\t   \n", "\t   \n"
            },
            new Object[]{
                "43", "43"
            },
            new Object[]{
                "parameters", "parameters"
            },
            new Object[]{
                "Parameters", "parameters"
            },
            new Object[]{
                "TEST", "tEST"
            },
            new Object[]{
                "SOmE", "sOmE"
            },
            new Object[]{
                "parametersForTestCapitalize", "parametersForTestCapitalize"
            },
            new Object[]{
                "ParametersForTestCapitalize", "parametersForTestCapitalize"
            }
        };
    }

    private Object[] parametersForTestTrim() {
        return new Object[]{
            new Object[]{
                null, null
            },
            new Object[]{
                "", ""
            },
            new Object[]{
                "    ", ""
            },
            new Object[]{
                "\t   \n", ""
            },
            new Object[]{
                "43", "43"
            },
            new Object[]{
                "\n\n\t\n\n\n    \n\n\n", ""
            },
            new Object[]{
                "     fg ", "fg"
            },
            new Object[]{
                "h7 \t", "h7"
            },
            new Object[]{
                "\u00A0h7 \t\u00A0", "h7"
            },
            new Object[]{
                "\u00A0\u000B\u00A0\u00A0\u00A0\u000B\u000B", ""
            }
        };
    }
}
