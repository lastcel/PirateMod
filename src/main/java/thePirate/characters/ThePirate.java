package thePirate.characters;

import basemod.abstracts.CustomPlayer;
import basemod.animations.SpineAnimation;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.cutscenes.CutscenePanel;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thePirate.PirateMod;
import thePirate.cards.attacks.AbstractCannonBallCard;
import thePirate.cards.attacks.RoundShot;
import thePirate.cards.attacks.Strike;
import thePirate.cards.skills.Defend;
import thePirate.cards.skills.Reload;
import thePirate.relics.BetterOnUseGold;
import thePirate.relics.GunsmithsBible;
import thePirate.relics.MoneyBag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static thePirate.PirateMod.*;
import static thePirate.characters.ThePirate.Enums.COLOR_GRAY;

//Wiki-page https://github.com/daviscook477/BaseMod/wiki/Custom-Characters
//and https://github.com/daviscook477/BaseMod/wiki/Migrating-to-5.0
//All text (starting description and loadout, anything labeled TEXT[]) can be found in DefaultMod-character-Strings.json in the resources

public class ThePirate extends CustomPlayer {
    public static final Logger logger = LogManager.getLogger(PirateMod.class.getName());

    // =============== CHARACTER ENUMERATORS =================
    // These are enums for your Characters color (both general color and for the card library) as well as
    // an enum for the name of the player class - IRONCLAD, THE_SILENT, DEFECT, YOUR_CLASS ...
    // These are all necessary for creating a character. If you want to find out where and how exactly they are used
    // in the basegame (for fun and education) Ctrl+click on the PlayerClass, CardColor and/or LibraryType below and go down the
    // Ctrl+click rabbit hole

    public static class Enums {
        @SpireEnum
        public static AbstractPlayer.PlayerClass THE_PIRATE;
        @SpireEnum(name = "PIRATE_PURPLE_COLOR") // These two HAVE to have the same absolutely identical name.
        public static AbstractCard.CardColor COLOR_GRAY;
        @SpireEnum(name = "PIRATE_PURPLE_COLOR") @SuppressWarnings("unused")
        public static CardLibrary.LibraryType LIBRARY_COLOR;
    }

    // =============== CHARACTER ENUMERATORS  =================


    // =============== BASE STATS =================

    public static final int ENERGY_PER_TURN = 3;
    public static final int STARTING_HP = 75;
    public static final int MAX_HP = 75;
    public static final int STARTING_GOLD = 99;
    public static final int CARD_DRAW = 5;
    public static final int ORB_SLOTS = 0;

    // =============== /BASE STATS/ =================


    // =============== STRINGS =================

    private static final String ID = makeID("PirateCharacter");
    private static final CharacterStrings characterStrings = CardCrawlGame.languagePack.getCharacterString(ID);
    private static final String[] NAMES = characterStrings.NAMES;
    private static final String[] TEXT = characterStrings.TEXT;

    public static int energyUsedThisTurn;

    // =============== /STRINGS/ =================

    private boolean isCannonAnimation;

    // =============== CHARACTER CLASS START =================

    public ThePirate(String name, PlayerClass setClass) {
        super(name, setClass, null, null, new SpineAnimation(
                "thePirateResources/images/char/defaultCharacter/idle/skeleton.atlas",
               "thePirateResources/images/char/defaultCharacter/idle/skeleton.json",1f ));
/*
        super(name, setClass, null,
                "thePirateResources/images/char/defaultCharacter/orb/vfx.png", null,
                new SpriterAnimation(
                        "thePirateResources/images/char/defaultCharacter/Spriter/theDefaultAnimation.scml"));
*/


        // =============== TEXTURES, ENERGY, LOADOUT =================  

        initializeClass(null, // required call to load textures and setup energy/loadout.
                // I left these in DefaultMod.java (Ctrl+click them to see where they are, Ctrl+hover to see what they read.)
                THE_DEFAULT_SHOULDER_2, // campfire pose
                THE_DEFAULT_SHOULDER_1, // another campfire pose
                THE_DEFAULT_CORPSE, // dead corpse
                getLoadout(), 20.0F, -10.0F, 220.0F, 290.0F, new EnergyManager(ENERGY_PER_TURN)); // energy manager

        // =============== /TEXTURES, ENERGY, LOADOUT/ =================


        // =============== ANIMATIONS =================  

        loadAnimation(
                THE_DEFAULT_SKELETON_ATLAS,
                THE_DEFAULT_SKELETON_JSON,
                1.0f);
        AnimationState.TrackEntry e = state.setAnimation(0, "animtion0", true);
        e.setTime(e.getEndTime() * MathUtils.random());

        // =============== /ANIMATIONS/ =================


        // =============== TEXT BUBBLE LOCATION =================

        dialogX = (drawX + 0.0F * Settings.scale); // set location for text bubbles
        dialogY = (drawY + 220.0F * Settings.scale); // you can just copy these values

        // =============== /TEXT BUBBLE LOCATION/ =================

    }

