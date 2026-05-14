module catpoint {

    requires java.desktop;
    requires java.prefs;

    requires com.google.gson;
    requires com.google.common;
    requires miglayout.swing;

    requires image.service;

    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;

    opens com.udacity.catpoint.service;
    opens com.udacity.catpoint.data to com.google.gson;

}
