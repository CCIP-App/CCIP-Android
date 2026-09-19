package app.opass.ccip.push

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PushContractTest {
    @Test fun localesIdentitiesAndClickRouting() {
        mapOf("zh-Hans-CN" to "zh-Hant", "zh-Hant-TW" to "zh-Hant", "zh-CN" to "zh-Hant",
            "zh" to "zh-Hant", "ZH-HANS-CN" to "zh-Hant", "nan-Hant-TW" to "zh-Hant",
            "nan-Latn-TW-pehoeji" to "zh-Hant", "nan-Latn-TW-tailo" to "zh-Hant",
            "en-US" to "en", "ja-JP" to "en", "ko-KR" to "en").forEach { (tag, locale) ->
            assertEquals(locale, PushContract.resolvePushLocale(tag, "en-US"))
        }
        assertEquals("zh-Hant", PushContract.resolvePushLocale("x-default", "zh-Hant-TW"))
        assertEquals("zh-Hant", PushContract.resolvePushLocale("x-default", "zh-Hans-CN"))
        assertEquals("en", PushContract.resolvePushLocale("x-default", "ja-JP"))
        for (locale in listOf("zh-Hans", "ja", "", "../en")) {
            assertThrows(IllegalArgumentException::class.java) { PushContract.topic("A", "staff", locale) }
        }
        assertEquals("opass-v1.A.staff.en", PushContract.topic("A", "staff", "en"))
        for (role in listOf("all", "", "a.b", "a".repeat(65))) assertFalse(PushContract.hasValidTopicIdentifiers("A", role))
        val data = mapOf("push_id" to "00000000-0000-4000-8000-000000000001", "event_id" to "B")
        assertEquals("B", PushContract.route(data)?.eventId)
        assertNull(PushContract.route(data - "push_id"))
        assertNull(PushContract.route(data + ("uri" to "http://example.org")))
        assertNull(PushContract.route(data + ("uri" to "https:///missing-host")))
        assertEquals("https://example.org", PushContract.route(data + ("uri" to "https://example.org"))?.uri)
        assertEquals("https://例子.台灣/會場", PushContract.route(data + ("uri" to "https://例子.台灣/會場"))?.uri)
        assertTrue(PushContract.invalidCredentials(400, "invalid token"))
        for (status in listOf(401, 403, 404, 429, 500, 503)) assertFalse(PushContract.invalidCredentials(status, "invalid token"))
        assertFalse(PushContract.invalidCredentials(400, "unrelated error"))
    }

    @Test fun lateSuccessAndInvalidationCannotOverwriteNewLoginOrAnotherEvent() {
        val old = IdentitySnapshot("A", "old-token", "r1")
        val newer = old.copy(token = "new-token", revision = "r2")
        val identities = mutableMapOf("A" to newer, "B" to IdentitySnapshot("B", "b-token", "r1"))
        for (stale in listOf(old, old.copy(token = "new-token"), newer.copy(revision = "older"))) {
            assertFalse(stale.applyIfCurrent(identities.getValue("A")) { fail("Stale success/invalidation must not run") })
        }
        val b = identities.getValue("B")
        assertTrue(newer.applyIfCurrent(identities.getValue(newer.eventId)) { identities["A"] = newer.copy(revision = "r3") })
        assertEquals(b, identities["B"])
        assertNotEquals(old.fingerprint(), newer.fingerprint())
        assertNotEquals(old.fingerprint(), old.copy(revision = "re-login-same-token").fingerprint())
    }

    @Test fun multipleEventsRoleLocaleAndLogoutKeepOtherSubscriptions() = runBlocking {
        val states = mutableMapOf("A" to TopicState("A.old.en"), "B" to TopicState("B.staff.en"))
        val active = mutableSetOf("A.old.en", "B.staff.en")
        val calls = mutableListOf<String>()
        suspend fun sync(event: String, desired: String?) {
            reconcileTopic({ desired }, { states.getValue(event) }, { states[event] = it },
                { calls += "-$it"; active.remove(it) }, { calls += "+$it"; active.add(it) })
        }
        sync("A", "A.new.zh-Hant")
        assertEquals(listOf("-A.old.en", "+A.new.zh-Hant"), calls)
        sync("A", "A.new.zh-Hant")
        assertEquals(2, calls.size)
        assertEquals(setOf("A.new.zh-Hant", "B.staff.en"), active)
        sync("A", null)
        assertEquals(setOf("B.staff.en"), active)
    }

    @Test fun completedSubscriptionBeforeStateWriteCleansPendingTopics() = runBlocking {
        var persisted = TopicState("old")
        val active = mutableSetOf("old")
        try {
            reconcileTopic({ "new" }, { persisted }, { next ->
                if (next.applied == "new" && next.pending.isEmpty()) error("Process interrupted before commit")
                persisted = next
            }, { active.remove(it) }, {
                assertEquals(setOf("old", "new"), persisted.pending)
                active.add(it)
            })
            fail("Must simulate crash")
        } catch (_: IllegalStateException) { }
        assertEquals(setOf("new"), active)
        reconcileTopic({ "third" }, { persisted }, { persisted = it }, { active.remove(it) }, { active.add(it) })
        assertEquals(setOf("third"), active)
        assertEquals(TopicState("third"), persisted)
    }

    @Test fun failedUnsubscribeNeverAddsReplacementAndTargetChangesConverge() = runBlocking {
        var persisted = TopicState("old")
        var desired = "new"
        val active = mutableSetOf("old")
        try {
            reconcileTopic({ desired }, { persisted }, { persisted = it }, { error("Offline") }, { fail("Must not subscribe") })
            fail("Must report failure")
        } catch (_: IllegalStateException) { }
        assertEquals(setOf("old", "new"), persisted.pending)
        reconcileTopic({ desired }, { persisted }, { persisted = it }, { active.remove(it) }, {
            active.add(it)
            if (it == "new") desired = "newer"
        })
        assertEquals(setOf("newer"), active)
    }

    @Test fun missingTargetDoesNotSubscribeAndPendingTopicIsReapplied() = runBlocking {
        var state = TopicState()
        var target: String? = null
        val active = mutableSetOf<String>()
        reconcileTopic({ target }, { state }, { state = it }, { active.remove(it) }, { active.add(it) })
        assertTrue(active.isEmpty())
        target = "A.verified.en"
        reconcileTopic({ target }, { state }, { state = it }, { active.remove(it) }, { active.add(it) })
        assertEquals(setOf(target), active)
        // FID replacement must reapply even a previously confirmed topic.
        state = state.copy(pending = setOf(target))
        var subscriptions = 0
        reconcileTopic({ target }, { state }, { state = it }, { active.remove(it) }, { subscriptions++; active.add(it) })
        assertEquals(1, subscriptions)
    }
}
