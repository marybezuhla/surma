# Бібліотеки й дані · Libraries and data

Сурма — GPL-3.0-or-later. Вона використовує такі бібліотеки (усі сумісні з GPL-3.0-or-later):

Surma is GPL-3.0-or-later. It uses these libraries (all compatible with GPL-3.0-or-later):

| Бібліотека · Library | Автори · Authors | Ліцензія · License |
|---|---|---|
| AndroidX: Compose (UI, Material 3), Activity, Core, Lifecycle | The Android Open Source Project | Apache-2.0 |
| Kotlin standard library, kotlinx.coroutines | JetBrains s.r.o. and contributors | Apache-2.0 |

Лише для тестів (не потрапляють у застосунок) · Tests only (not in the app): JUnit (EPL-1.0), Robolectric (MIT), AndroidX Test (Apache-2.0), JSON-java (Public Domain).

## Дані · Data

- **Дописи:** публічний канал Повітряних сил ЗСУ, [t.me/kpszsu](https://t.me/kpszsu). Сурма лише читає те, що там опубліковано; вона не повʼязана з Повітряними силами. · **Posts:** the public channel of the Ukrainian Air Force. Surma only reads what is published there; it is not affiliated with the Air Force.
- **Місця:** Кодифікатор адміністративно-територіальних одиниць та територій територіальних громад (КАТОТТГ), відкриті дані [Мінрозвитку](https://mindev.gov.ua/diialnist/rozvytok-mistsevoho-samovriaduvannia/kodyfikator-administratyvno-terytorialnykh-odynyts-ta-terytorii-terytorialnykh-hromad). · **Places:** Ukraine's official register of administrative units (KATOTTG), open data of the Ministry for Development of Communities and Territories.
- **Точки місць** (`app/src/main/assets/places.tsv`): © учасники [OpenStreetMap](https://www.openstreetmap.org/copyright), ліцензія [Open Database License (ODbL) 1.0](https://opendatacommons.org/licenses/odbl/1-0/). Узято через [ua-geo](https://github.com/alexbabintsev/ua-geo) (код — MIT, © ua-geo contributors), версію `d37d5e9`; зібрано скриптом `scripts/places/build_places.py`. Файл `places.tsv` — похідна база даних і поширюється за тією самою ODbL 1.0. · **Points of places:** © OpenStreetMap contributors, ODbL 1.0, through ua-geo (code MIT), version `d37d5e9`, built by `scripts/places/build_places.py`. `places.tsv` is a derived database and is shared under the same ODbL 1.0.
- **Картки «Що робити»** (`app/src/main/assets/help/`): дослівно зі [«Спротиву»](https://github.com/marybezuhla/sprotyv) (версія `5c4ef1e`, скрипт `scripts/help/copy_cards.py`). Це адаптація відкритого [«Довідника виживання» 2.3](https://github.com/snegireff/dovidnyk-vyzhyvannia) (CC BY-SA 4.0); малюнки — з того самого довідника. Цей матеріал лишається під [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) зі збереженим авторством, окремо від коду під GPL-3.0. Англійський текст — переклад «Спротиву», теж CC BY-SA 4.0. Фахівець картки ще не перевіряв. · **"What to do" cards:** word for word from Sprotyv, an adaptation of the open survival guide "Довідник виживання" 2.3 (CC BY-SA 4.0), with its pictures; they stay under CC BY-SA 4.0 with their authorship kept, separate from the GPL-3.0 code. Not yet reviewed by a specialist.
- `app/src/main/res/drawable/pict_press.xml` — піктограма зі «Спротиву», складена з Material Symbols Rounded (Google, Apache License 2.0). · A pictogram from Sprotyv, composed from Material Symbols Rounded (Google, Apache 2.0).
