package thePirate.cards.targeting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.cards.targeting.TargetingHandler;
import com.evacipated.cardcrawl.mod.stslib.patches.CustomTargeting;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.*;

public class RelicTargeting extends TargetingHandler<AbstractRelic> {
    @SpireEnum
    public static AbstractCard.CardTarget RELIC;

    public static AbstractRelic getTarget(AbstractCard card) {
        return CustomTargeting.getCardTarget(card);
    }

    private AbstractRelic hovered = null;

    @Override
    public boolean hasTarget() {
        return hovered != null;
    }

    @Override
    public void updateHovered() {
        hovered = null;

        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            relic.hb.update();
            if (relic.hb.hovered && canTarget(relic)) {
                hovered = relic;
                break;
            }
        }
    }


    @Override
    public AbstractRelic getHovered() {
        return hovered;
    }

    @Override
    public void clearHovered() {
        hovered = null;
    }


    public static boolean canTarget(AbstractRelic relic){
        boolean canTarget = false;
        switch (relic.relicId){
            case VelvetChoker.ID:
            case SneckoEye.ID:
            case BagOfPreparation.ID:
            case BronzeScales.ID:
            case BloodVial.ID:
            case BagOfMarbles.ID:
            case Vajra.ID:
            case Anchor.ID:
            case HappyFlower.ID:
            case OddlySmoothStone.ID:
            case MercuryHourglass.ID:
            case HornCleat.ID:
            case DuVuDoll.ID:
            case FossilizedHelix.ID:
            case Girya.ID:
            case IncenseBurner.ID:
            case Pocketwatch.ID:
            case CaptainsWheel.ID:
            case GamblingChip.ID:
            case ThreadAndNeedle.ID:
            case ClockworkSouvenir.ID:
            case Toolbox.ID:
            case Sling.ID:
            case Akabeko.ID:
            case CentennialPuzzle.ID:
            case Pantograph.ID:
            case CoffeeDripper.ID:
            case SlaversCollar.ID:
            case RunicDome.ID:
            case FusionHammer.ID:
            case PhilosopherStone.ID:
            case BustedCrown.ID:
            case Ectoplasm.ID:
            case Sozu.ID:
            case CursedKey.ID:
            case Lantern.ID:
            case AncientTeaSet.ID:
            case CultistMask.ID:
            case Enchiridion.ID:
            case MutagenicStrength.ID:
            case Necronomicon.ID:
            case RedMask.ID:
            case WarpedTongs.ID:
                //had to hardcode these...
            case "thePirate:BlackCannon":
            case "thePirate:SilverCannon":
            case "thePirate:GoldCannon":
            case "thePirate:PlatinumCannon":
            case "thePirate:Motivation":
            case "thePirate:GreedyChest":
            case "thePirate:ExperimentalCannon":
            case "thePirate:ExoticDish":
            case "thePirate:WoodenLeg":
            case "thePirate:NavigationDevice":
                canTarget = true;
                break;
            default:
                canTarget = false;
        }
        return canTarget;
    }


    private static void renderReticleCorner(AbstractRelic relic, SpriteBatch sb, float x, float y, boolean flipX, boolean flipY) {
        Color reticleColor = new Color(1.0F, 1.0F, 1.0F, 0.0F);
        Color reticleShadowColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
        float reticleAlpha = 1.0F;

        reticleShadowColor.a = reticleAlpha / 4.0F;
        sb.setColor(reticleShadowColor);
        sb.draw(ImageMaster.RETICLE_CORNER, relic.hb.cX + x - 18.0F + 4.0F * Settings.scale, relic.hb.cY + y - 18.0F - 4.0F * Settings.scale, 18.0F, 18.0F, 36.0F, 36.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 36, 36, flipX, flipY);

        reticleColor.a = reticleAlpha;
        sb.setColor(reticleColor);
        sb.draw(ImageMaster.RETICLE_CORNER, relic.hb.cX + x - 18.0F, relic.hb.cY + y - 18.0F, 18.0F, 18.0F, 36.0F, 36.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 36, 36, flipX, flipY);
    }

    public static void renderReticle(AbstractRelic relic, SpriteBatch sb) {
        float reticleOffset = 0.0f;
        renderReticleCorner(relic, sb, -relic.hb.width / 2.0F + reticleOffset, relic.hb.height / 2.0F - reticleOffset, false, false);
        renderReticleCorner(relic, sb, relic.hb.width / 2.0F - reticleOffset, relic.hb.height / 2.0F - reticleOffset, true, false);
        renderReticleCorner(relic, sb, -relic.hb.width / 2.0F + reticleOffset, -relic.hb.height / 2.0F + reticleOffset, false, true);
        renderReticleCorner(relic, sb, relic.hb.width / 2.0F - reticleOffset, -relic.hb.height / 2.0F + reticleOffset, true, true);
    }

    @Override
    public void renderReticle(SpriteBatch sb) {
        if (hovered != null) {
            renderReticle(hovered, sb);
        }
    }

}