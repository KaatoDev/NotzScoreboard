<div align="center">
  
##### **NotzPlugins**
<a href="https://modrinth.com/plugin/notzexcavate">
<img src="https://repo.kaato.dev/images/plugins/NotzExcavate2.png" alt="Notz Excavate" width="150"/>
</a>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard2.png" alt="Notz Scoreboard" width="150"/>
<a href="https://modrinth.com/plugin/notzwarps">
<img src="https://repo.kaato.dev/images/plugins/NotzWarps2.png" alt="Notz Warps" width="150"/>
</a>
  
#
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard2.png" alt="NotzScoreboardV3" height="300" >

#
NotzScoreboard is a complete and fully customizable scoreboard plugin that features: support for multiple simultaneous scoreboards; its own placeholders and PlaceholderAPI; database via MySQL or SQLite and dynamic updating for greater efficiency.

<br/>

## Information

### Scoreboards
The plugin has dynamic scoreboards that only update the placeholders inserted in each line, without having to reload the scoreboard completely!
It also contains multiple customization commands so that they can be created and modified directly from the server without having to mess with the files.

### Groups
The scoreboards are separated by groups and pre-programmed so that it is possible to view players from other groups (and even from the same group) on your scoreboard or from the group itself on other scoreboards.

### Templates
It also has a system of templates that can be created in the plugin's configuration file and set through the game using them as Header, Footer or the main template, thus allowing greater customization and standardization of each scoreboard.

### Placeholders
You can choose to use dynamic placeholders from other plugins via the PlaceholderAPI or your own static placeholders that can be created via the plugin's configuration file.

### Color codes
The plugin allows you to use the old color code system and short color codes instead of creating long <color> strings in every scoreboard line or in messages sent to players.

