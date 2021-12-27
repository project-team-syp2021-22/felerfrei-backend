package at.htlstp.felerfrei.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageTest {


    @Test
    void equals() {
        var image1 = new Image(1, "my path");
        var image2 = new Image(1, "different path");

        assertEquals(image1, image2);
    }

    @Test
    void not_equals() {
        var image1 = new Image(1, "my path");
        var image2 = new Image(2, "different id");
        assertNotEquals(image1, image2);
    }

    @Test
    void same_hash_code() {
        var image1 = new Image(1, "my path");
        var image2 = new Image(1, "different path");
        assertEquals(image1.hashCode(), image2.hashCode());
    }

}