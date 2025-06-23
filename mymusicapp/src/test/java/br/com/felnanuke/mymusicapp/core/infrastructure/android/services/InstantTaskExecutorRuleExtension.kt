package br.com.felnanuke.mymusicapp.core.infrastructure.android.services

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension for InstantTaskExecutorRule.
 * This extension allows LiveData to work synchronously in JUnit 5 tests.
 */
class InstantTaskExecutorRuleExtension : InstantTaskExecutorRule(), BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        starting(null)
    }

    override fun afterEach(context: ExtensionContext) {
        finished(null)
    }
}
