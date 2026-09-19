# 公開活動推播

公開推播依 [Gateway ADR 0001](https://github.com/CCIP-App/CCIP-Push-Gateway/blob/main/docs/adr/0001-fcm-topic-push-gateway.md)，使用 Firebase Messaging `25.1.3` 的 FID registration；同專案保留既有 Analytics `23.2.0`。版本與 API 依 [Firebase release notes](https://firebase.google.com/support/release-notes/android) 及 [Android FCM 指引](https://firebase.google.com/docs/cloud-messaging/android/get-started) 核對。App 使用 `opass-8b7db` 的 `app.opass.ccip` client，不包含 Gateway key 或 service-account JSON。

## 訂閱與恢復

`PushTopicManager` 集中處理所有已登入活動。每活動只有一個 `opass-v1.<event>.<role>.<locale>`；切換畫面活動不退訂其他活動。登入成功、身分失效、角色變動、語系變更、FID 更新、啟動及回到主畫面會觸發同步。通知權限不決定訂閱資格。

英文 `en` 與正體中文 `zh-Hant` 是必需的推播內容，目前啟用這兩個推播語系。App 介面保留既有翻譯與語言選項，與推播語系分開處理：`zh` 及其延伸標籤、`nan-Hant-*`、`nan-Latn-*` 對應 `zh-Hant`，其他介面語言（包含日文與韓文）對應 `en`。`x-default` 先解析系統語系；正體與簡體介面共用正體中文推播內容。

既有／還原的 token 須先向該活動的 `/status` 驗證，且回傳活動、token 與角色合法，才能建立本安裝的已驗證狀態。活動設定來自既有快取或 Portal，使用設定時核對活動 ID。每個請求保存活動、token 與身分 revision，更新及清除時在同一鎖內比對；晚到的舊回應不修改新登入或另一活動。

CCIP-Server 的 `400` 且 JSON `message` 精確為 `invalid token` 才確認憑證失效。`403` 是活動 Wi-Fi 限制；離線、其他 4xx、5xx、格式損壞及本機讀取失敗不清除登入資料。若其他 Server 使用不同錯誤契約，先核對該契約，不能擴大成所有錯誤皆登出。

本安裝已驗證身分摘要、角色、已套用 topic、待清理 topic 及 registration 摘要使用 `noBackupFilesDir/push-topics.json` 的原子檔案，不參與雲端備份或裝置轉移。`identity` 保存登入身分快照的 SHA-256 摘要；`registration` 保存 FCM registration 回呼提供的 FID 的 SHA-256 摘要。登入憑證維持既有依活動儲存方式。還原裝置不會帶入「已訂閱」標記；新身分必須重新驗證。

SDK 操作序列化，先持久化可能殘留的舊／新 topic，退訂後才訂閱，最後寫入確認結果。如果 SDK 成功後、本機寫入前中斷，下次啟動先退訂所有候選，再套用目前已驗證的目標。網路錯誤保留待處理狀態，使用 SDK 重試及後續生命週期同步，沒有自製派送佇列或中央裝置清單。

## 通知與點擊

公告使用 `announcements` channel、預設 importance、提示音與振動，與 `session_bookmark` 提醒分開。App 宣告 `VIBRATE` 權限；Android 8+ 在建立頻道時啟用振動，保留既有頻道的使用者設定，不刪除或更換頻道來覆蓋選擇。Android 7 的前景通知使用預設聲音與振動，背景通知則由 Gateway 的 FCM payload 指定。實際提示仍遵循系統靜音、勿擾與通知設定。

背景／未執行使用 FCM notification 系統顯示，前景由 `PushMessagingService` 顯示同樣的通知。前景 PendingIntent 與背景 data 都交給 `LauncherActivity` 的共同解析路徑；需合法 `push_id` 與 `event_id`。HTTPS URI 開啟外部連結，無 URI 則切換至對應活動的公告頁。活動 ID 不提供登入權限；找不到有效活動或公告入口時顯示失敗，不擅自導向另一活動。

Android 13+ 登入提示使用既有 Activity Result 權限流程。拒絕權限不影響訂閱；本機不撤除一小時前已顯示的通知。Firebase background 顯示與前景自建通知都不得視為已驗證的統計來源。

## 建置與驗收

Firebase 專案須啟用 `fcmregistrations.googleapis.com`（[FCM Registration API](https://console.cloud.google.com/apis/library/fcmregistrations.googleapis.com?project=opass-8b7db)），才能完成這個 FID registration 流程。它與 Gateway 發送用的 `fcm.googleapis.com` 分開；App 不需要 service-account JSON。Firebase installations 建立成功不等於 FCM registration 或 topic 訂閱成功。若 API 未啟用，保留登入及 pending 狀態，設定生效後重新啟動 App 驗證，不以重裝或清除資料處理。

```sh
ANDROID_HOME=/path/to/Android/Sdk ./gradlew testDebugUnitTest lintDebug assembleDebug
```

APK 位於 `app/build/outputs/apk/debug/app-debug.apk`。沿用 repo 的 Gradle wrapper、SDK 37.1 與 version catalog；單元測試使用最小 JUnit 4 設定，驗證純 Kotlin 契約與狀態機，包括語系／topic／通知導頁資料、舊身分回應、多活動、退訂失敗、中途換目標，以及模擬 SDK 成功但未落盤、新安裝與 registration 重建。這些測試不執行 `PushTopicManager`、`PreferenceUtil`、Android 備份或真實 Firebase SDK；持久化、生命週期、訂閱及通知仍須另外整合驗收。

原地升級使用相同 application ID 與簽章，並核對版本及 Firebase client；既有登入資料須保留，升級後驗收所有已登入活動的重新驗證與訂閱。解除安裝後重裝屬於新安裝案例，不能取代升級驗收。

實機矩陣至少包含前景、背景、未執行（與 force-stop 分開）、允許／拒絕通知、非目前活動點擊、有／無 HTTPS URI、角色及介面語言切換、英文／正體推播對應、簡體介面及系統語系、啟動恢復、離線、憑證失效、晚到回應與中斷恢復。另核對新頻道的預設聲音與振動、Android 7 的前景／背景提示，以及升級後保留既有頻道設定；振動關閉或勿擾時不得強制振動。備份還原／FID 更新需可控的測試安裝，不以解除安裝既有 App 取代升級驗收。Gateway accepted、裝置通知出現、聲音／振動與點擊結果各自保存證據。

本整合的範圍是基本通知、訂閱與導頁，沒有新增 delivery export 或自製開啟事件。統計功能須另行定義蒐集預設、詢問及撤回流程，並驗證 Firebase／BigQuery 成效與 CSV 交付；不從既有 Analytics／SDK 預設推定同意，也不以基本通知測試代替統計驗收。
