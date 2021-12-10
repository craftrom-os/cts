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

package com.android.bedstead.nene.device;

import android.os.RemoteException;
import android.support.test.uiautomator.UiDevice;

import androidx.test.platform.app.InstrumentationRegistry;

import com.android.bedstead.nene.exceptions.AdbException;
import com.android.bedstead.nene.exceptions.NeneException;
import com.android.bedstead.nene.utils.Poll;
import com.android.bedstead.nene.utils.ShellCommand;

/** Helper methods related to the device. */
public final class Device {
    public static final Device sInstance = new Device();
    private static final UiDevice sDevice =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    private Device() {

    }

    /**
     * Turn the screen on.
     */
    public void wakeUp() {
        try {
            ShellCommand.builder("input keyevent")
                    .addOperand("KEYCODE_WAKEUP")
                    .validate(String::isEmpty)
                    .execute();
        } catch (AdbException e) {
            throw new NeneException("Error waking up device", e);
        }

        Poll.forValue("isScreenOn", this::isScreenOn)
                .toBeEqualTo(true)
                .errorOnFail()
                .await();
    }

    /**
     * True if the screen is on.
     */
    public boolean isScreenOn() {
        try {
            return sDevice.isScreenOn();
        } catch (RemoteException e) {
            throw new NeneException("Error getting isScreenOn", e);
        }
    }
}