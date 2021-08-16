/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.bedstead.harrier.annotations;

import static com.android.bedstead.harrier.OptionalBoolean.TRUE;

import com.android.bedstead.harrier.DeviceState;
import com.android.bedstead.harrier.OptionalBoolean;
import com.android.bedstead.harrier.annotations.meta.RequireRunOnUserAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark that a test method should run on the system user.
 *
 * <p>Your test configuration should be such that this test is only run on the system user
 *
 * <p>Optionally, you can guarantee that these methods do not run outside of the system
 * user by using {@link DeviceState}.
 *
 * <p>Note that this requires that the test runs on the system user, including headless system
 * users. To mark that a test should run on the primary user, excluding headless
 * system users, see {@link RequireRunOnPrimaryUser}.
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequireRunOnUserAnnotation(
        {"android.os.usertype.full.SYSTEM", "android.os.usertype.system.HEADLESS"})
public @interface RequireRunOnSystemUser {
    /**
     * Should we ensure that we are switched to the given user
     */
    OptionalBoolean switchedToUser() default TRUE;
}