### Tip
For questions or issues: [Discord @Gago32](https://discord.com/users/258701013198831617)

<br/>

## Scoreboard Demo

<br/>

### Player example with staff on and off
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Player3-1.png" alt="Player1 scoreboard" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Player3-2.png" alt="Player2 scoreboard" width="300"/>
</div>
</details>

<br/>

### Helper and Trial example
<details>
<summary>Show</summary>
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Helper3.png" alt="Helper scoreboard" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Trial3.png" alt="Trial scoreboard" width="300"/>
</div>
</details>

<br/>

### Mod and Admin example
<details>
<summary>Show</summary>
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Mod3.png" alt="Mod scoreboard" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Admin3.png" alt="Admin scoreboard" width="300"/>
</div>
</details>

<br/>

### Manager and Owner example
<details>
<summary>Show</summary>
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Manager3.png" alt="Manager scoreboard" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Owner3.png" alt="Owner scoreboard" width="300"/>
</div>
</details>
</div>

<br/>

## Scoreboard system

### Multiline separator: `--`
Use `--` or more hyphens to distinguish lines.
### Prefix and Suffix line separators:
`Placeholder` (the suffix), `:` (separate and keeps the ":"), `::` (only separates).

### Placeholder system
It doesn't matter if you put a placeholder with `{}` or `%%`!
The `:` in placeholders defines a default message if the placeholder doesn't exist.

### Note:
Player clients in versions <1.20.3 have short lines, so the max lenght for an area in a scoreboard line (prefix, entry and suffix) is 16 chars each, making the prefix area (before a 'placeholder' or ':') only 32 chars long (counting the color codes §/&).

<details>
<summary>Example</summary>
  
```yml
templates:
  - player:
      - ''
      - '⧽ {player_displayname}'
      - ''
      - '⎧ {rank}'
      - '⎩ {status_rankup}'
      - ''
      - '⎧ Money: {money:0.0}'
      - '--⎩ Cash&f: {cash:0c}'
      - '--⎩ Tokens&f: {tokens:0t}'
      - ''
      - '⎧ Clan: {simpleclans_clan:No clan}'
      - '⎩ KDR: {simpleclans_kdr:0.0}'
```
</details>

<br/>

## Custom color code system

### Legacy color codes
Use `&` along with any legacy color code.\
Example: `&b` (Old color AQUA).

### Gradient with old color codes
Use `&g` along with any legacy color code.\
Example: `&g2b` (Gradient from GREEN to AQUA).

### Gradient with hexadecimal colors
Use `&g[_color1_:_color..._]` along with the hexadecimal color, whether short (#f0f) or long (#ff00ff).\
Example: `&g[#fff:#000]` (Gradient from WHITE to BLACK).

### Pride colors
Use `&p` alone or `&p[_flag_]` together with any [pride flag name](https://docs.papermc.io/adventure/minimessage/format/#pride).\
Example: `&p[bi]` (Bi flag gradient).

### Rainbow
Use `&!` alone or together with [! or phase](https://docs.papermc.io/adventure/minimessage/format/#rainbow).\
Example: `&!` (Normal rainbow).\
Example: `&!!` (Inverted rainbow).\
Example: `&!2` (Rainbow phase 2).\
Example: `&!!2` (Inverted rainbow and phase 2).

### Chat event
Use `&&_action_[_value_](_message_)` with any supported action:
[copy, url, run, sug|suggest](https://docs.papermc.io/adventure/minimessage/format/#click)\
[hover](https://docs.papermc.io/adventure/minimessage/format/#hover)\
Example: `&&hover[Boooo!](Hover over this message to see something.)` (Display a message on hover).\
Example: `&&copy[You copied me!](Click here to copy something.)` (Copies a message).\
Example: `&&run[/tutorial](Click here to run the tutorial!)` (Executes a command).\
Example: `&&sug[/bc yipeee](Click here to be happy!)` (Suggests a command).

### End code
Use `&.` to terminate a color code (the '&.' will be removed from the message). It will also be terminated when another color code begins (auto detects another `&`).

<br/>

## Dependencies
- PlaceholderAPI (Optional)
- ViaVersion (Optional)

<br/>

## Placeholders
 - `{money}` - Player money straight from VaultUnlocked and formmated by NotzAPI. [PlaceholderAPI]
 - `{staff}` - Get the nick of one of the online players in the visible groups for that group.
 - `{supstaff}` - Same thing as {staff}, but returns an alternative message if there is no online player in the visible groups for that group.
 - `{staff_list}` - Number of online players in the visible groups for that group.
 - `{staff_'group'}` - Get one of the players in that group.
 - `{'group'_list}` - Gets the number of online players in that group.

<br/>

## Permissions
- `notzscoreboard.admin` - Enables the player to use the /notzsb admin command.

<br/>

## Commands
### `/notzsb`
 - `cleanOldDatabase` - Deletes the old database;
 - `convert` <all/<scoreboard>> - Convert all the old players and all or a specifically one of the old Scoreboards;
 - `create` \<name> \<display> (header) (template) (footer) - Creates the scoreboard with the option of already setting the templates.
 - `delete` \<scoreboard> - Deletes a scoreboard.
 - `list` - Lists all the scoreboards created.
 - `players` - Lists all registered players and their respective scoreboards.
 - `reload` - Reloads parts of the plugin.
 - `reset` \<player> - Resets the player's scoreboard to the default scoreboard.
 - `set` \<scoreboard> - Sets the scoreboard itself.
 - `update` - Updates all scoreboards.
 - `<scoreboard>`
   - `addplayer` <player> - Adds a player to the scoreboard.
   - `addgroup` <group> - Adds a group to the scoreboard's VisibleGroups.
   - `clearheader` - Clears the scoreboard header.
   - `clearfooter` - Clears the footer of the scoreboard.
   - `cleartemplate` - Clears the scoreboard template.
   - `pause` (minutes) - Pause the scoreboard update for X minutes (by default it is paused for 1 minute).
   - `players` - View the players registered on the scoreboard.
   - `remplayer` \<player> - Remove a player from the scoreboard.
   - `remgroup` \<group> - Remove a VisibleGroups group from the scoreboard
   - `setcolor` \<color> - Sets a new color for the scoreboard.
   - `setdisplay` \<display> - Sets a new display on the scoreboard.
   - `setheader` \<template> - Set a new header on the scoreboard.
   - `setfooter` \<template> - Set a new footer on the scoreboard.
   - `settemplate` \<template> - Sets a new template on the scoreboard.
   - `view` - View the scoreboard without having to set it.
   - `visiblegroups` - View the visible groups inserted in the group.

<br/>
<sub> | <> required argument. | ( ) optional argument. | </sub>

#

<sub> Tested versions: 1.8 - 1.21.11 </sub>
