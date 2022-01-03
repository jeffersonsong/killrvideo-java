/*
 * Copyright (C) 2012-2016 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.killrvideo.dse.utils

import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

/**
 *
 * In Cassandra there is no difference between an empty collection/map
 * and a null value for collection/map
 * <br></br>
 * <br></br>
 * In Java we do make the difference. This annotations allows mapping null values from
 * **Cassandra** into empty collection & map.
 * <br></br>
 * <br></br>
 * Empty list will default to **ArrayList**.<br></br>
 * Empty set will default to **HashSet**.<br></br>
 * Empty map will default to **HashMap**.<br></br>
 *
 * <pre class="code">`
 * @Column
 * **@EmptyCollectionIfNull**
 * private List<String> friends
`</pre> *
 * <br></br>
 *
 * This annotation can be used for **nested** collections too:
 * <br></br>
 * <pre class="code">`
 * @Column
 * private Tuple2<Integer,**@EmptyCollectionIfNull** List<String>> friends
`</pre> *
 *
 *
 *
 * @see [@EmptyCollectionIfNull](https://github.com/doanduyhai/Achilles/wiki/Achilles-Annotations.emptycollectionifnull)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD, ElementType.TYPE_USE)
@Documented
annotation class EmptyCollectionIfNull