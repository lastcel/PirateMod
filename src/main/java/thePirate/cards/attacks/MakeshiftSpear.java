package thePirate.cards.attacks;

import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.SpawnModificationCard;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.UpgradeRandomCardAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.watcher.LessonLearnedAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.ShiningLight;
import com.megacrit.cardcrawl.events.shrines.Designer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.MoltenEgg2;
import com.megacrit.cardcrawl.relics.TinyHouse;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.combat.CleaveEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import thePirate.PirateMod;
import thePirate.cards.AbstractDynamicCard;
import thePirate.cards.Makeshift;
import thePirate.characters.ThePirate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.megacrit.cardcrawl.core.CardCrawlGame.languagePack;
import static thePirate.PirateMod.makeCardPath;

public class MakeshiftSpear extends AbstractDynamicCard implements Makeshift, SpawnModificationCard {

    private boolean purge;
    public boolean queuedForPurge;

    // STAT DECLARATION
    private static final CardRarity RARITY = CardRarity.COMMON; //  Up to you, I like auto-complete on these
    private static final CardTarget TARGET = CardTarget.ALL_ENEMY;  //   since they don't change much.
    private static final CardType TYPE = CardType.ATTACK;       //
    public static final CardColor COLOR = ThePirate.Enums.COLOR_GRAY;

    private static final int COST = 1;
    private static final int UPGRADED_COST = 1;

    private static final int DAMAGE = 8;
    private static final int UPGRADE_PLUS_DMG = 0;
    // /STAT DECLARATION/

    // TEXT DECLARATION
    public static final String ID = PirateMod.makeID(MakeshiftSpear.class.getSimpleName());
    public static final String IMG = makeCardPath(MakeshiftSpear.class.getSimpleName() + ".png", TYPE);
    // /TEXT DECLARATION/

    public MakeshiftSpear() {
        super(ID, IMG, COST, TYPE, COLOR, RARITY, TARGET);
        damage = baseDamage = DAMAGE;
        this.isMultiDamage = true;
    }

    @Override
    public boolean canSpawnShop(ArrayList<AbstractCard> currentShopCards) {
        //don't allow spawning if player has molten egg
        return !AbstractDungeon.player.hasRelic(MoltenEgg2.ID);
    }

    @Override
    public boolean canSpawn(ArrayList<AbstractCard> currentRewardCards) {
        //don't allow spawning if player has molten egg
        return !AbstractDungeon.player.hasRelic(MoltenEgg2.ID);
    }

    //TODO: if there are any other makeshift cards, this needs to be generalized.
    @Override
    public boolean canUpgrade() {
        boolean canUpgrade = true;
        //can't be randomly upgraded at combat reward screens
        if  (AbstractDungeon.currMapNode == null || AbstractDungeon.CurrentScreen.COMBAT_REWARD.equals(AbstractDungeon.screen)){
            canUpgrade = false;
        }
        //don't allow upgrading in random events
        else if(AbstractDungeon.getCurrRoom() != null){
            AbstractEvent event = AbstractDungeon.getCurrRoom().event;
            if(event instanceof Designer || event instanceof ShiningLight){
                canUpgrade = false;
            }
        }
        return canUpgrade;
    }

    // Actions the card should do.
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        this.addToBot(new SFXAction("ATTACK_HEAVY"));
        this.addToBot(new VFXAction(p, new CleaveEffect(), 0.1F));
        this.addToBot(new DamageAllEnemiesAction(p, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE));
    }

    public void onSmith(){
        AbstractDungeon.effectsQueue.add(new PurgeCardEffect(this));
        AbstractDungeon.player.masterDeck.removeCard(this);
    }

    // Upgraded stats.
    @Override
    public void upgrade() {
        setPurge(true);
        upgradeName();
        upgradeDescription();
    }

    @Override
    public List<TooltipInfo> getCustomTooltips() {
        List<TooltipInfo> toolTips = new ArrayList<>();
        String title = languagePack.getCardStrings(ID).EXTENDED_DESCRIPTION[0];
        String desc = languagePack.getCardStrings(ID).EXTENDED_DESCRIPTION[1];
        toolTips.add(new TooltipInfo(title, desc));
        return toolTips;
    }

    @Override
    public void setPurge(boolean purge) {
        this.purge = purge;

    }

    @Override
    public boolean getPurge() {
        return purge;
    }

    @Override
    public boolean queuedForPurge() {
        return queuedForPurge;
    }

    @Override
    public void setQueuedForPurge(boolean queuedForPurge) {
        this.queuedForPurge = queuedForPurge;
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "transformCard", paramtypez = { AbstractCard.class, boolean.class, Random.class})
    public static class PreventUpgradeTransformPatch {

        @SpireInsertPatch(
                locator=Locator.class,
                localvars={"transformedCard"}
        )
        public static SpireReturn<Void> Insert(AbstractCard c, boolean autoUpgrade, Random rng, @ByRef AbstractCard[] transformedCard){
            if (transformedCard[0] instanceof MakeshiftSpear && (autoUpgrade || AbstractDungeon.player.hasRelic(MoltenEgg2.ID))){
                AbstractCard newTransformedCard = transformedCard[0];
                do {
                    transformedCard[0] = AbstractDungeon.returnTrulyRandomCardFromAvailable(c, rng).makeCopy();
                } while ((transformedCard[0] instanceof MakeshiftSpear));
                AbstractDungeon.transformedCard = transformedCard[0];
            }

            return SpireReturn.Continue();
        }
        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws PatchingException, CannotCompileException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(UnlockTracker.class, "markCardAsSeen");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = UpgradeRandomCardAction.class, method = "update")
    public static class PreventRandomUpgrade {

        @SpireInsertPatch(
                locator=Locator.class,
                localvars={"upgradeable"}
        )
        public static void Insert(UpgradeRandomCardAction __instance, @ByRef CardGroup[] upgradeable){
            Iterator<AbstractCard> iterator = upgradeable[0].group.iterator();
            while (iterator.hasNext()){
                AbstractCard card = iterator.next();
                if (card instanceof Makeshift){
                    iterator.remove();
                }
            }
        }
        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws PatchingException, CannotCompileException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(CardGroup.class, "size");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = LessonLearnedAction.class, method = "update")
    public static class PreventLessonsLearnedUpgrade {

        @SpireInsertPatch(
                locator=Locator.class,
                localvars={"possibleCards"}
        )
        public static void Insert(LessonLearnedAction __instance, @ByRef ArrayList<AbstractCard>[] possibleCards){
            Iterator<AbstractCard> iterator = possibleCards[0].iterator();
            while (iterator.hasNext()){
                AbstractCard card = iterator.next();
                if (card instanceof Makeshift){
                    iterator.remove();
                }
            }
        }
        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws PatchingException, CannotCompileException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "isEmpty");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = TinyHouse.class, method = "onEquip")
    public static class PreventTinyHouseUpgrade {

        @SpireInsertPatch(
                locator=Locator.class,
                localvars={"upgradableCards"}
        )
        public static void Insert(TinyHouse __instance, @ByRef ArrayList<AbstractCard>[] upgradableCards){
            Iterator<AbstractCard> iterator = upgradableCards[0].iterator();
            while (iterator.hasNext()){
                AbstractCard card = iterator.next();
                if (card instanceof Makeshift){
                    iterator.remove();
                }
            }
        }
        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws PatchingException, CannotCompileException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Collections.class, "shuffle");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }
}
