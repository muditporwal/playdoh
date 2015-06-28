package com.lsoftware.playdoh;

import com.lsoftware.playdoh.exception.FixtureNotFoundException;
import com.lsoftware.playdoh.objects.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlaydohBuildTest {

    @Test
    public void testBuildDummyObject() throws Exception {
        final Dummy builtDummy = Playdoh.build(Dummy.class);
        assertNotNull(builtDummy);
        assertNotNull(builtDummy.getIntegerValue());
        assertNotNull(builtDummy.getStringValue());

        final NestedDummy nestedDummy = builtDummy.getNestedDummy();
        assertNotNull(nestedDummy);
        assertNotNull(nestedDummy.getIntValue());
        assertNotNull(nestedDummy.getStringValue());
    }

    @Test
    public void testBuildParentObject() {
        final ParentObject builtParent = Playdoh.build(ParentObject.class);
        assertNotNull(builtParent.getParentString());
    }

    @Test
    public void testBuildInheritanceObject() {
        final InheritanceObject builtInheritance = Playdoh.build(InheritanceObject.class);
        assertNotNull(builtInheritance.getInheritanceString());
        assertNotNull(builtInheritance.getParentString());
    }

    @Test
    public void testBuildObjectWithArray() {
        final ObjectWithArray objectWithArray = Playdoh.build(ObjectWithArray.class);
        assertNotNull(objectWithArray);
        assertTrue(objectWithArray.getIntArray().length > 0);
    }

    @Test
    public void testBuildObjectWithPrimitiveArray() {
        final ObjectWithPrimitiveArray objectWithPrimitiveArray = Playdoh.build(ObjectWithPrimitiveArray.class);
        assertNotNull(objectWithPrimitiveArray);
        assertTrue(objectWithPrimitiveArray.getIntArray().length > 0);
    }

    @Test
    public void testBuildObjectWithEnum() {
        final ObjectWithEnum objectWithEnum = Playdoh.build(ObjectWithEnum.class);
        assertNotNull(objectWithEnum);
    }

    @Test
    public void testBuildObjectWithSpecificValueOnField() {
        Dummy dummy = Playdoh.builder(Dummy.class)
                .with("stringValue", "test")
                .with("integerValue", 123)
                .build();

        assertEquals("test", dummy.getStringValue());
        assertEquals(123, dummy.getIntegerValue());
    }

    @Test
    public void testBuildObjectWithSpecificNestedObject() {
        NestedDummy nestedDummy = Playdoh.build(NestedDummy.class);

        Dummy dummy = Playdoh.builder(Dummy.class)
                .with("nestedDummy", nestedDummy)
                .build();

        assertEquals(nestedDummy, dummy.getNestedDummy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildObjectWithAbsentSpecificFieldName() {
        Playdoh.builder(Dummy.class)
                .with("absentValue", "test")
                .build();
    }

    @Test
    public void testBuildObjectFromFixture() {
        User user1 = Playdoh.build(User.class, "aUser");
        assertNotNull(user1);
        assertEquals("Lucas Saldanha", user1.getName());
        assertEquals("lucas@example.com", user1.getEmail());
        assertEquals(123, user1.getAge());
    }

    @Test
    public void testBuildOtherObjectFromFixture() {
        User user1 = Playdoh.build(User.class, "anotherUser");
        assertNotNull(user1);
        assertEquals("Abdi Abidu", user1.getName());
        assertEquals("aa@example.com", user1.getEmail());
        assertEquals(999, user1.getAge());
    }

    @Test(expected = FixtureNotFoundException.class)
    public void testBuildObjectFromAbsentFixture() {
        Playdoh.build(ClassWithoutFixture.class, "aClass");
    }
}