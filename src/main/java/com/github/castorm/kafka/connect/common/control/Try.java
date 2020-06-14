package com.github.castorm.kafka.connect.common.control;

/*-
 * #%L
 * Kafka Connect HTTP
 * %%
 * Copyright (C) 2020 CastorM
 * %%
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
 * #L%
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@NoArgsConstructor
public abstract class Try<T> {

    public static <T> Try<T> of(Callable<T> callable) {
        try {
            return new Success<>(callable.call());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static <T> BinaryOperator<Try<T>> zip(BinaryOperator<T> reducer) {
        return (a, b) -> Try.zip(a, b, reducer);
    }

    private static <T> Try<T> zip(Try<T> left, Try<T> right, BinaryOperator<T> reducer) {
        if (!left.isFailure() && !right.isFailure()) {
            return new Success<>(reducer.apply(left.getValue(), right.getValue()));
        } else if (left.isFailure()) {
            return new Failure<>(left.getCause());
        } else {
            return new Failure<>(right.getCause());
        }
    }

    public T getOrFail() {
        return getOrFail(RuntimeException::new);
    }

    private <X extends RuntimeException> T getOrFail(Function<Exception, X> exceptionProvider) throws X {
        if (isFailure()) {
            throw exceptionProvider.apply(getCause());
        }
        return getValue();
    }

    protected abstract boolean isFailure();

    protected abstract T getValue();

    protected abstract Exception getCause();

    @RequiredArgsConstructor
    static class Success<T> extends Try<T> {

        @Getter
        private final T value;

        @Override
        protected boolean isFailure() {
            return false;
        }

        @Override
        protected Exception getCause() {
            throw new UnsupportedOperationException();
        }
    }

    @RequiredArgsConstructor
    static class Failure<T> extends Try<T> {

        @Getter
        private final Exception cause;

        @Override
        protected boolean isFailure() {
            return true;
        }

        @Override
        protected T getValue() {
            throw new UnsupportedOperationException();
        }
    }
}
