package se.lublin.mumla.app;

import android.app.Activity;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MumlaApplicationTest extends AndroidTestCase {
    public void testMicrophoneVisibilityAcrossScreens() {
        MumlaApplication application = new MumlaApplication();
        List<Boolean> visibility = new ArrayList<>();
        application.setForegroundListener(visibility::add);
        Activity main = new Activity();
        Activity settings = new Activity();

        assertFalse(application.isForeground());
        application.onActivityStarted(main);
        application.onActivityStarted(settings);
        application.onActivityStopped(main);
        assertTrue(application.isForeground());
        assertEquals(Arrays.asList(false, true), visibility);

        application.onActivityPaused(settings);
        assertTrue(application.isForeground());
        application.onActivityStopped(settings);
        assertFalse(application.isForeground());
        application.onActivityStarted(main);
        assertEquals(Arrays.asList(false, true, false, true), visibility);
    }

    public void testServiceReceivesCurrentVisibilityAndCanUnregister() {
        MumlaApplication application = new MumlaApplication();
        Activity main = new Activity();
        application.onActivityStarted(main);

        List<Boolean> visibility = new ArrayList<>();
        application.setForegroundListener(visibility::add);
        assertEquals(Arrays.asList(true), visibility);

        application.setForegroundListener(null);
        application.onActivityStopped(main);
        assertEquals(Arrays.asList(true), visibility);
        assertFalse(application.isForeground());

        application.setForegroundListener(visibility::add);
        assertEquals(Arrays.asList(true, false), visibility);
    }
}
