package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes;

import javafx.scene.Parent;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;

public abstract class AddressPane extends FXObject
{
    private final AddressDataType addressDataType;
    public AddressPane(String name, AddressDataType addressDataType)
    {
        super(name);

        this.addressDataType = addressDataType;
    }

    public AddressDataType getAddressDataType()
    {
        return addressDataType;
    }

    public abstract AddressStringParser<?> getAddressStringParser();

    public abstract Parent getMainParent();

    public abstract void parseAttributeChangerList(SetupPaneAttributeChangerList<? extends AddressAttribute> attributeChangerList);

    public abstract void setAsState();

    public abstract void setAsGlobal();

    //public abstract void parseUndoRedoManager(UndoRedoManager undoRedoManager);
}
