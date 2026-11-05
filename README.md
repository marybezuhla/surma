# Сурма · Surma

Приватний застосунок повітряної тривоги для Android. Стежить за публічним каналом Повітряних сил ЗСУ й одразу сповіщає про загрози у ваших областях. Українською та англійською. Безкоштовно, без реклами, акаунтів, стеження й власного сервера.

> ⚠️ **Сурма — додаткове джерело.** Вона читає дописи автоматично й може помилятися. Сурма не замінює сирену, офіційні сповіщення та вказівки місцевої влади.
>
> Це **не офіційний застосунок**: його не робили Повітряні сили чи держава.

## Можливості

- **Сповіщення про загрози** у вибраних областях: ударні й реактивні БпЛА, ракети, балістика, швидкісні цілі, КАБи, активність авіації, відбій. За бажанням — загрози для всієї країни без названої області.
- **Що й куди:** тип загрози, кількість, область, напрямок руху («курсом на…») і звідки. Сурма знає всі ≈29,7 тис. міст, селищ і сіл та 136 районів з Кодифікатора, у будь-якому відмінку («на Іванівку», «в районі Білої Церкви»); однакові назви розрізняє за областю з допису, а якщо не може — показує назву текстом. Під кожною загрозою — сам рядок допису, бо автоматичне розпізнавання може помилитися.
- **Ощадливо:** під час загроз перевіряє частіше (кожні 20–60 с), у тиші — рідше (45 с — 3 хв). Між перевірками телефон спить.
- **На заблокованому екрані** — лише тип загрози («Сурма: балістика»), без області.
- **Вигляд:** кольори телефона або світлі й темні теми з рідними назвами. Для телефонів, планшетів і розкладних.

Карта загроз — у наступній версії.

## Приватність

Сурма читає публічну вебсторінку каналу [t.me/s/kpszsu](https://t.me/s/kpszsu), тож **Telegram бачить адресу телефона** щоразу, коли вона перевіряє. Telegram-акаунт не потрібен. Більше Сурма нікуди не зʼєднується й нічого не надсилає. Вона не знає, де ви: області ви обираєте самі, і вони лишаються на телефоні. Докладно — у [PRIVACY.md](PRIVACY.md).

## Вимоги

**Android 12** або новіший. Щоб сповіщення приходили вчасно, коли екран вимкнено, Сурма попросить дозволити «Будильники й нагадування» та роботу без обмежень заряду. Скільки заряду й мобільного інтернету вона витрачає на справжньому телефоні, ще вимірюємо.

## Як працює

1. Поки стеження ввімкнено, працює служба з тихим постійним сповіщенням (цього вимагає Android).
2. Будильник Android будить її на кілька секунд: вона читає нові дописи каналу, розпізнає загрози й сповіщає про ті, що стосуються ваших областей.
3. Наступна перевірка — за 20 с — 3 хв, залежно від того, чи тривають загрози, і від вибраної частоти.

Розпізнавання — звичайні правила для українського тексту (без ШІ), з тестами на реальних дописах каналу.

---

# Surma

A private air-raid alert app for Android. It watches the public channel of the Ukrainian Air Force and alerts you at once about threats in your regions. Ukrainian and English. Free, no ads, no accounts, no tracking, no server of its own.

> ⚠️ **Surma is a supplementary source.** It reads posts automatically and can be wrong. It does not replace sirens, official alerts or instructions from local authorities.
>
> It is **not an official app**: it is not made by the Air Force or the state.

## Features

- **Alerts for threats** in the regions you choose: attack and jet drones, missiles, ballistic missiles, high-speed targets, guided bombs, aircraft activity, all clear. Optionally, nationwide threats that name no region.
- **What and where:** kind, number, region, heading ("towards…") and where from. Surma knows all ≈29.7 thousand cities, towns and villages and 136 districts of the official register, in any grammatical case; places that share a name are told apart by the region in the post, and if that is not possible the name is shown as text. Each threat shows the post's own line, because automatic reading can be wrong.
- **Easy on the battery:** checks more often while threats come (every 20–60 s), less often when quiet (45 s – 3 min). The phone sleeps between checks.
- **On the lock screen** only the kind of threat shows ("Surma: ballistic missiles"), never the region.
- **Look:** the phone's own colours, or light and dark themes. For phones, tablets and foldables.

A threat map comes in the next version.

## Privacy

Surma reads the channel's public web page [t.me/s/kpszsu](https://t.me/s/kpszsu), so **Telegram sees the phone's address** every time it checks. No Telegram account is needed. Surma connects nowhere else and sends nothing. It does not know where you are: you choose the regions, and they stay on the phone. Details: [PRIVACY.md](PRIVACY.md).

## Requirements

**Android 12** or newer. For alerts to come on time while the screen is off, Surma asks to allow "Alarms and reminders" and to run without battery limits. How much battery and mobile data it uses on a real phone is still being measured.

---

## Перевірка APK · Verify the APK

SHA-256 сертифіката підпису · Signing certificate SHA-256:

```
32108e17719507cfa7fd0a7865b877aae0960f3329053f765ea8521a4b028843
```

```
apksigner verify --print-certs surma-<версія>.apk
```

## ШІ · AI

Код написано за допомогою ШІ (Claude, Anthropic) під керівництвом і з тестуванням автора.

The code was written with AI (Claude, Anthropic), directed and tested by the author.

## Ліцензія · License

**GPL-3.0-or-later.** Сурма — вільне програмне забезпечення: її можна поширювати та змінювати за умовами GNU General Public License, опублікованої Free Software Foundation, версії 3 або (на ваш вибір) будь-якої новішої. Вона поширюється без жодних гарантій. Повний текст: [LICENSE](LICENSE). Бібліотеки й дані (зокрема точки місць © учасники OpenStreetMap, ODbL): [NOTICE.md](NOTICE.md).

Surma is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. It comes without any warranty. Full text: [LICENSE](LICENSE). Libraries and data (including points of places © OpenStreetMap contributors, ODbL): [NOTICE.md](NOTICE.md).

SPDX-License-Identifier: GPL-3.0-or-later
