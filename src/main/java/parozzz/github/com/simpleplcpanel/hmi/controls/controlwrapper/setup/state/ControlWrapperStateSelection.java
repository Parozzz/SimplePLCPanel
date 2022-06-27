package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.state;

import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;

import java.util.*;
import java.util.function.Consumer;

public class ControlWrapperStateSelection
{
    public enum SelectionType
    {
        NOT_SELECTED,
        MAIN,
        SECONDARY;
    }

    private final ControlWrapperSetupStage setupStage;
    private final Set<WrapperState> secondarySelectedWrapperStateSet;
    private WrapperState mainSelectedState;

    public ControlWrapperStateSelection(ControlWrapperSetupStage setupStage)
    {
        this.setupStage = setupStage;
        secondarySelectedWrapperStateSet = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public int getCount()
    {
        return 1 + secondarySelectedWrapperStateSet.size(); //There is always at least 1 state, that is the main.
    }

    public void setMainState(WrapperState wrapperState)
    {
        var selectedControlWrapper = setupStage.getSelectedControlWrapper();

        //When changing the mainSelectedWrapperStateProperty it will call the listener inside the StateMap
        //of the ControlWrapper updating everything.
        secondarySelectedWrapperStateSet.clear(); //THIS NEED TO BE FIRST. Otherwise, when setting a new value to main
        //all the secondary data will be overwritten by default values!
        //More checks has been added inside the SetupPaneAttributeChanger to
        //avoid this problem, but you never know.
        mainSelectedState = wrapperState;
        if (wrapperState != null)
        {
            Objects.requireNonNull(selectedControlWrapper, "Trying to set a selected state while the selected ControlWrapper is null?");

            //When a WrapperState is set, force it inside the ControlWrapper to allow the listener to be called (And execute the ABOVE).
            selectedControlWrapper.getStateMap().setWrapperState(wrapperState);
        }
    }


    public SelectionType getSelectionTypeOf(WrapperState wrapperState)
    {
        if(mainSelectedState == wrapperState)
        {
            return SelectionType.MAIN;
        }
        else if (secondarySelectedWrapperStateSet.contains(wrapperState))
        {
            return SelectionType.SECONDARY;
        }

        return SelectionType.NOT_SELECTED;
    }

    public WrapperState getMainState()
    {
        return mainSelectedState;
    }

    public Set<WrapperState> getAllStates()
    {
        var set = new HashSet<>(secondarySelectedWrapperStateSet);
        set.add(mainSelectedState);
        return set;
    }

    public Set<WrapperState> getSecondaryStates()
    {
        return Collections.unmodifiableSet(secondarySelectedWrapperStateSet);
    }

    public void addSecondaryState(WrapperState wrapperState)
    {
        //If there is not a main selection, it will be forced into!
        if (mainSelectedState == null)
        {
            this.setMainState(wrapperState);
        } else if (mainSelectedState != wrapperState) //Cannot add a main as a secondary!
        {
            secondarySelectedWrapperStateSet.add(wrapperState);
        }
    }

    public void forEach(Consumer<WrapperState> consumer)
    {
        if(mainSelectedState != null) //If the main is null, there can't be any secondary.
        {
            consumer.accept(mainSelectedState);
            secondarySelectedWrapperStateSet.forEach(consumer);
        }
    }

}
