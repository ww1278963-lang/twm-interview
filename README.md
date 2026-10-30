# 智慧訂單與促銷引擎

Java 21、Spring Boot、JUnit 5。資料存在記憶體，沒有對外 API。

```
mvn test
```

## 架構

| 模組 | 位置 | 重點 |
| --- | --- | --- |
| 模型 | `order.model` | 金額全用 `BigDecimal`；建構時驗證資料；狀態只能 PENDING → PAID → SHIPPED |
| 折扣 | `order.promotion` | Strategy 模式。新增促銷只要加一個 `DiscountStrategy` 的 `@Component`，不用改既有類別 |
| 通知 | `order.notification` | 每個通道各開一條 virtual thread 平行發送；付款不等通知完成，單一通道失敗不影響其他通道 |
| 流程 | `order.service` | `OrderService` 串起以上模組，用 `ConcurrentHashMap` 儲存 |

## 題目沒定義、我自己決定的規則

| 問題 | 決定 |
| --- | --- |
| 三種折扣能否併用 | 全部併用，依序為滿額 → 節慶 → 會員。調整順序只要改 `@Order` |
| 滿額折的判斷基準 | 原始總額，剛好滿額也算（滿 $1,000 含 $1,000） |
| 滿額折兩級是否累加 | 不累加，只取最高一級（滿 $2,000 折 $250） |
| `discountedAmount` 的意思 | 折扣後應付金額，不是折掉的金額 |
| 四捨五入 | 全部算完才四捨五入到小數 2 位 |

範例：$2,000、GOLD、雙 11 → (2000 − 250) × 0.9 × 0.9 = **$1,417.50**

## Email：Mock 與實際寄送

題目一處要求實際寄送，另兩處要求用 Mock，所以兩者都實作，用設定切換。預設是 Mock，Slack 一律是 Mock。

```properties
app.notification.email.mode=smtp
app.notification.email.from=shop@example.com
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=...
spring.mail.password=...
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 上線前還缺的部分

通知沒有重試，失敗只記 log；程序在發送前當掉，通知就會遺失。正式環境應該用 Outbox pattern 或訊息佇列（如 Kafka），加上重試。
