package parozzz.github.com.hmi.attribute.impl;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.ParsableAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.hmi.main.picturebank.PictureBankStage;
import parozzz.github.com.hmi.serialize.JSONSerializables;

import java.util.ArrayList;
import java.util.Collections;

public class BackgroundAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BACKGROUND_ATTRIBUTE";

    public static final AttributeProperty<Color> BACKGROUND_COLOR = new ParsableAttributeProperty<>("BackgroundColor", Color.WHITE, JSONSerializables.COLOR);
    public static final AttributeProperty<String> PICTURE_BANK_IMAGE_NAME = new StringAttributeProperty("PictureBankImageName", "");
    public static final AttributeProperty<Integer> CORNER_RADII = new NumberAttributeProperty<>("CornerRadii", 0, Number::intValue);
    public static final AttributeProperty<Boolean> STRETCH_IMAGE = new BooleanAttributeProperty("StretchImage");

    private final PictureBankStage pictureBank;
    private Background background;

    public BackgroundAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.BACKGROUND, ATTRIBUTE_NAME);

        this.pictureBank = attributeMap.getControlWrapper().getControlMainPage()
                .getMainEditStage().getPictureBankStage();

        super.getAttributePropertyManager().addAll(BACKGROUND_COLOR, PICTURE_BANK_IMAGE_NAME, CORNER_RADII, STRETCH_IMAGE);
        this.update(); //Have the background not to be null at startup!
    }

    public Background getBackground()
    {
        return background;
    }

    @Override
    public void update()
    {
        //Reset the image in case is not valid below
        Image image = null;

        var imageName = this.getValue(PICTURE_BANK_IMAGE_NAME);
        if(imageName != null && !imageName.isEmpty())
        {
            var imageURI = pictureBank.getImageURI(imageName);
            if(imageURI != null)
            {
                image = new Image(imageURI.toString());
            }else
            {
                setValue(PICTURE_BANK_IMAGE_NAME, "");
            }
        }

        var backgroundImageList = new ArrayList<BackgroundImage>();
        if(image != null)
        {
            var stretch = this.getValue(STRETCH_IMAGE);

            //width, height, widthAsPercentage, heightAsPercentage
            var backgroundSize = stretch
                                 ? new BackgroundSize(1.0, 1.0, true, true, false, false)
                                 : BackgroundSize.DEFAULT;
            backgroundImageList.add(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        }

        var backgroundColor = this.getValue(BACKGROUND_COLOR);
        var cornerRadii = this.getValue(CORNER_RADII);
        var backgroundFill = new BackgroundFill(backgroundColor, new CornerRadii(cornerRadii), Insets.EMPTY);
        background = new Background(Collections.singletonList(backgroundFill), backgroundImageList);
    }
}
