# Reality Fracture — Horror Mod for Minecraft 1.7.10 (Forge)

> *"They were always watching. The world was never stable. You just didn't notice until now."*

---

## Структура проекта

```
horrormod/
├── build.gradle
└── src/main/
    ├── java/com/horrormod/
    │   ├── HorrorMod.java                    ← @Mod главный класс
    │   ├── CommonProxy.java                  ← Серверная сторона
    │   ├── client/
    │   │   ├── ClientProxy.java              ← Регистрация рендереров
    │   │   └── SettingsManipulator.java      ← Искажение настроек (CLIENT ONLY)
    │   ├── config/
    │   │   └── HorrorConfig.java             ← Конфигурация (forge config file)
    │   ├── entity/
    │   │   ├── EntityRegistry.java           ← Регистрация всех сущностей
    │   │   ├── EntityTheWatcher.java         ← Моб-наблюдатель
    │   │   ├── EntityHerobrine.java          ← Херобрин
    │   │   ├── EntityIllusionPlayer.java     ← Иллюзорный игрок
    │   │   ├── EntityTheFracture.java        ← Босс "The Fracture"
    │   │   ├── RenderTheWatcher.java         ← Рендерер Watcher
    │   │   ├── RenderHerobrine.java          ← Рендерер Herobrine
    │   │   ├── RenderIllusionPlayer.java     ← Рендерер иллюзии
    │   │   └── RenderTheFracture.java        ← Рендерер Fracture
    │   ├── event/
    │   │   └── HorrorEventHandler.java       ← Центральный обработчик событий
    │   └── world/
    │       └── RealityTearEvent.java         ← Удаление чанков
    └── resources/
        ├── mcmod.info
        └── assets/horrormod/
            ├── sounds.json
            ├── lang/en_US.lang
            ├── sounds/                       ← Сюда кладёте .ogg файлы
            │   ├── whisper1.ogg
            │   ├── static1.ogg
            │   └── ...
            └── textures/entity/
                ├── watcher.png               ← Текстура Watcher (64×64)
                ├── herobrine.png             ← Текстура Herobrine (64×64, белые глаза)
                └── fracture.png              ← Текстура Fracture (64×64, искажённая)
```

---

## Механики

### 1. Случайные звуки
Каждые **30 сек – 5 минут** рядом с игроком проигрывается один из:
- `ambient.cave.cave` — пещерные шумы
- `mob.endermen.stare` — взгляд эндермена
- `mob.ghast.moan` — вой гаста
- `mob.endermen.idle` — шёпот эндермена
- И другие ванильные звуки

Настраивается в конфиге: `soundMinInterval`, `soundMaxInterval`.

### 2. Разрыв реальности (Chunk Tear)
Каждые **15–45 минут** случайный чанк в радиусе 64 блоков **стирается в воздух**.
- Дым, звук взрыва + портала
- Жуткое сообщение в чате
- Если `chunkRestoreDays > 0` — через N игровых дней чанк восстанавливается

### 3. Иллюзорный игрок
В мультиплеере каждые **10–30 минут** появляется призрак другого игрока:
- Копирует скин настоящего игрока
- Стоит в **16–32 блоках**, смотрит на цель
- Исчезает при приближении (< 8 блоков)
- **Без ника**, полупрозрачный, тихо шепчет

### 4. Херобрин
Раз в **1–3 игровых дня** появляется на расстоянии **48–64 блоков**:
- Стандартная фигура игрока, **белые глаза**
- Следит взглядом
- Исчезает с порталом, когда игрок смотрит на него, или через **15–30 сек**
- **Никогда не атакует**

### 5. The Watcher (Наблюдатель)
Каждые **2–6 минут** появляется в **18–26 блоках**:
- Статичная фигура с искажённой текстурой
- Исчезает при приближении (< 5 блоков) или ударе
- Живёт **10–15 секунд**

### 6. Искажение настроек (CLIENT-ONLY)
Каждые **1–3 минуты** на **30–60 секунд**:
- Яркость → 0 (кромешная тьма)
- Дальность прорисовки → 2 чанка
- FOV медленно качается от 30° до 110°
- Громкость скачет случайно

После окончания — **все настройки восстанавливаются**.

### 7. Ложные эффекты
Каждые **90 сек – 4 минуты** происходит одно из:
- Мгновенное обнуление здоровья (иллюзия смерти + звук удара)
- Сброс полоски голода
- Перемещение предмета в инвентаре

### 8. Босс The Fracture
Раз в **2–5 игровых дней**:
- Появляется в **20–36 блоках** от игрока
- Ломает блоки на пути к цели
- Телепортируется (глитч-эффект)
- **200 HP**, неуязвим к отбрасыванию
- При смерти — взрыв порталных частиц

---

## Сборка

```bash
# Требуется Forge MDK 1.7.10-10.13.4.1614
gradle setupDecompWorkspace
gradle build
```

Готовый `.jar` появится в `build/libs/`.

---

## Текстуры

Положите в `src/main/resources/assets/horrormod/textures/entity/`:

| Файл | Описание |
|------|----------|
| `watcher.png` | 64×64 — тёмная, размытая фигура |
| `herobrine.png` | 64×64 — стандартный скин с **белыми глазами** |
| `fracture.png` | 64×64 — искажённая, разломанная текстура |

---

## Звуки

Положите `.ogg` файлы в `src/main/resources/assets/horrormod/sounds/`:
- `whisper1.ogg`, `whisper2.ogg`, `whisper3.ogg`
- `static1.ogg` (помехи)
- `childlaugh1.ogg` (детский смех)
- `footsteps1.ogg`, `footsteps2.ogg`
- `scrape1.ogg`

---

## Конфигурация

Файл создаётся автоматически: `config/horrormod.cfg`

```ini
# Интервал звуков (секунды)
S:soundMinInterval=30
S:soundMaxInterval=300

# Разрыв чанков (минуты)
S:chunkTearMin=15
S:chunkTearMax=45
S:chunkTearRadius=64
S:chunkRestoreDays=5

# Херобрин (игровые дни)
S:herobrineMinDays=1
S:herobrineMaxDays=3

# The Fracture (игровые дни)
S:fractureMinDays=2
S:fractureMaxDays=5

# Ложные эффекты (секунды)
S:falseEffectMin=90
S:falseEffectMax=240
```

---

## Важные замечания

1. **Текстуры** — необходимо создать самостоятельно (или скачать/нарисовать).
2. **Звуки** — мод использует стандартные ванильные звуки + слоты для кастомных `.ogg`.
3. **Мультиплеер** — иллюзорные игроки активны только при 2+ игроках на сервере.
4. `SettingsManipulator` регистрируется **только на клиенте** через `ClientProxy`.
5. `RealityTearEvent.checkAndRestoreChunks()` нужно вызывать из тик-хендлера раз в сутки (добавить в `HorrorEventHandler`).

---

*"The chunks were always empty. The figure was always there. You just couldn't see it before."*
