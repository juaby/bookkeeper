/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
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

package org.apache.distributedlog.api.kv;

import static io.netty.util.ReferenceCountUtil.retain;

import java.util.concurrent.CompletableFuture;
import org.apache.bookkeeper.common.annotation.InterfaceAudience;
import org.apache.bookkeeper.common.annotation.InterfaceStability;
import org.apache.distributedlog.api.kv.options.RangeOption;
import org.apache.distributedlog.api.kv.result.KeyValue;
import org.apache.distributedlog.api.kv.result.RangeResult;

/**
 * A review view of a key/value space.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public interface TableReadView<K, V> extends PTableBase<K, V> {

    CompletableFuture<RangeResult<K, V>> get(K key, RangeOption<K> option);

    default CompletableFuture<V> get(K key) {
        RangeOption<K> option = opFactory().optionFactory().newRangeOption().build();
        return get(key, option)
            .thenApply(result -> {
                try {
                    if (result.count() == 0) {
                        return null;
                    } else {
                        return retain(result.kvs().get(0).value());
                    }
                } finally {
                    result.close();
                }
            })
            .whenComplete((value, cause) -> option.close());

    }

    default CompletableFuture<KeyValue<K, V>> getKv(K key) {
        RangeOption<K> option = opFactory().optionFactory().newRangeOption()
            .limit(1)
            .endKey(null)
            .build();
        return get(key, option)
            .thenApply(result -> {
                try {
                    if (result.count() == 0) {
                        return null;
                    } else {
                        return result.getKvsAndClear().get(0);
                    }
                } finally {
                    result.close();
                }
            })
            .whenComplete((value, cause) -> option.close());
    }

}
