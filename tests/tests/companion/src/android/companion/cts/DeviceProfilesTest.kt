package android.companion.cts

import android.Manifest
import android.app.role.RoleManager.ROLE_ASSISTANT
import android.app.role.RoleManager.ROLE_BROWSER
import android.app.role.RoleManager.ROLE_CALL_REDIRECTION
import android.app.role.RoleManager.ROLE_CALL_SCREENING
import android.app.role.RoleManager.ROLE_DIALER
import android.app.role.RoleManager.ROLE_EMERGENCY
import android.app.role.RoleManager.ROLE_HOME
import android.app.role.RoleManager.ROLE_SMS
import android.app.role.RoleManager.ROLE_SYSTEM_SUPERVISION
import android.app.role.RoleManager.ROLE_SYSTEM_WELLBEING
import android.companion.AssociationRequest
import android.companion.AssociationRequest.DEVICE_PROFILE_APP_STREAMING
import android.companion.AssociationRequest.DEVICE_PROFILE_AUTOMOTIVE_PROJECTION
import android.companion.AssociationRequest.DEVICE_PROFILE_WATCH
import android.companion.cts.RecordingCallback.CallbackMethod.OnAssociationPending
import android.platform.test.annotations.AppModeFull
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Test CDM device profiles.
 *
 * Run: atest CtsCompanionDevicesTestCases:DeviceProfilesTest
 *
 * @see android.companion.AssociationRequest.DEVICE_PROFILE_APP_STREAMING
 * @see android.companion.AssociationRequest.DEVICE_PROFILE_AUTOMOTIVE_PROJECTION
 * @see android.companion.AssociationRequest.DEVICE_PROFILE_WATCH
 * @see android.companion.CompanionDeviceManager.associate
 */
@AppModeFull(reason = "CompanionDeviceManager APIs are not available to the instant apps.")
@RunWith(AndroidJUnit4::class)
class DeviceProfilesTest : TestBase() {
    /** Test that all supported device profiles require a permission. */
    @Test
    fun test_supportedProfiles() {
        val callback = RecordingCallback()
        SUPPORTED_PROFILES.forEach { (profile, permission) ->
            callback.clearRecordedInvocation()
            val request = buildRequest(deviceProfile = profile)

            // Should fail if called without the required permissions.
            assertFailsWith(SecurityException::class) {
                cdm.associate(request, SIMPLE_EXECUTOR, callback)
            }
            // Make sure callback wasn't invoked.
            assertEmpty(callback.invocations)

            // Should succeed when called with the required permission.
            withShellPermissionIdentity(permission) {
                cdm.associate(request, SIMPLE_EXECUTOR, callback)
            }
            // Wait for callback.
            callback.waitForInvocation()
            // Make sure it's the right callback.
            assertEquals(actual = callback.invocations.size, expected = 1)
            callback.invocations[0].apply {
                assertEquals(actual = method, expected = OnAssociationPending)
                assertNotNull(intentSender)
            }
        }
    }

    /** Test that CDM rejects "arbitrary" profiles. */
    @Test
    fun test_unsupportedProfiles() = withShellPermissionIdentity {
        val callback = RecordingCallback()
        UNSUPPORTED_PROFILES.forEach { profile ->
            val request = buildRequest(deviceProfile = profile)
            // Should fail if called without the required permissions.
            assertFailsWith(IllegalArgumentException::class) {
                cdm.associate(request, SIMPLE_EXECUTOR, callback)
            }
            // Make sure callback wasn't invoked.
            assertEmpty(callback.invocations)
        }
    }

    /** Test that the null profile is supported and does not require a permission. */
    @Test
    fun test_nullProfile() {
        val callback = RecordingCallback()
        val request = buildRequest(deviceProfile = null)

        // Should not require a permission.
        cdm.associate(request, SIMPLE_EXECUTOR, callback)
        // Wait for callback.
        callback.waitForInvocation()
        // Make sure it's the right callback.
        assertEquals(actual = callback.invocations.size, expected = 1)
        callback.invocations[0].apply {
            assertEquals(actual = method, expected = OnAssociationPending)
            assertNotNull(intentSender)
        }
    }

    private fun buildRequest(deviceProfile: String?) = AssociationRequest.Builder()
            .apply { deviceProfile?.let { setDeviceProfile(it) } }
            .build()

    companion object {
        val SUPPORTED_PROFILES = mapOf(
                DEVICE_PROFILE_WATCH to Manifest.permission.REQUEST_COMPANION_PROFILE_WATCH,
                DEVICE_PROFILE_APP_STREAMING to
                        Manifest.permission.REQUEST_COMPANION_PROFILE_APP_STREAMING,
                DEVICE_PROFILE_AUTOMOTIVE_PROJECTION to
                        Manifest.permission.REQUEST_COMPANION_PROFILE_AUTOMOTIVE_PROJECTION
        )

        val UNSUPPORTED_PROFILES = setOf(
                // Each supported device profile is backed by a role. However, other roles should
                // not be treated as device profiles.
                ROLE_ASSISTANT,
                ROLE_BROWSER,
                ROLE_DIALER,
                ROLE_SMS,
                ROLE_EMERGENCY,
                ROLE_HOME,
                ROLE_CALL_REDIRECTION,
                ROLE_CALL_SCREENING,
                ROLE_SYSTEM_WELLBEING,
                ROLE_SYSTEM_SUPERVISION,
                // Other arbitrarily Strings should not be supported either.
                "watch",
                "auto",
                "companion_device",
                ""
        )
    }
}