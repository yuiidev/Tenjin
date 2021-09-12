# Relationships

When saving a newly created object that has relationships, make sure to save the relationships in the correct order
so as not to have foreign keys fail.

For example adding a player to a new guild as the guild master:

```java
import net.demozo.tenjin.Tenjin;

public class AddPlayerToNewGuildExample {
    /**
     * In this example the player already exists in the database.
     */
    public void addPlayerToNewGuild(Player player) {
        Guild guild = new Guild("Guild Name", "GN", player.getUniqueId());
        GuildMember guildMember = new GuildMember(guild, player, "GUILD_MASTER");

        // WRONG, foreign key will fail, because the guild has not yet been saved to the database
        Tenjin.save(guildMember);
        Tenjin.save(guild);
        
        // RIGHT, foreign key will not fail because the guild HAS been saved to the database
        Tenjin.save(guild);
        Tenjin.save(guildMember);
    }
}
```

This example uses a n-n relationship with pivot properties to demonstrate the correct order of saving object, 
however this goes for any type of relationship.p

When faced with an exception where a value is null, double check whether or not the columns in the database match the
defined columns in your Java classes. Especially their relationship types. Only `BelongsTo` type relationships get saved
with the record you're saving. The data of a `BelongsTo` relationship type, lives on the model itself. Whereas a `Has`
relationship type points to the other Model to check if it is the right one. For example a `User` _has_ a `Phone`, and a
`Phone` _belongs to_ a `User`. The `User` can exist without a `Phone` but the `Phone` can't exist without the `User`,
therefore the foreign key is placed on the `Phone` model.