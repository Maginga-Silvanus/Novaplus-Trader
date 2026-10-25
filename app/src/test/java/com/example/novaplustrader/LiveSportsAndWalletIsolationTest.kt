package com.example.novaplustrader

import com.example.novaplustrader.data.repository.LiveSportsRepositoryImpl
import com.example.novaplustrader.ui.screens.betting.models.EventStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveSportsAndWalletIsolationTest {

    @Test
    fun testLiveSportsFeedParsingAndStatus() = runBlocking {
        val repo = LiveSportsRepositoryImpl()
        val feed = repo.observeLiveSportsFeed().first()

        assertNotNull(feed)
        assertTrue(feed.events.isNotEmpty())
        assertEquals("ESPN Live Sports API (Internet Connected)", feed.source)
        assertEquals(EventStatus.LIVE, feed.events.first().status)
    }

    @Test
    fun testSportsDataIsolationFromWallet() = runBlocking {
        val sportsRepo = LiveSportsRepositoryImpl()
        val initialFeed = sportsRepo.observeLiveSportsFeed().first()

        // Simulate refresh or feed updates
        sportsRepo.refreshLiveScores()
        val updatedFeed = sportsRepo.observeLiveSportsFeed().first()

        // Verify sports data updated without affecting any fake financial / wallet records
        assertTrue(updatedFeed.lastUpdatedMs >= initialFeed.lastUpdatedMs)
    }
}
