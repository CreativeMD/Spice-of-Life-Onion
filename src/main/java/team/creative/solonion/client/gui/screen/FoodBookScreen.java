package team.creative.solonion.client.gui.screen;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.solonion.api.FoodPlayerData;
import team.creative.solonion.api.SOLOnionAPI;
import team.creative.solonion.client.gui.BenefitsPage;
import team.creative.solonion.client.gui.DiversityPage;
import team.creative.solonion.client.gui.FoodListPage;
import team.creative.solonion.client.gui.Page;
import team.creative.solonion.client.gui.PageFlipButton;
import team.creative.solonion.client.gui.elements.UIElement;
import team.creative.solonion.client.gui.elements.UIImage;
import team.creative.solonion.client.gui.elements.UILabel;
import team.creative.solonion.common.SOLOnion;
import team.creative.solonion.common.benefit.BenefitThreshold;

@OnlyIn(Dist.CLIENT)
public final class FoodBookScreen extends Screen implements PageFlipButton.Pageable {
    private static final ResourceLocation texture = ResourceLocation.tryBuild(SOLOnion.MODID, "textures/gui/food_book.png");
    private static final UIImage.Image bookImage = new UIImage.Image(texture, new Rectangle(0, 0, 186, 192));
    public static final UIImage.Image carrotImage = new UIImage.Image(texture, new Rectangle(0, 240, 16, 16));
    
    public static final Color fullBlack = Color.BLACK;
    public static final Color lessBlack = new Color(0, 0, 0, 128);
    public static final Color leastBlack = new Color(0, 0, 0, 64);
    public static final Color activeGreen = new Color(29, 104, 29, 255);
    public static final Color inactiveRed = new Color(104, 29, 29, 255);
    
    private final List<UIElement> elements = new ArrayList<>();
    private UIImage background;
    private UILabel pageNumberLabel;
    
    private PageFlipButton nextPageButton;
    private PageFlipButton prevPageButton;
    
    private Player player;
    private FoodPlayerData foodData;
    
    private final List<Page> pages = new ArrayList<>();
    private int currentPageNumber = 0;
    
    public static void open(Player player) {
        Minecraft.getInstance().setScreen(new FoodBookScreen(player));
    }
    
    public FoodBookScreen(Player player) {
        super(Component.literal(""));
        this.player = player;
    }
    
    @Override
    public void init() {
        super.init();
        
        foodData = SOLOnionAPI.getFoodCapability(player);
        
        background = new UIImage(bookImage);
        background.setCenterX(width / 2);
        background.setCenterY(height / 2);
        
        elements.clear();
        
        // page number
        pageNumberLabel = new UILabel("1");
        pageNumberLabel.setCenterX(background.getCenterX());
        pageNumberLabel.setMinY(background.getMinY() + 156);
        elements.add(pageNumberLabel);
        
        initPages();
        
        int pageFlipButtonSpacing = 50;
        prevPageButton = addRenderableWidget(new PageFlipButton(background.getCenterX() - pageFlipButtonSpacing / 2 - PageFlipButton.width, background
                .getMinY() + 152, PageFlipButton.Direction.BACKWARD, this));
        nextPageButton = addRenderableWidget(new PageFlipButton(background.getCenterX() + pageFlipButtonSpacing / 2, background
                .getMinY() + 152, PageFlipButton.Direction.FORWARD, this));
        
        updateButtonVisibility();
    }
    
    private void initPages() {
        pages.clear();
        
        double foodDiversity = foodData.foodDiversity(player);
        int foodEaten = foodData.trackCount();
        pages.add(new DiversityPage(foodDiversity, foodEaten, background.frame));
        
        addPages("food_queue_label", Lists.newArrayList(foodData), player);
        
        List<BenefitThreshold> active = new ArrayList<>();
        List<BenefitThreshold> inactive = new ArrayList<>();
        for (BenefitThreshold threshold : SOLOnion.CONFIG.benefits)
            if (threshold.threshold <= foodDiversity)
                active.add(threshold);
            else
                inactive.add(threshold);
            
        addPages("active_benefits_header", active, activeGreen);
        
        if (SOLOnion.CONFIG.shouldShowInactiveBenefits)
            addPages("inactive_benefits_header", inactive, inactiveRed);
        
        active = new ArrayList<>();
        inactive = new ArrayList<>();
        for (BenefitThreshold threshold : SOLOnion.CONFIG.detriments)
            if (threshold.threshold > foodDiversity)
                active.add(threshold);
            else
                inactive.add(threshold);
            
        addPages("active_detriments_header", active, inactiveRed);
        
        if (SOLOnion.CONFIG.shouldShowInactiveDetriments)
            addPages("inactive_detriments_header", inactive, activeGreen);
    }
    
    private void addPages(String headerLocalizationPath, List<BenefitThreshold> benefitInfoList, Color activeColor) {
        String header = LanguageUtils.translate("gui.solonion.food_book." + headerLocalizationPath);
        
        pages.addAll(BenefitsPage.pages(background.frame, header, benefitInfoList, activeColor));
    }
    
    private void addPages(String headerLocalizationPath, List<ItemStack> stacks, Player player) {
        String header = LanguageUtils.translate("gui.solonion.food_book." + headerLocalizationPath, stacks.size());
        pages.addAll(FoodListPage.pages(background.frame, header, stacks, player));
    }
    
    @Override
    protected void renderBlurredBackground() {}
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //renderBackground(graphics, mouseX, mouseY, partialTicks);
        
        UIElement.render(graphics, background, mouseX, mouseY);
        
        super.render(graphics, mouseX, mouseY, partialTicks);
        
        if (!pages.isEmpty()) { // might not be loaded yet; race condition
            // current page
            UIElement.render(graphics, elements, mouseX, mouseY);
            UIElement.render(graphics, pages.get(currentPageNumber), mouseX, mouseY);
        }
    }
    
    @Override
    public void switchToPage(int pageNumber) {
        if (!isWithinRange(pageNumber))
            return;
        
        currentPageNumber = pageNumber;
        updateButtonVisibility();
        
        pageNumberLabel.text = "" + (currentPageNumber + 1);
    }
    
    @Override
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }
    
    @Override
    public boolean isWithinRange(int pageNumber) {
        return pageNumber >= 0 && pageNumber < pages.size();
    }
    
    private void updateButtonVisibility() {
        prevPageButton.updateState();
        nextPageButton.updateState();
    }
}
