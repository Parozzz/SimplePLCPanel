package parozzz.github.com.util.functionalinterface;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FunctionalInterfaceUtil
{
    private final static Consumer EMPTY_CONSUMER = ignored -> {};
    private final static BiConsumer EMPTY_BI_CONSUMER = (ignored, ignored1) -> {};
    private FunctionalInterfaceUtil()
    {

    }

    public static <T> Consumer<T> emptyConsumer()
    {
        return EMPTY_CONSUMER;
    }

    public static <T, H> BiConsumer<T, H> emptyBiConsumer()
    {
        return EMPTY_BI_CONSUMER;
    }
}
