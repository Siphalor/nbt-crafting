# Nbt Crafting - Dollar Update

[![curseforge downloads](http://cf.way2muchnoise.eu/full_nbt-crafting_downloads.svg)](https://minecraft.curseforge.com/projects/nbt-crafting)
[![curseforge mc versions](http://cf.way2muchnoise.eu/versions/nbt-crafting.svg)](https://minecraft.curseforge.com/projects/nbt-crafting)

![logo](images/logo_variation_big.png?raw=true)

## About
A fabric mod which allows you to add/change JSON crafting/cooking/etc. recipes to use nbt input and output.

Everything is kept nicely visualized in the vanilla gui.

This can be achieved through the reintroduced `data` attribute. 

**You can find more information in the [wiki](https://mcwiki.siphalor.de/nbt-crafting/v2).**

## Example
A simple recipe allowing to craft wooden swords with 20 damage from diamond swords with 2 damage.

```json
{
  "type": "crafting_shapeless",
  "ingredients": [
    {
      "item": "minecraft:diamond_sword",
      "data": {
      	"require": {
          "Damage": 2
	}
      }
    }
  ],
  "result": {
    "item": "minecraft:wooden_sword",
    "data": {
      "Damage": 2
    }
  }
}
```

## Extra
Due to some additions made with this mod the recipe book displays also the output amount of all recipes - yay!

## License

This mod is available under The Unlicense license. Feel free to learn from it and incorporate it in your own projects.