    // =============== /CHARACTER CLASS END/ =================

    // Starting description and loadout
    @Override
    public CharSelectInfo getLoadout() {
        return new CharSelectInfo(NAMES[0], TEXT[0],
                STARTING_HP, MAX_HP, ORB_SLOTS, STARTING_GOLD, CARD_DRAW, this, getStartingRelics(),
                getStartingDeck(), false);
    }

    // Starting Deck
    @Override
    public ArrayList<String> getStartingDeck() {
        ArrayList<String> retVal = new ArrayList<>();

        logger.info("Begin loading starter Deck Strings");

        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(RoundShot.ID);
        retVal.add(Reload.ID);

        return retVal;
    }

    // Starting Relics	
    public ArrayList<String> getStartingRelics() {
        ArrayList<String> retVal = new ArrayList<>();

        retVal.add(GunsmithsBible.ID);
/*
        // Mark relics as seen - makes it visible in the compendium immediately
        // If you don't have this it won't be visible in the compendium until you see them in game
        UnlockTracker.markRelicAsSeen(PlaceholderRelic.ID);
        UnlockTracker.markRelicAsSeen(PlaceholderRelic2.ID);
        UnlockTracker.markRelicAsSeen(DefaultClickableRelic.ID);
*/

        return retVal;
    }

    @Override
    public void useCard(AbstractCard c, AbstractMonster monster, int energyOnUse) {
        if (c instanceof AbstractCannonBallCard){
            isCannonAnimation = true;
        }
        else {
            isCannonAnimation = false;
        }
        super.useCard(c, monster, energyOnUse);
    }

    @Override
    protected void updateFastAttackAnimation() {
        if (isCannonAnimation){
            this.animationTimer -= Gdx.graphics.getDeltaTime();
            float targetPos = 90.0F * Settings.scale;
            if (!this.isPlayer) {
                targetPos = -targetPos;
            }

            if (this.animationTimer > 0.5F) {
                this.animX = -(Interpolation.exp5In.apply(0.0F, targetPos, (1.0F - this.animationTimer / 1.0F) * 2.0F));
            } else if (this.animationTimer < 0.0F) {
                this.animationTimer = 0.0F;
                this.animX = 0.0F;
            } else {
                this.animX = -(Interpolation.fade.apply(0.0F, targetPos, (this.animationTimer / 1.0F * 2.0F)));
            }
        }
        else {
            super.updateFastAttackAnimation();
        }


    }

