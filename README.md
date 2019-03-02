# Nbt Crafting - Dollar Update

![logo](images/logo_dollar_big.png?raw=true)

## About
A fabric mod which allows you to add/change JSON crafting/cooking/etc. recipes to use nbt input and output.

Everything is kept nicely visualized in the vanilla gui.

This can be achieved through the reintroduced `data` attribute. 

**You can find more information in the [wiki](https://github.com/Siphalor/nbt-crafting/wiki).**

## Example
A simple recipe allowing to craft wooden swords with 20 damage from diamond swords with 2 damage.

```json
{
  "type": "crafting_shapeless",
  "ingredients": [
    {
      "item": "minecraft:diamond_sword",
	  "data": "{Damage:2}"
    }
  ],
  "result": {
    "item": "minecraft:wooden_sword",
	"data": "{Damage:20}"
  }
}
```

![Demo](images/demo.png?raw=true)

## Extra
Due to some additions made with this mod the recipe book displays also the output amount of all recipes - yay!

## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
