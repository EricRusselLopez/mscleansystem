package src.main.java;
import javax.swing.SwingUtilities;


/**
 * Entry point for the Laundry Management & Monitoring System. This class
 * initializes the graphical user interface (GUI) using SwingUtilities to ensure
 * thread safety.
 *
 * @author Eric Russel M. Lopez
 * @version 24 (Java SE 24)
 * @since 2025
 * @license Educational Project Use Only - © 2025 Eric Russel M. Lopez
 * @see
 * <a href="https://github.com/EricRusselLopez/mscleansystem">GitHub
 * Repository</a>
 */

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