    // character Select screen effect
    @Override
    public void doCharSelectScreenSelectEffect() {
        PirateMod.sound.playA("CANNON_HIT_SHIP", 0f); // Sound Effect
        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.HIGH, ScreenShake.ShakeDur.SHORT,
                false); // Screen Effect
    }

    // character Select on-button-press sound effect
    @Override
    public String getCustomModeCharacterButtonSoundKey() {
        return "ATTACK_DAGGER_1";
    }

    // Should return how much HP your maximum HP reduces by when starting a run at
    // Ascension 14 or higher. (ironclad loses 5, defect and silent lose 4 hp respectively)
    @Override
    public int getAscensionMaxHPLoss() {
        return 4;
    }

    // Should return the card color enum to be associated with your character.
    @Override
    public AbstractCard.CardColor getCardColor() {
        return COLOR_GRAY;
    }

    // Should return a color object to be used to color the trail of moving cards
    @Override
    public Color getCardTrailColor() {
        return PirateMod.PIRATE_PURPLE.cpy();
    }

    // Should return a BitmapFont object that you can use to customize how your
    // energy is displayed from within the energy orb.
    @Override
    public BitmapFont getEnergyNumFont() {
        return FontHelper.energyNumFontRed;
    }

    // Should return class name as it appears in run history screen.
    @Override
    public String getLocalizedCharacterName() {
        return NAMES[0];
    }

    //Which card should be obtainable from the Match and Keep event?
    @Override
    public AbstractCard getStartCardForEvent() {
        return new RoundShot();
    }

    // The class name as it appears next to your player name in-game
    @Override
    public String getTitle(AbstractPlayer.PlayerClass playerClass) {
        return NAMES[1];
    }

    // Should return a new instance of your character, sending name as its name parameter.
    @Override
    public AbstractPlayer newInstance() {
        return new ThePirate(name, chosenClass);
    }

    // Should return a Color object to be used to color the miniature card images in run history.
    @Override
    public Color getCardRenderColor() {
        return PirateMod.PIRATE_PURPLE.cpy();
    }

    // Should return a Color object to be used as screen tint effect when your
    // character attacks the heart.
    @Override
    public Color getSlashAttackColor() {
        return PirateMod.PIRATE_PURPLE.cpy();
    }

    // Should return an AttackEffect array of any size greater than 0. These effects
    // will be played in sequence as your character's finishing combo on the heart.
    // Attack effects are the same as used in DamageAction and the like.
    @Override
    public AbstractGameAction.AttackEffect[] getSpireHeartSlashEffect() {
        return new AbstractGameAction.AttackEffect[]{
                AbstractGameAction.AttackEffect.BLUNT_HEAVY,
                AbstractGameAction.AttackEffect.BLUNT_HEAVY,
                AbstractGameAction.AttackEffect.BLUNT_HEAVY};
    }

    @Override
    public List<CutscenePanel> getCutscenePanels() {
        List<CutscenePanel> panels = new ArrayList();
        panels.add(new CutscenePanel(PirateMod.getModID() + "Resources/images/scenes/pirate1.png", "BLUNT_HEAVY"));
        panels.add(new CutscenePanel(PirateMod.getModID() + "Resources/images/scenes/pirate2.png"));
        panels.add(new CutscenePanel(PirateMod.getModID() + "Resources/images/scenes/pirate3.png"));
        return panels;
    }

    // Should return a string containing what text is shown when your character is
    // about to attack the heart. For example, the defect is "NL You charge your
    // core to its maximum..."
    @Override
    public String getSpireHeartText() {
        return TEXT[1];
    }

    // The vampire events refer to the base game characters as "brother", "sister",
    // and "broken one" respectively.This method should return a String containing
    // the full text that will be displayed as the first screen of the vampires event.
    @Override
    public String getVampireText() {
        return TEXT[2];
    }

    @Override
    public void loseGold(int goldAmount) {
        logger.info("enter ThePirate.loseGold(): " + goldAmount);

        Iterator var2;
        AbstractRelic r;
        if (AbstractDungeon.getCurrRoom() instanceof ShopRoom) {
            var2 = this.relics.iterator();

            while(var2.hasNext()) {
                r = (AbstractRelic)var2.next();
                if (r instanceof BetterOnUseGold){
                    ((BetterOnUseGold) r).onSpendGold(goldAmount);
                }
            }
        }else {
            var2 = this.relics.iterator();

            while(var2.hasNext()) {
                r = (AbstractRelic)var2.next();
                int counter = r.counter;
                if (r instanceof BetterOnUseGold){
                    ((BetterOnUseGold) r).onLoseGold(goldAmount);
                }
                if (r instanceof MoneyBag){
                    if (goldAmount <= counter){
                        goldAmount = 0;
                    }else{
                        goldAmount -= counter;
                    }
                }
            }
            if (powers != null){
                for (AbstractPower power : powers){
                    if (power instanceof BetterOnUseGold){
                        ((BetterOnUseGold) power).onLoseGold(goldAmount);

                    }
                }
            }

        }
        super.loseGold(goldAmount);
    }
}
