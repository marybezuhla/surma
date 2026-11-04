# Безпека · Security

## Як повідомити про вразливість

Через **приватний звіт** GitHub: вкладка *Security* → *Report a vulnerability*. Будь ласка, не пишіть про вразливості в публічних issues.

- Підтвердження отримання — **протягом 3 днів**.
- Критичну вразливість (наприклад, фальшиві чи пропущені сповіщення через підроблену сторінку, витік даних) виправляємо **протягом 7 днів**, решту — в найближчому випуску.

## Що підтримується

Лише **остання версія**. Оновлення безпеки виходять тільки для неї.

## Мінімальна версія Android

**Android 12 (API 31).** Саме з нього Android дає те, на що спирається Сурма: суворі правила запуску фонових служб і точних будильників, сучасні сповіщення та налаштування мережі, що довіряють лише вбудованим сертифікатам.

## Що Сурма робить для безпеки

- Зʼєднується лише з `t.me` і лише через HTTPS; перенаправлення на інші адреси відхиляються; сертифікати, додані користувачем, не довіряються.
- Сторінка читається з обмеженням розміру (2 МБ), довжини допису й кількості загроз; незрозуміла сторінка ніколи не зупиняє стеження й не ламає застосунок.
- Жоден компонент не відкритий іншим застосункам, окрім головного екрана й приймача перезавантаження (його надсилає лише Android).
- Без резервних копій Android, без журналів у випусковій збірці.

---

## Reporting a vulnerability

Use GitHub's **private report**: the *Security* tab → *Report a vulnerability*. Please do not post vulnerabilities in public issues.

- We confirm receipt **within 3 days**.
- A critical vulnerability (for example, fake or missed alerts through a forged page, or a data leak) is fixed **within 7 days**; others in the next release.

## Supported versions

Only the **latest version** gets security updates.

## Minimum Android version

**Android 12 (API 31)**: from there Android has what Surma relies on — strict rules for background services and exact alarms, current notifications, and network settings that trust only the built-in certificates.
