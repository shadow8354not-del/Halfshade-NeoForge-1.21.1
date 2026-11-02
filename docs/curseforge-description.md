# Halfshade — An Origins Addon

A fallen warrior caught between humanity and shadow. Become a Halfshade, slip through walls, strike with wither-infused swords, and fight through your final breath.

Halfshade adds one playable origin to **Origins (NeoForge) 0.3 by IAFEnvoy** for **Minecraft Java 1.21.1**.

## Abilities

- **Night Wind:** gain Speed I at night.
- **Shadow Form:** press the Origins primary ability key to toggle your shadow form. Phase through walls, or sneak to phase down through the floor. Blocks prohibited by Origins remain impassable.
- **Shadow Venom:** while in shadow form, sword hits inflict Wither I for 3 seconds, with a shared 6-second cooldown. A short message confirms a successful application. This ability does not work in human form.
- **Last Breath:** lethal damage gives you a final chance to fight. You enter a forced shadow form with 3.5 hearts, Strength II, and Speed I. Enemies can finish you off, and you die after 10 seconds regardless. Hunger cannot cancel this state.
- **No Legs:** take no fall damage while in shadow form.

## The price of shadow

- You have **7 hearts** in your normal and shadow forms.
- Fire and lava damage are doubled, and Totems of Undying cannot save you.
- Shadow form multiplies normal exhaustion by **5**. Saturation is consumed before hunger; simply standing still does not create a separate hunger drain.
- In shadow form you cannot eat, drink, use items, pick up items, interact with blocks or entities, or break blocks. You may attack with swords while outside solid blocks.
- Direct sunlight sets your shadow form on fire.
- You cannot manually leave shadow form inside a solid block. At **6 hunger points / 3 food icons**, the form ends automatically, even inside a wall. Plan your escape carefully!
- Leaving solid blocks in shadow form causes **Blindness for 2 seconds**.

## Hunger warning

A black vignette appears in shadow form. It turns bright red at **10 hunger points / 5 food icons**, warning that only two food icons remain before the form switches off. The centre remains clear, and the vignette uses reduced opacity.

## Requirements and installation

- Minecraft Java Edition **1.21.1**
- **NeoForge 21.1.228 or later in the 21.1 branch**
- **Origins (NeoForge), exactly version 0.3, by IAFEnvoy**
- **Jupiter**, the dependency required by that Origins port

Install the Halfshade JAR in your `mods` folder alongside the required mods. For multiplayer, install it on both the server and clients. Choose **Полутень** in the Origins selection screen. The current in-game ability names and messages are in Russian.

Use the key assigned to **Origins Primary Active** in Controls. To select the origin in an existing world, use an Orb of Origin or this administrator command:

```text
/origin set @s origins:origin poluten:poluten
```

This addon targets this specific NeoForge port; other Origins ports and versions have not been verified. Ability durations use game ticks: 10 seconds assumes the server runs at 20 TPS.

## Development

The concept, abilities, and balance decisions were created by the project author. The implementation, project icon, and parts of the project text were created with AI assistance, followed by automated server checks and manual gameplay testing by the author. This is an independent community addon, not an official Origins release.

---

## Русское описание

**Полутень** — происхождение воина, проигравшего сражение с тьмой и ставшего получеловеком-полутенью.

Ночью вы получаете Скорость I. Основная способность позволяет проходить сквозь стены, а приседание — сквозь пол. В теневой форме удары мечом накладывают Иссушение I на 3 секунды с общим кулдауном 6 секунд; урон от падения отсутствует.

Цена способностей: 7 сердец, удвоенный урон от огня и лавы, неработающие тотемы. В тени обычное истощение увеличено в 5 раз; нельзя есть, пить, использовать и подбирать предметы, взаимодействовать и ломать блоки. На солнце тень загорается.

При 5 значках сытости чёрная виньетка становится ярко-красной. На 3 значках форма отключается даже внутри стены. Вручную выйти из тени внутри блока нельзя. После выхода из блоков в теневой форме наступает слепота на 2 секунды.

**Последний вздох:** при смертельном уроне остаются 3,5 сердца, включается принудительная теневая форма, Сила II и Скорость I. Вас можно добить; через 10 секунд смерть неизбежна. Голод не отключает эту форму.

Нужны Minecraft 1.21.1, NeoForge, Origins (NeoForge) строго 0.3 от IAFEnvoy и Jupiter. Дополнение устанавливается в `mods` на сервере и клиентах. Идея и баланс принадлежат автору проекта; реализация создана с помощью ИИ.
