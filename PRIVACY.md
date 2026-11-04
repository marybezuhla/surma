# Приватність · Privacy

## Які дані є

| Що | Де | Навіщо |
|---|---|---|
| Вибрані області, частота перевірок, вигляд, мова | приватне сховище Сурми на телефоні | щоб сповіщати лише про ваші області |
| Номер останнього прочитаного допису, час останньої перевірки | там само | щоб не сповіщати двічі |
| Загрози за останні 12 годин (не більше 150): тип, місце, рядок допису | там само (`feed.json`) | для головного екрана |

Це лише те, що канал і так опублікував, плюс ваш вибір областей. Сурма не знає вашого місця: геолокації вона не використовує. Акаунтів, аналітики, звітів про збої й реклами немає.

Дані не шифруються окремо: їх захищає шифрування самого телефона, а інші застосунки не мають до них доступу. Вибір областей може натякати, де ви живете, тому на заблокованому екрані сповіщення показує лише тип загрози.

## Що бачать інші

- **Telegram** бачить адресу телефона (IP) і час кожної перевірки: кожні 20 секунд — 3 хвилини, поки стеження ввімкнено. Сурма називає себе просто `Surma` і нічого не передає про вас чи телефон.
- **Google** та інші сервіси нічого від Сурми не отримують: інших зʼєднань немає.
- Резервні копії Android і перенесення на інший телефон вимкнено: дані Сурми не потрапляють у хмару.

## Як усе стерти

Вимкніть стеження й видаліть застосунок: Android прибирає всі його дані. Або Налаштування → Застосунки → Сурма → Пам'ять → Очистити дані.

---

## What data there is

| What | Where | Why |
|---|---|---|
| Chosen regions, how often to check, look, language | Surma's private storage on the phone | to alert only about your regions |
| The last post read, the time of the last check | the same | not to alert twice |
| Threats of the last 12 hours (at most 150): kind, place, the post's line | the same (`feed.json`) | for the home screen |

Only what the channel published anyway, plus your choice of regions. Surma does not know where you are: it uses no location. No accounts, analytics, crash reports or ads.

The data is not encrypted separately: the phone's own encryption protects it, and other apps cannot read it. The chosen regions may hint where you live, so on the lock screen an alert shows only the kind of threat.

## What others see

- **Telegram** sees the phone's address (IP) and the time of every check: every 20 seconds to 3 minutes while watching is on. Surma calls itself just `Surma` and sends nothing about you or the phone.
- **Google** and other services get nothing from Surma: there are no other connections.
- Android backups and phone-to-phone transfer are off: Surma's data never reaches a cloud.

## How to erase everything

Turn watching off and uninstall the app: Android removes all its data. Or Settings → Apps → Surma → Storage → Clear data.
