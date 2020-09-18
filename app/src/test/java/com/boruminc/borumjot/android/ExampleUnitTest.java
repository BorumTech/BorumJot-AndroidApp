package com.boruminc.borumjot.android;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

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