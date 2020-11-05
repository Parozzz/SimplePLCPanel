package parozzz.github.com.hmi.controls.controlwrapper.setup;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.effect.SepiaTone;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.extra.ControlWrapperSetupUtil;

public abstract class SetupPane<A extends Attribute> extends FXObject
{
    private final ControlWrapperSetupStage setupStage;
    private final Class<A> attributeClass;
    private final SetupPaneAttributeChangerList<A> attributeChangerList;

    private final Tab tab;

    public SetupPane(ControlWrapperSetupStage setupStage, String name, String tabName, Class<A> attributeClass)
    {
        super(name);

        this.setupStage = setupStage;
        this.attributeClass = attributeClass;
        this.attributeChangerList = new SetupPaneAttributeChangerList<>(this, attributeClass);

        this.tab = new Tab();
        tab.setText(tabName);
    }

    @Override
    public void setup()
    {
        super.setup();

        tab.setUserData(this);

        var stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(this.getMainParent());
        stackPane.setPadding(new Insets(3));
        tab.setContent(stackPane);
    }

    public ControlWrapperSetupStage getSetupStage()
    {
        return setupStage;
    }

    protected Tab getTab()
    {
        return tab;
    }

    public abstract Parent getMainParent();

    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(attributeClass);
    }

    public SetupPaneAttributeChangerList<A> getAttributeChangerList()
    {
        return attributeChangerList;
    }

    public void revertToDefaultValues()
    {
        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
        if(selectedControlWrapper != null)
        {
            //Set the changed data to ALL the states of the wrapper
            selectedControlWrapper.getStateMap().forEach(wrapperState ->
            {
                var attribute = AttributeFetcher.fetch(wrapperState, attributeClass);
                if (attribute != null)
                {
                    attributeChangerList.forEach(attributeChanger ->
                            attributeChanger.getPropertyBis().revertToDefaultValues(attribute)
                    );
                }
            });
        }
    }

    protected void clearAllControlEffect()
    {
        attributeChangerList.getPropertySet().forEach(property ->
        {
            var bean = property.getBean();
            if (bean instanceof Control)
            {
                ((Control) bean).setEffect(null);
            }
        });
    }

    protected void computeGlobalProperties()
    {
        this.computeProperties(false, true, false);
    }

    protected void computeProperties()
    {
        this.computeProperties(true, true, true);
    }

    protected void computeProperties(boolean addContextMenu, boolean addUndoRedo, boolean allowMultipleSelection)
    {
        var propertySet = attributeChangerList.getPropertySet();

        if(addUndoRedo)
        {
            setupStage.getUndoRedoManager().addProperties(propertySet, this);
        }

        propertySet.forEach(property ->
        {
            var bean = property.getBean();
            if (bean instanceof Control)
            {
                var control = (Control) bean;

                if(addContextMenu)
                {
                    var setDataToAllStateMenuItem = new MenuItem("Set data to all states");
                    setDataToAllStateMenuItem.setOnAction(actionEvent ->
                    {
                        var selectedControlWrapper = setupStage.getSelectedControlWrapper();
                        if(selectedControlWrapper != null)
                        {
                            ControlWrapperSetupUtil.writeAttributeChangerToAllStates(selectedControlWrapper, attributeClass,
                                    attributeChangerList, property);
                        }
                    });

                    var contextMenu = new ContextMenu();
                    contextMenu.getItems().addAll(setDataToAllStateMenuItem);
                    control.setContextMenu(contextMenu);
                }

                if(allowMultipleSelection)
                {
                    var selectAndWriteMultiple = setupStage.getSelectAndMultipleWrite();
                    selectAndWriteMultiple.onSelectingMultiplesChangeListener(selectingMultiples ->
                    {
                        control.setDisable(selectingMultiples);
                        control.setEffect(null);
                    });

                    var areaParent = control.getParent();
                    if (areaParent instanceof StackPane)
                    {
                        areaParent.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
                        {
                            if (selectAndWriteMultiple.isSelectingMultiples())
                            {
                                if (control.getEffect() == null)
                                {
                                    selectAndWriteMultiple.addSelected(property, attributeClass, attributeChangerList);
                                    control.setEffect(new SepiaTone());
                                } else
                                {
                                    selectAndWriteMultiple.removeSelected(property);
                                    control.setEffect(null);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
