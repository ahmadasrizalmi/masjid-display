package com.asridigital.masjiddisplay.admin

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue

class AdminLauncherContractTest {
    @Test
    fun launcherActivityClassExists() {
        assertTrue(MainActivity::class.java.name == "com.asridigital.masjiddisplay.admin.MainActivity")
    }
}
