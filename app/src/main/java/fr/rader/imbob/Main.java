package fr.rader.imbob;

import fr.rader.imbob.updater.PSLUpdater;

public class Main {

    public static void main(String[] args) {
        PSLUpdater updater = new PSLUpdater();
        updater.update();

        ImBob bobLite = new ImBob(updater);
        bobLite.start();
    }
}
