package com.boruminc.borumjot;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class JottingHierarchyUnitTest {
    @Test
    public void jottingsHierarchy_isCorrect() {
        assertTrue("Jotting is abstract", Modifier.isAbstract(Jotting.class.getModifiers()));
        assertTrue("Note extends Jottting", Jotting.class.isAssignableFrom(Note.class));
        assertTrue("Task extends Jotting", Jotting.class.isAssignableFrom(Task.class));
    }

    @Test
    public void noteShareeAddsWhenCorrect() {
        Note myNote = new Note("");
        myNote.addSharee("Varun"); // Add a new sharee to an empty list of sharees
        assertArrayEquals("Note.addSharee() when valid and returns true", myNote.getSharees().toArray(), new String[] {"Varun"});
    }

    @Test
    public void noteShareeDoesNotAddWhenDuplicate() {
        Note myNote = new Note("");
        myNote.addSharee("Varun");
        myNote.addSharee("Varun");
        assertArrayEquals("Note.addSharee() when invalid and returns false", myNote.getSharees().toArray(), new String[] {"Varun"});
    }
}
