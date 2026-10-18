package com.example.novaplustrader

import com.example.novaplustrader.ui.screens.admin.models.AdminArea
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import com.example.novaplustrader.ui.screens.admin.models.canAccess
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminRolePermissionsTest {
    @Test
    fun administrativeAreasFollowCustomClaimRolePolicy() {
        for (role in listOf(AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.READ_ONLY)) {
            for (area in AdminArea.entries) assertTrue(role.canAccess(area))
        }
        assertTrue(AdminRole.SUPPORT.canAccess(AdminArea.SUPPORT))
        assertFalse(AdminRole.SUPPORT.canAccess(AdminArea.OPERATIONS))
        assertFalse(AdminRole.SUPPORT.canAccess(AdminArea.GENERAL))
        assertTrue(AdminRole.OPERATIONS.canAccess(AdminArea.OPERATIONS))
        assertFalse(AdminRole.OPERATIONS.canAccess(AdminArea.SUPPORT))
        assertFalse(AdminRole.OPERATIONS.canAccess(AdminArea.COMPLIANCE))
        assertTrue(AdminRole.COMPLIANCE.canAccess(AdminArea.COMPLIANCE))
        assertFalse(AdminRole.COMPLIANCE.canAccess(AdminArea.OPERATIONS))
        assertFalse(AdminRole.COMPLIANCE.canAccess(AdminArea.SUPPORT))
    }
}
