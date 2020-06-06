package crux;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class SubmissionTests {
    @Test
    public void checkAuthors() {
        Assertions.assertTrue(Authors.all.length > 0);
    }
}
