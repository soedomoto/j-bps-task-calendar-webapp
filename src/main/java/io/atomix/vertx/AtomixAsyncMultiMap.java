/*
 * Copyright 2015 the original author or authors.
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
 * limitations under the License
 */
package io.atomix.vertx;

import io.atomix.catalyst.util.Assert;
import io.atomix.collections.DistributedMultiMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

import java.util.function.Predicate;

/**
 * Atomix async multi map.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class AtomixAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
  private final Vertx vertx;
  private final DistributedMultiMap<K, V> map;

  public AtomixAsyncMultiMap(Vertx vertx, DistributedMultiMap<K, V> map) {
    this.vertx = Assert.notNull(vertx, "vertx");
    this.map = Assert.notNull(map, "map");
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
    map.put(k, v).whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    map.get(k).whenComplete(VertxFutures.convertHandler(handler, AtomixChoosableIterable::new, vertx.getOrCreateContext()));
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    map.remove(k, v).whenComplete(VertxFutures.resultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> handler) {
    map.removeValue(v).whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void removeAllMatching(Predicate<V> predicate, Handler<AsyncResult<Void>> handler) {

  }

}
