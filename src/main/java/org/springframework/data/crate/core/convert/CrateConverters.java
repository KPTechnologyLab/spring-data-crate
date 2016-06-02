/*
 * Copyright 2002-2014 the original author or authozrs.
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
package org.springframework.data.crate.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.Date;
import java.util.Locale;

/**
 * Wrapper class to contain useful converters for the usage with Crate.
 *
 * @author Hasnain Javed
 * @since 1.0.0
 */
public abstract class CrateConverters {

    /**
     * Private constructor to prevent instantiation.
     */
    private CrateConverters() {
    }

    @WritingConverter
    public static enum LocaleToStringConverter implements Converter<Locale, String> {
        INSTANCE;

        @Override
        public String convert(Locale locale) {
            return locale == null ? null : locale.toString();
        }
    }

    @WritingConverter
    public static enum DateToLongConverter implements Converter<Date, Long> {
        INSTANCE;

        @Override
        public Long convert(Date source) {
            return source == null ? 0L : source.getTime();
        }
    }

    @ReadingConverter
    public static enum LongToDateConverter implements Converter<Long, Date> {
        INSTANCE;

        @Override
        public Date convert(Long source) {
            return source == null ? null : new Date(source);
        }
    }
}
