package parozzz.github.com.simpleplcpanel.util.functionalinterface;

@FunctionalInterface
public interface TriConsumer<A, B, C>
{
    void accept(A a, B b, C c);
}
