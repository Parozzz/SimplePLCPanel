package parozzz.github.com.util.functionalinterface;

@FunctionalInterface
public interface TriConsumer<A, B, C>
{
    void accept(A a, B b, C c);
}
