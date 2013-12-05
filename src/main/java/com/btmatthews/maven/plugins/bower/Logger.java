/*
 *  Copyright 2013 Brian Matthews
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.btmatthews.maven.plugins.bower;

import org.apache.maven.plugin.logging.Log;

import java.util.ResourceBundle;

/**
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public class Logger {

    public static final String ERROR_CANNOT_DOWNLOAD_PACKAGES_FILE = "com.btmatthews.maven.plugin.bower.ERROR_CANNOT_DOWNLOAD_PACKAGES_FILE";
    public static final String ERROR_CANNOT_PARSE_PACKAGES_FILE = "com.btmatthews.maven.plugin.bower.ERROR_CANNOT_PARSE_PACKAGES_FILE";
    public static final String ERROR_CANNOT_DOWNLOAD_PACKAGE_CONTENTS = "com.btmatthews.maven.plugin.bower.ERROR_CANNOT_DOWNLOAD_PACKAGE_CONTENTS";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("com.btmatthews.maven.plugins.bower.messages");
    private final Log log;

    public Logger(final Log log) {
        this.log = log;
    }

    public String info(final String key) {
        final String message = MESSAGES.getString(key);
        log.info(message);
        return message;
    }

    public String info(final String key, final Throwable e) {
        final String message = MESSAGES.getString(key);
        log.info(message, e);
        return message;
    }

    public String debug(final String key) {
        final String message = MESSAGES.getString(key);
        log.debug(message);
        return message;
    }

    public String debug(final String key, final Throwable e) {
        final String message = MESSAGES.getString(key);
        log.debug(message, e);
        return message;
    }

    public String warn(final String key) {
        final String message = MESSAGES.getString(key);
        log.error(message);
        return message;
    }

    public String warn(final String key, final Throwable e) {
        final String message = MESSAGES.getString(key);
        log.error(message, e);
        return message;
    }

    public String error(final String key) {
        final String message = MESSAGES.getString(key);
        log.error(message);
        return message;
    }

    public String error(final String key, final Throwable e) {
        final String message = MESSAGES.getString(key);
        log.error(message, e);
        return message;
    }
}
