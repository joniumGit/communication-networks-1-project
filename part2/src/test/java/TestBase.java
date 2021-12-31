import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
abstract class TestBase {

    abstract int index();

    @Test
    void test() {
        TestSupport.paramTest(TestSupport.provider().get(index()));
    }

}
