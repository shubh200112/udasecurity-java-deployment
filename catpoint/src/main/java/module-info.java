module catpoint {

    requires java.desktop;
    requires java.prefs;

    requires com.google.gson;
    requires com.google.common;
    requires miglayout.swing;

    requires image.service;

    opens com.udacity.catpoint.data to com.google.gson;
opens com.udacity.catpoint.service to org.mockito, org.junit.platform.commons;

}