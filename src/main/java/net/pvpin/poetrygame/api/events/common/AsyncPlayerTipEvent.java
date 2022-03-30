package net.pvpin.poetrygame.api.events.common;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncPlayerTipEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected Player player;
    protected BaseComponent tip;
    protected boolean cancelled = false;

    public AsyncPlayerTipEvent(Game game, Player player, BaseComponent tip) {
        super(true);
        this.game = game;
        this.player = player;
        this.tip = tip;
    }

    public AsyncPlayerTipEvent(Game game, Player player, String tip) {
        super(true);
        this.game = game;
        this.player = player;
        this.tip = new TextComponent(tip);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = cancelled;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public BaseComponent getTip() {
        return tip;
    }

    public void setTip(BaseComponent tip) {
        this.tip = tip;
    }
}
